package com.inqool.dcap.ip.tile;

import com.google.common.io.ByteStreams;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.ip.ImageType;
import com.inqool.dcap.ip.gm.ConvertTempCmd;
import com.inqool.dcap.ip.jp2.OpenJPEGLoader;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.im4java.core.*;
import org.im4java.process.ProcessStarter;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ImageTiler {
    @Inject
    @ConfigProperty(name = "image.tile.size")
    private Integer tileSize;

    @Inject
    @ConfigProperty(name = "image.tile.format")
    private String tileFormat;

    @Inject
    @ConfigProperty(name = "graphicsmagick.folder")
    private String gmFolder;

    @Inject
    @ConfigProperty(name = "tiler.temp")
    private String tempFolder;

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private OpenJPEGLoader openJPEGLoader;

    @PostConstruct
    private void postConstruct() {
        System.setProperty("im4java.useGM", "true");
        ProcessStarter.setGlobalSearchPath(gmFolder);
    }

    public TiledImage tileImage(InputStream in, ImageType type) throws IOException {
        logger.debug("Image tiling started.");
        File srcImage;

        switch (type) {
            case JPEG2000:
                logger.debug("Image is JPEG2000 - needs conversion.");
                srcImage = openJPEGLoader.convertJPEG2000ToTIF(in);
                break;
            case JPEG:
                logger.debug("Creating temporary file.");
                srcImage = new File(tempFolder, UUID.randomUUID().toString() + "." + tileFormat);

                try (OutputStream srcStream = new FileOutputStream(srcImage)) {
                    // store to temp
                    ByteStreams.copy(in, srcStream);
                }
                break;
            default:
                throw new IOException("Invalid image format specified .");
        }

        try {
            try {
                // convert to MPC
                logger.debug("Creating MPC from image.");
                File image = getMpc(srcImage);

                logger.debug("Retrieving image information.");
                Info imageInfo = new Info(image.getAbsolutePath(), true);

                int imageWidth = imageInfo.getImageWidth();
                int imageHeight = imageInfo.getImageHeight();

                int imageMax = Math.max(imageWidth, imageHeight);
                int levels = (int) Math.ceil(Math.log(imageMax) / Math.log(2));

                logger.debug("Width: {}, Height: {}, Levels: {}", imageWidth, imageHeight, levels);
                double width = imageWidth;
                double height = imageHeight;

                List<Tile> tiles = new ArrayList<>();
                for (int level = levels; level >= 0; level--) {
                    int nCols = (int) Math.ceil(width / tileSize);
                    int nRows = (int) Math.ceil(height / tileSize);

                    logger.debug("Level: {}, Width: {}, Height: {}, Columns: {}, Rows: {}", level, width, height, nCols, nRows);

                    tiles.addAll(getTiles(image, nCols, nRows, level));

                    // Scale down image for next level
                    width = Math.ceil(width / 2);
                    height = Math.ceil(height / 2);
                    File oldImage = image;
                    image = resizeImage(image, width, height);
                    deleteMpc(oldImage);

                }

                deleteMpc(image);

                return new TiledImage(tileFormat, 0, tileSize, imageWidth, imageHeight, tiles);

            } catch (InfoException ex) {
                throw new IOException(ex);
            }
        } finally {
            srcImage.delete();
        }
    }

    private void deleteMpc(File file) {
        if (file != null) {
            String path = file.getAbsolutePath();
            File cache = new File(path.substring(0, path.length() - ".mpc".length()) + ".cache");

            file.delete();
            cache.delete();
        }
    }

    private File getMpc(File img) throws IOException {
        File image = new File(tempFolder, UUID.randomUUID().toString() + ".mpc");

        try {
            Info imageInfo = new Info(img.getAbsolutePath(), true);

            ConvertCmd cmd = new ConvertTempCmd(tempFolder);

            IMOperation op = new IMOperation();
            op.addImage(img.getAbsolutePath());

            op.resize(imageInfo.getImageWidth(), imageInfo.getImageHeight());
            op.addImage(image.getAbsolutePath());


            cmd.run(op);

            return image;
        } catch (InterruptedException | IM4JavaException e) {
            throw new IOException(e);
        }
    }

    private List<Tile> getTiles(File img, int nCols, int nRows, int level) throws IOException {
        List<Tile> tiles = new ArrayList<>();

        File image = new File(tempFolder, UUID.randomUUID().toString() + "");

        try {
            ConvertCmd cmd = new ConvertTempCmd(tempFolder);

            // crop whole image into tiles with one command
            IMOperation op = new IMOperation();
            op.crop(tileSize, tileSize);
            op.addImage(img.getAbsolutePath());
            op.p_adjoin();
            op.addImage(image.getAbsolutePath() + "_%d."+tileFormat);

            try {
                cmd.run(op);
            } catch (InterruptedException | IM4JavaException e) {
                throw new IOException(e);
            }

            // construct files on top of created files
            for (int row = 0; row < nRows; row++) {
                for (int col = 0; col < nCols; col++) {
                    int index = (row * nCols) + col;
                    File data = new File(image.getAbsolutePath() + "_" + index + "." + tileFormat);

                    tiles.add(new Tile(data, level, row, col));
                }
            }


            return tiles;
        } finally {
            image.delete();
        }
    }

    private File resizeImage(File img, double width, double height) throws IOException {
        File image = new File(tempFolder, UUID.randomUUID().toString() + ".mpc");

        ConvertCmd cmd = new ConvertTempCmd(tempFolder);

        IMOperation op = new IMOperation();
        op.addImage(img.getAbsolutePath());
        op.resize((int)width,(int)height);
        op.addImage(image.getAbsolutePath());

        try {
            cmd.run(op);
        } catch (InterruptedException | IM4JavaException e) {
            throw new IOException(e);
        }

        return image;
    }
}
