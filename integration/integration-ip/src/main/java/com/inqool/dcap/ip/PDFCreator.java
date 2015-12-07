package com.inqool.dcap.ip;

import com.google.common.io.ByteStreams;
import com.inqool.dcap.SimpleImageInfo;
import com.inqool.dcap.common.dto.PdfCreatorDto;
import com.inqool.dcap.integration.model.ZdoFileType;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.ip.gm.ConvertTempCmd;
import gov.loc.standards.alto.ns_v2_.*;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * Created by John on 6.7.2015.
 *
 * This code was created by Kapurka and every bit of sanity found here was added by Lukess.
 */
@Dependent
@Stateless
@TransactionAttribute(TransactionAttributeType.NEVER)
public class PDFCreator {

    @Inject
    @ConfigProperty(name = "tmp.folder")
    private String tmpFolder;

    @Inject
    private DataStore store;

    @Inject
    @ConfigProperty(name = "graphicsmagick.folder")
    private String gmFolder;

    @PostConstruct
    private void postConstruct() {
        System.setProperty("im4java.useGM", "true");
        ProcessStarter.setGlobalSearchPath(gmFolder);
    }

    @Asynchronous
    public void createPdf(PdfCreatorDto pdfCreatorDto) {
        String pdfId = pdfCreatorDto.getPdfId();
        String waterMarkId = pdfCreatorDto.getWatermarkId();
        List<String> imageIds = pdfCreatorDto.getImageIds();
        List<String> altoIds = pdfCreatorDto.getAltoIds();
        File watermark = null;
        try {
            //Download watermark to temporary file
            if (waterMarkId != null) {
                watermark = new File(tmpFolder + UUID.randomUUID().toString() + ".png");
                InputStream imageInputStream = new URL(store.createUrl(waterMarkId)).openStream();
                FileOutputStream fileOutputStream = new FileOutputStream(watermark);
                ByteStreams.copy(imageInputStream, fileOutputStream);
            }

            try(PDDocument doc = new PDDocument()) {
                PDType0Font font = PDType0Font.load(doc, this.getClass().getClassLoader().getResourceAsStream("DM.ttf"));

                //Work through pages
                for (int i = 0; i < imageIds.size(); i++) {
                    String altoId = null;
                    if(i < altoIds.size()) {
                        altoId = altoIds.get(i);
                    }
                    convertPage(doc, imageIds.get(i), altoId, watermark, pdfCreatorDto.getWatermarkPosition(), font);
                }

                //Save pdf to temp file and upload to Fedora
                String pdfPath = tmpFolder + pdfId;
                File tmpOutput = null;
                try {
                    tmpOutput = new File(pdfPath);
                    doc.save(tmpOutput);
                    doc.close();
                    ZdoModel zdoModel = new ZdoModel(store.createUrl(pdfId), new FileInputStream(pdfPath));
                    zdoModel.add(ZdoTerms.mimeType, "application/pdf");
                    zdoModel.add(ZdoTerms.fileType, ZdoFileType.pdf.name());
                    store.update(zdoModel);
                } finally {
                    if (tmpOutput != null) {
                        tmpOutput.delete();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("PDF could not be created.", e);
        } finally {
            if(watermark != null) {
                watermark.delete();
            }
        }
    }

    public void convertPage(PDDocument doc, String imageId, String altoId, File watermark, String watermarkPosition, PDType0Font font) throws JAXBException, IOException {
        PDRectangle pageSize = null;

        //Unmarshall and process Alto
        if(altoId != null) {
            JAXBContext jaxbContext;
            jaxbContext = JAXBContext.newInstance(Alto.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Alto customer = (Alto) jaxbUnmarshaller.unmarshal(new URL(store.createUrl(altoId)).openStream());
            List<Alto.Layout.Page> pageList = customer.getLayout().getPage();
            if (pageList.size() != 1) {
                throw new RuntimeException("Alto has more or less than 1 page element.");
            }
            Alto.Layout.Page altoPage = pageList.get(0);
            pageSize = new PDRectangle(altoPage.getPrintSpace().getWIDTH(), altoPage.getPrintSpace().getHEIGHT());

            PDPage pdfPage = new PDPage(pageSize);
            doc.addPage(pdfPage);
            PDPageContentStream contentStream = new PDPageContentStream(doc, pdfPage, true, true);

            for (BlockType blockType : altoPage.getPrintSpace().getTextBlockOrIllustrationOrGraphicalElement()) {
                processBlock(blockType, contentStream, pageSize, font);
            }

            contentStream.close();
        }

        File tmpImage = null;
        try {
            //Load image from fedora and save it to temporary file
            tmpImage = new File(tmpFolder + imageId + ".jpg");
            InputStream imageInputStream = new URL(store.createUrl(imageId)).openStream();
            FileOutputStream fileOutputStream = new FileOutputStream(tmpImage);
            ByteStreams.copy(imageInputStream, fileOutputStream);
            fileOutputStream.close();

            //If image is too big, rescale it
            SimpleImageInfo imageInfo = new SimpleImageInfo(tmpImage);
            int height = imageInfo.getHeight();
            int width = imageInfo.getWidth();
            if(height > 1500 || width > 1500) {
                File resizedImage = new File(tmpFolder + imageId + "_resized.jpg");
                //Crop the source image
                ConvertCmd cmd = new ConvertTempCmd(tmpFolder);
                IMOperation op = new IMOperation();
                op.addImage(tmpImage.getAbsolutePath());
                op.resize(1500, 1500);
                op.addImage(resizedImage.getAbsolutePath());
                try {
                    cmd.run(op);
                } catch (InterruptedException | IM4JavaException e) {
                    throw new IOException(e);
                }
                tmpImage.delete();
                tmpImage = resizedImage;
            }

            //Insert image to PDF
            PDImageXObject imageXObject = PDImageXObject.createFromFile(tmpImage, doc);
            if(pageSize == null) {
                pageSize = new PDRectangle(imageXObject.getWidth(), imageXObject.getHeight());
                PDPage pdfPage = new PDPage(pageSize);
                doc.addPage(pdfPage);
            }
            imgToPDF(imageXObject, doc, 0, 0, pageSize.getWidth(), pageSize.getHeight());
        } finally {
            if(tmpImage != null) {
                tmpImage.delete();
            }
        }
        //Insert watermark
        if(watermark != null) {
            PDImageXObject imageXObject = PDImageXObject.createFromFile(watermark, doc);
            if(watermarkPosition == null) {
                watermarkPosition = "cc";
            }

            //Size should be half of page size, but maintaining aspect ratio of the watermark
            int imgWidth = imageXObject.getWidth();
            int imgHeight = imageXObject.getHeight();
            float aspectRatio = (float) imgWidth / (float) imgHeight;
            float width;
            float height;
            if(imgWidth > imgHeight) {
                width = pageSize.getWidth() / 2;
                height = width / aspectRatio;
            }
            else {
                height = pageSize.getHeight() / 2;
                width = height * aspectRatio;
            }
            float x;
            float y;
            switch(watermarkPosition) {
                case "tl":
                    x = 0;
                    y = 0;
                    break;
                case "tr":
                    x = pageSize.getWidth() - width;
                    y = 0;
                    break;
                case "br":
                    x = pageSize.getWidth() - width;
                    y = pageSize.getHeight() - height;
                    break;
                case "bl":
                    x = 0;
                    y = pageSize.getHeight() - height;
                    break;
                case "cc":
                default:
                    x = (pageSize.getWidth() - width) / 2;
                    y = (pageSize.getHeight() - height) / 2;
                    break;
            }
            imgToPDF(imageXObject, doc, x, y, width, height);
        }
    }

    private void processBlock(BlockType blockType, PDPageContentStream contentStream, PDRectangle pageSize, PDType0Font font) throws IOException {
        if(blockType instanceof ComposedBlockType) {
            for(BlockType newBlockType : ((ComposedBlockType) blockType).getTextBlockOrIllustrationOrGraphicalElement()){
                processBlock(newBlockType, contentStream, pageSize, font);
            }
        }
        else if (blockType instanceof TextBlockType) {
            for(TextBlockType.TextLine textLine : ((TextBlockType) blockType).getTextLine()){
                for(Object object : textLine.getStringAndSP()){
                    if(object instanceof StringType) {
                        StringType stringType = (StringType) object;
                        float y = pageSize.getHeight() - stringType.getVPOS() - stringType.getHEIGHT();
                        float height = (float) (stringType.getWIDTH() / stringType.getCONTENT().length() * 1.7);
                        textToPDF(stringType.getCONTENT(), stringType.getHPOS(), y, height, contentStream, font);
                    }
                }
            }
        }
    }

    private void textToPDF(String word, float x, float y, float height, PDPageContentStream contentStream, PDType0Font font) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, height);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(word);
        contentStream.endText();
    }

    private void imgToPDF(PDImageXObject imageXObject, PDDocument doc, float x, float y, float imgWidth, float imgHeight) throws IOException {
        PDPage page = doc.getPage(doc.getNumberOfPages() - 1);
        PDPageContentStream contentStream = new PDPageContentStream(doc, page,true,true);
        contentStream.drawImage(imageXObject,
                x,
                y,
                imgWidth,
                imgHeight);
        contentStream.close();
    }
}
