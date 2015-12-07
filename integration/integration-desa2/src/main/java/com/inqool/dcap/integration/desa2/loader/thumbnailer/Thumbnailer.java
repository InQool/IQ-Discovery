package com.inqool.dcap.integration.desa2.loader.thumbnailer;

import com.google.common.io.ByteStreams;
import com.inqool.dcap.config.Zdo;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author Lukas Jane (inQool) 14. 4. 2015.
 */
@RequestScoped
public class Thumbnailer {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private OpenJPEGLoader openJPEGLoader;

    @Inject
    @ConfigProperty(name = "thumbnail.tmp")
    private String tempFolder;

    @Inject
    @ConfigProperty(name = "graphicsmagick.folder")
    private String gmFolder;

    @PostConstruct
    private void postConstruct() {
        System.setProperty("im4java.useGM", "true");
        ProcessStarter.setGlobalSearchPath(gmFolder);
    }

    private File jpegImage;
    private File jpegThumbnail;

    /**
     * This method creates jpeg image and jpeg thumbnail that can be retrieved by corresponding methods.
     * When you are done with them, you must use the clean() method to delete these temporary files.
     * @param in
     * @param mimeType
     * @throws IOException
     */
    public void parseImageStream(InputStream in, String mimeType) throws IOException {
        clean();
        logger.debug("Processing another image.");
        //Convert other image formats to jpeg
        if ("image/jp2".equals(mimeType) || "image/tiff".equals(mimeType)) {
            File tifImage = null;
            try {
                if("image/jp2".equals(mimeType)) {  //Convert JPEG2000 to TIFF
                    tifImage = openJPEGLoader.convertJPEG2000ToTIF(in);
                }
                else { //If it already was TIFF, just put it to file
                    tifImage = new File(tempFolder, UUID.randomUUID().toString() + ".tif");
                    try (OutputStream tifWriterStream = new FileOutputStream(tifImage)) {
                        ByteStreams.copy(in, tifWriterStream);    // store to temp
                    }
                }
                //Convert TIFF to JPEG
                ConvertCmd cmd = new ConvertTempCmd(tempFolder);
                String thumbUuid = UUID.randomUUID().toString();
                jpegImage = new File(tempFolder, thumbUuid + ".jpg");
                IMOperation op = new IMOperation();
                op.addImage(tifImage.getAbsolutePath());
                op.addImage(jpegImage.getAbsolutePath());
                try {
                    cmd.run(op);
                } catch (InterruptedException | IM4JavaException e) {
                    throw new IOException(e);
                }
            } finally {
                if (tifImage != null) {
                    tifImage.delete();
                }
            }
        } else if ("video/mp4".equals(mimeType)) {
            jpegImage = new File(tempFolder, UUID.randomUUID().toString() + ".jpg");
            try (OutputStream srcStream = new FileOutputStream(jpegImage)) {
                //in
                ByteStreams.copy(this.getClass().getClassLoader().getResourceAsStream("movie.jpg"), srcStream);    // store to temp
            }
        } else if ("application/pdf".equals(mimeType)) {
            //Create image from first page of PDF

            //Load PDF from Fedora to buffer
            ByteBuffer byteBuffer;
            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                ByteStreams.copy(in, buffer);    // store to temp
                in.close();
                buffer.flush();
                byteBuffer = ByteBuffer.wrap(buffer.toByteArray());
            }

            PDFFile pdf = new PDFFile(byteBuffer);
            PDFPage page = pdf.getPage(0);

            // create the image
            Rectangle rect = new Rectangle(0, 0, (int) page.getBBox().getWidth(),
                    (int) page.getBBox().getHeight());
            BufferedImage bufferedImage = new BufferedImage(rect.width, rect.height,
                    BufferedImage.TYPE_INT_RGB);

            Image pageImage = page.getImage(rect.width, rect.height,    // width & height
                    rect,                       // clip rect
                    null,                       // null for the ImageObserver
                    true,                       // fill background with white
                    true                        // block until drawing is done
            );
            Graphics2D bufImageGraphics = bufferedImage.createGraphics();
            bufImageGraphics.drawImage(pageImage, 0, 0, null);

            //Write it to file
            jpegImage = new File(tempFolder, UUID.randomUUID().toString() + ".jpg");
            ImageIO.write(bufferedImage, "jpg", jpegImage);
        } else {    //Save jpg stream to file
            jpegImage = new File(tempFolder, UUID.randomUUID().toString() + ".jpg");
            try (OutputStream srcStream = new FileOutputStream(jpegImage)) {
                ByteStreams.copy(in, srcStream);    // store to temp
            }
        }

        //Crop the source image
        ConvertCmd cmd = new ConvertTempCmd(tempFolder);
        String thumbUuid = UUID.randomUUID().toString();
        jpegThumbnail = new File(tempFolder, thumbUuid + ".jpg");
        IMOperation op = new IMOperation();
        op.addImage(jpegImage.getAbsolutePath());
        op.resize(300, 300);
        op.addImage(jpegThumbnail.getAbsolutePath());
        try {
            cmd.run(op);
        } catch (InterruptedException | IM4JavaException e) {
            throw new IOException(e);
        }

//              The slow approach
//                    BufferedImage originalImage = ImageIO.read(in);
//                    System.out.println(System.currentTimeMillis());
//                    BufferedImage resizedImage = Scalr.resize(originalImage, 300);
//                    System.out.println(System.currentTimeMillis());
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    ImageIO.write(resizedImage, "jpg", baos);
//                    InputStream is = new ByteArrayInputStream(baos.toByteArray());
    }

    public File getJpegImage() {
        return jpegImage;
    }

    public File getJpegThumbnail() {
        return jpegThumbnail;
    }

    public void clean() {
        if(jpegImage != null) {
            jpegImage.delete();
            jpegImage = null;
        }
        if(jpegThumbnail != null) {
            jpegThumbnail.delete();
            jpegThumbnail = null;
        }
    }
}
