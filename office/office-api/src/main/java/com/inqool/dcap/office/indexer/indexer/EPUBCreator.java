package com.inqool.dcap.office.indexer.indexer;

import com.inqool.dcap.integration.model.ZdoFileType;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.service.DataStore;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import org.apache.commons.io.IOUtils;
import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by John on 8.7.2015.
 *
 * This code was created by Kapurka and every bit of sanity found here was added by Lukess.
 */
@Stateless
public class EPUBCreator {

    @Inject
    @ConfigProperty(name = "tmp.folder")
    private String tmpFolder;

    @Inject
    private DataStore store;

    public EPUBCreator() {
    }

    @Asynchronous
    public void createBook(String epubId, String title, String author, List<String> txtIds){
        List<File> tmpFiles = new ArrayList<>();
        try {
            Book book = new Book();

            //Add metadata
            Metadata metadata = book.getMetadata();
            metadata.addTitle(title);
            if (author != null) {
                String[] splited = author.split(" ", 2);
                if (splited.length < 2) {
                    metadata.addAuthor(new Author("", splited[0]));
                } else {
                    metadata.addAuthor(new Author(splited[0], splited[1]));
                }
            }

            int pageCounter = 0;
            for (String id : txtIds) {
                //Load text from Fedora
                InputStream in = new URL(store.createUrl(id)).openStream();
                StringWriter writer = new StringWriter();
                IOUtils.copy(in, writer, "utf-8");
                String text = writer.toString();

                //Convert text string to temporary html file
                File tmpHtml = new File(tmpFolder + UUID.randomUUID() + ".html");
                tmpFiles.add(tmpHtml);
                toHtml(text, tmpHtml);

                //Add page
                pageCounter++;
                book.addSection("Page " + String.valueOf(pageCounter), new Resource(new FileInputStream(tmpHtml),
                        tmpHtml.getName()));
            }

            File tmpFile = null;
            try {
                //Save epub to temporary file
                tmpFile = new File(tmpFolder + epubId + ".epub");
                EpubWriter epubWriter = new EpubWriter();
                FileOutputStream fos = new FileOutputStream(tmpFile);
                epubWriter.write(book, fos);
                fos.close();

                //Upload epub to fedora
                FileInputStream fis = new FileInputStream(tmpFile);
                ZdoModel zdoModel = new ZdoModel(store.createUrl(epubId), fis);
                zdoModel.add(ZdoTerms.mimeType, "application/epub+zip");
                zdoModel.add(ZdoTerms.fileType, ZdoFileType.epub.name());
                store.update(zdoModel);
                fis.close(); //?
            } finally {
                if (tmpFile != null) {
                    tmpFile.delete();   //fixme still does not delete
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Epub could not be created", e);
            //fixme
        } finally {
            if(tmpFiles != null) {
                for(File f : tmpFiles) {
                    if(f != null) {
                        f.delete();
                    }
                }
            }
        }
    }

    private void toHtml(String txt, File tmpHtml) throws IOException {
        /*String txt = new String(Files.readAllBytes(Paths.get(path)));*/

        StringBuilder builder = new StringBuilder();
        boolean previousWasASpace = false;
        for( char c : txt.toCharArray() ) {
            if( c == ' ' ) {
                if( previousWasASpace ) {
                    builder.append("&nbsp;");
                    previousWasASpace = false;
                    continue;
                }
                previousWasASpace = true;
            } else {
                previousWasASpace = false;
            }
            switch(c) {
                case '<': builder.append("&lt;"); break;
                case '>': builder.append("&gt;"); break;
                case '&': builder.append("&amp;"); break;
                case '"': builder.append("&quot;"); break;
                case '\r': break;
                case '\n': builder.append("<br>"); break;
                case '\t': builder.append("&nbsp; &nbsp; &nbsp;"); break;
                default:
                    if( c < 128 ) {
                        builder.append(c);
                    } else {
                        builder.append("&#").append((int)c).append(";");
                    }
            }
        }

        BufferedWriter out = new BufferedWriter(new FileWriter(tmpHtml));
        out.write(builder.toString());
        out.close();
    }

//    The original
//
//    public void prepareNewBook(String title, String author){
//        Book book = new Book();
//        Metadata metadata = book.getMetadata();
//
//        String[] splited = author.split(" ", 2);
//
//        metadata.addTitle(title);
//        metadata.addAuthor(new Author(splited[0], splited[1]));
//
//        setBook(book);
//
//    }
//
//    public void prepareOldBook(String path){
//        Book book = null;
//        File file = new File(path);
//        InputStream epubInputStream;
//
//        try {
//            epubInputStream = new FileInputStream(file);
//            book = (new EpubReader()).readEpub(epubInputStream);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        setPageCounter(book.getContents().size());
//        setBook(book);
//    }
//
//    public void addPage(String path) throws IOException {
//        setPageCounter(getPageCounter() + 1);
//        getBook().addSection("Page " + getPageCounter().toString(), new Resource(toHtml(path),
//                path.substring(path.lastIndexOf(File.separator) + 1, path.length() - 4) + ".html"));
//    }
//
//    public void addPages(List<String> list) throws IOException {
//        for (String path : list){
//            addPage(path);
//        }
//    }
//
//    public InputStream toHtml(String path) throws IOException {
//        String txt = new String(Files.readAllBytes(Paths.get(path)));
//
//        StringBuilder builder = new StringBuilder();
//        boolean previousWasASpace = false;
//        for( char c : txt.toCharArray() ) {
//            if( c == ' ' ) {
//                if( previousWasASpace ) {
//                    builder.append("&nbsp;");
//                    previousWasASpace = false;
//                    continue;
//                }
//                previousWasASpace = true;
//            } else {
//                previousWasASpace = false;
//            }
//            switch(c) {
//                case '<': builder.append("&lt;"); break;
//                case '>': builder.append("&gt;"); break;
//                case '&': builder.append("&amp;"); break;
//                case '"': builder.append("&quot;"); break;
//                case '\r': break;
//                case '\n': builder.append("<br>"); break;
//                case '\t': builder.append("&nbsp; &nbsp; &nbsp;"); break;
//                default:
//                    if( c < 128 ) {
//                        builder.append(c);
//                    } else {
//                        builder.append("&#").append((int)c).append(";");
//                    }
//            }
//        }
//
//        File temp = new File("JustForThisMoment.html");
//
//        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
//        out.write(builder.toString());
//        out.close();
//
//        InputStream is = new FileInputStream(temp);
//        temp.delete();
//
//        return is;
//    }
//
//    public void createEpub(String path) throws IOException {
//        EpubWriter epubWriter = new EpubWriter();
//        epubWriter.write(getBook(), new FileOutputStream(path));
//
//    }
//
//    public Book getBook() {
//        return book;
//    }
//
//    public void setBook(Book book) {
//        this.book = book;
//    }
//
//    public Integer getPageCounter() {
//        return pageCounter;
//    }
//
//    public void setPageCounter(Integer pageCounter) {
//        this.pageCounter = pageCounter;
//    }
}
