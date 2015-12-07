/*
 * OpenJPEGLoader.java
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

package com.inqool.dcap.integration.desa2.loader.thumbnailer;

import com.google.common.io.ByteStreams;
import com.inqool.dcap.config.Zdo;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A class that uses OpenJPEG to load jp2 files
 * This does it the easiest way; by using binaries.  Not JNI (yet) - waiting for
 * library to mature
 * @author wpalmer
 * @author Matus Zamborsky (inQool)
 *
 */
@ApplicationScoped
public class OpenJPEGLoader {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "thumbnail.tmp")
    private String tempFolder;

    @Inject
    @ConfigProperty(name = "openjpeg.executable")
    private String executable;

    public File convertJPEG2000ToTIF(InputStream in) throws IOException {
        File srcFile = new File(tempFolder, UUID.randomUUID().toString() + ".jp2");
        File dstFile = new File(tempFolder, UUID.randomUUID().toString() + ".tif");

        try(OutputStream srcStream = new FileOutputStream(srcFile)) {
            // store to temp
            ByteStreams.copy(in, srcStream);

            // prepare command
            List<String> commandLine = Arrays.asList(
                    executable,
                    "-i",
                    srcFile.getAbsolutePath(),
                    "-o",
                    dstFile.getAbsolutePath()
            );

            /*logger.debug("running: " + commandLine.toString());*/
            ToolRunner runner = new ToolRunner();
            int exitCode = runner.runCommand(commandLine, tempFolder);
            /*logger.debug("exit code: " + exitCode);*/

            if (exitCode != 0) {
                //some error
                BufferedReader log = runner.getStdout();
                while (log.ready()) {
                    logger.error("log: " + log.readLine());
                }

                throw new IOException("Cant load image.");
            } else {
                return dstFile;
            }
        } finally {
            srcFile.delete();
        }
    }

    /**
     * Method to load a JP2 file using OpenJPEG executable
     * @param in jp2 stream to load
     * @return decoded buffered image from file
     */
    public BufferedImage loadJP2(InputStream in) throws IOException {
        File srcFile = new File(tempFolder, UUID.randomUUID().toString() + ".jp2");
        File dstFile = new File(tempFolder, UUID.randomUUID().toString() + ".tif");

        try(OutputStream srcStream = new FileOutputStream(srcFile)) {
            // store to temp
            ByteStreams.copy(in, srcStream);

            // prepare command
            List<String> commandLine = Arrays.asList(
                    executable,
                    "-i",
                    srcFile.getAbsolutePath(),
                    "-o",
                    dstFile.getAbsolutePath()
            );

            logger.debug("running: " + commandLine.toString());
            ToolRunner runner = new ToolRunner();
            int exitCode = runner.runCommand(commandLine, tempFolder);
            logger.debug("exit code: " + exitCode);

            if (exitCode != 0) {
                //some error
                BufferedReader log = runner.getStdout();
                while (log.ready()) {
                    logger.error("log: " + log.readLine());
                }

                throw new IOException("Cant load image.");
            } else {
                return ImageIO.read(dstFile);
            }
        } finally {
            srcFile.delete();
            dstFile.delete();
        }
    }
}
