package com.inqool.dcap.integration.desa2.preprocess;

import com.inqool.dcap.config.Zdo;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lukess on 7. 11. 2014.
 */
@ApplicationScoped
public class KDRDataPreprocessor {

    private static final String[] masterCopyFolderNames = {
            "masterCopy",
            "PS"
    };

    @Inject
    @Zdo
    private Logger logger;

    public void preprocessData(String inputDirPath, String outputDirPath) {
        //Check arguments
        if(!Files.exists(new File(outputDirPath).toPath())) {
            throw new IllegalArgumentException("Output path doesn't exist.");
        }
        if(!outputDirPath.endsWith("/")) {
            outputDirPath += "/";
        }
        File inputDir = new File(inputDirPath);
        if(!Files.exists(inputDir.toPath())) {
            throw new IllegalArgumentException("Input path doesn't exist.");
        }
        File[] directoryListing = inputDir.listFiles();
        if (directoryListing == null || directoryListing.length == 0) {
            logger.info("No new zip files to parse.");
            return;
        }
        //Process all found files
        for (File child : directoryListing) {
            if(!removeMasterCopiesFromZip(child.toURI())) {
                logger.error("Could not remove master copies from zip.");
                return;
            }
            //Move modified zip to output folder
            String filename = child.getName();
            File output = new File(outputDirPath+filename);
            if(!child.renameTo(output)) {
                logger.error("Could not move zip package.");
                return;
            }
            //Persist info about the package
            Entry entry = new Entry();
            String inventoryNumber = filename.substring(0, filename.lastIndexOf('.'));  //strip .zip
            entry.setInventoryNumber(inventoryNumber);
            entry.setPath(outputDirPath+filename);
            //persist somewhere?
        }
    }

    private boolean removeMasterCopiesFromZip(URI uri) {
        Map<String, String> zip_properties = new HashMap<>();
        zip_properties.put("create", "false");
        zip_properties.put("encoding", "UTF-8");
        URI fixedURI;
        try {
            fixedURI = new URI("jar:" + uri.toString());
        } catch(URISyntaxException e) {
            logger.error("This should never happen.", e);
            return false;
        }
        try (FileSystem zipfs = FileSystems.newFileSystem(fixedURI, zip_properties)) {
            Path masterCopyPath = findMasterCopyFolder(zipfs);
            //Did we find it anywhere?
            if(masterCopyPath == null) {
                logger.error("Master copy directory not found in zip.");
                return false;
            }
            //Delete all files inside
            DirectoryStream<Path> stream = Files.newDirectoryStream(masterCopyPath);
            for (Path masterCopyFile : stream) {
                Files.delete(masterCopyFile);
                logger.debug("Successfully deleted " + masterCopyFile.toUri());
            }
            //Delete also the PS folder itself
            Files.delete(masterCopyPath);
            logger.debug("Successfully deleted " + masterCopyPath.toUri());
        } catch (IOException e) {
            logger.error("Error during deleting master copies from zip.", e);
            return false;
        }
        return true;
    }

    private Path findMasterCopyFolder(FileSystem zipfs) {
        //Is master copy directory in root of zip?
        Path masterCopyPath;
        for(String possibleName : masterCopyFolderNames) {
            masterCopyPath = zipfs.getPath(possibleName);
            if(Files.isDirectory(masterCopyPath)) {
                return masterCopyPath;
            }
        }
        //Search subdirectories for PS dir
        try{
            for(Path subdir : Files.newDirectoryStream(zipfs.getPath("/"))) {
                for(String possibleName : masterCopyFolderNames) {
                    masterCopyPath = zipfs.getPath(subdir+possibleName);
                    if(Files.isDirectory(masterCopyPath)) {
                        return masterCopyPath;
                    }
                }
            }
        } catch(IOException e) {
            logger.error("This should never happen.", e);
            return null;
        }
        return null;
    }
}
