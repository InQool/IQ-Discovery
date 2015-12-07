///*
// * MessageListener.java
// *
// * Copyright (c) 2014  inQool a.s.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package com.inqool.dcap.ip;
//
//import com.hp.hpl.jena.rdf.model.ResourceFactory;
//import com.inqool.dcap.config.Zdo;
//import com.inqool.dcap.integration.Constants;
//import com.inqool.dcap.integration.model.ZdoModel;
//import com.inqool.dcap.integration.model.ZdoTerms;
//import com.inqool.dcap.integration.service.DataStore;
//import com.inqool.dcap.ip.tile.ImageTiler;
//import com.inqool.dcap.ip.tile.TiledImage;
//import com.inqool.dcap.ip.write.ImageWriter;
//import org.apache.deltaspike.core.api.config.ConfigProperty;
//import org.slf4j.Logger;
//
//import javax.ejb.ActivationConfigProperty;
//import javax.ejb.MessageDriven;
//import javax.inject.Inject;
//import javax.jms.JMSException;
//import javax.jms.Message;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//
//@SuppressWarnings("unused")
//@MessageDriven(name = "IpMDB", activationConfig = {
//        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
//        @ActivationConfigProperty(propertyName = "destination", propertyValue = "ip")})
//public class MessageListener implements javax.jms.MessageListener {
//    @Inject
//    @ConfigProperty(name = "input.folder")
//    private String inputFolder;
//
//    @Inject
//    @ConfigProperty(name = "output.folder")
//    private String outputFolder;
//
//    @Inject
//    private DataStore dataStore;
//
//    @Inject
//    @Zdo
//    private Logger logger;
//
//    @Inject
//    private ImageTiler tiler;
//
//    @Inject
//    private ImageWriter imageWriter;
//
//    public void onMessage(Message message) {
//        try {
//            logger.debug("Received message: {}", message.getJMSMessageID());
//        } catch (final JMSException e) {
//            logger.error("Received unintelligible message: {}", e);
//            return;
//        }
//
//        try {
//            String id = message.getStringProperty(Constants.IP_SRC_ID_HEADER_NAME);
//            String url = dataStore.createUrl(id);
//
//            if (url == null || outputFolder == null) {
//                logger.error("Missing source url or output folder.");
//                return;
//            }
//
//            ZdoModel metadata = dataStore.get(url + "/fcr:metadata");
//
//            if (metadata == null) {
//                throw new IOException("Image metadata does not exists.");
//            }
//
//            ImageType type;
//            switch (metadata.getProperty(ResourceFactory.createProperty(url), ZdoTerms.mimeType).getString()) {
//                case "image/jpeg":
//                    type = ImageType.JPEG;
//                    break;
//                case "image/jp2":
//                    type = ImageType.JPEG2000;
//                    break;
//                default:
//                    throw new IOException("Unsupported image format '{"+url+"}' detected.");
//            }
//
//            try (InputStream in = new URL(url).openStream()) {
//                TiledImage image = tiler.tileImage(in, type);
//                imageWriter.output(image, new File(outputFolder, dataStore.createDeepPath(id)));
//            }
//
//        } catch (final JMSException | IOException e) {
//            logger.error("Error processing JMS event!", e);
//        }
//
//    }
//}
