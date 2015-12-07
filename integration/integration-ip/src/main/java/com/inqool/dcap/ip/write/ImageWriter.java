/*
 * ImageOutput.java
 *
 * Copyright (c) 2014  inQool a.s.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.inqool.dcap.ip.write;

import com.google.common.io.Files;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.exception.ExWrapper;
import com.inqool.dcap.exception.LambdaException;
import com.inqool.dcap.ip.tile.TiledImage;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;

/**
 * @author Matus Zamborsky (inQool)
 */
@ApplicationScoped
public class ImageWriter {
    @Inject
    @Zdo
    private Logger logger;

    public void output(final TiledImage image, final File folder) throws IOException {
        if(!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Failed to create folder for " + folder.getAbsolutePath());
        }

        final File descriptor = new File(folder, "image.dzi");
        final File files = new File(folder, "image_files");

        try {
            logger.debug("Creating descriptor file.");
            JAXBContext context = JAXBContext.newInstance(TiledImage.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(image, descriptor);

            if(!files.isDirectory() && !files.mkdir()) {
                throw new IOException("Failed to create _files folder for " + folder.getAbsolutePath());
            }


            image.getTiles().forEach(ExWrapper.accept(tile -> {
                final File levelFolder = new File(files, String.valueOf(tile.getLevel()));

                if (!levelFolder.isDirectory() && !levelFolder.mkdir()) {
                    throw new IOException("Failed to create _files folder for " + folder.getAbsolutePath());
                }

                final String tileName = tile.getColumn() + "_" + tile.getRow() + "." + image.getFormat();
                final File tileFile = new File(levelFolder, tileName);

                Files.move(tile.getData(), tileFile);
            }));

        } catch (JAXBException | LambdaException e) {
            throw new IOException(e);
        }

    }
}
