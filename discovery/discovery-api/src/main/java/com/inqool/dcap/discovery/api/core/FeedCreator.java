package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.common.entity.FeedEntry;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 9. 9. 2015.
 */
@RequestScoped
public class FeedCreator {

    @Inject
    @ConfigProperty(name = "feed.folder")
    private String FEED_FOLDER;

    @Inject
    @ConfigProperty(name = "discovery.web.endpoint")
    private String DISCOVERY_WEB_ENDPOINT;

    public void generateFeeds(List<FeedEntry> feedEntries) throws IOException, FeedException {
        if(feedEntries.isEmpty()) {
            return;
        }

        //Make sure output folder exists
        File feedFolder = new File(FEED_FOLDER);
        if(!feedFolder.exists()) {
            feedFolder.mkdirs();
        }

        //If newest news are older than feed file, nothing has changed
        File atomFile = new File(feedFolder, "atom.xml");
        LocalDateTime lastModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(atomFile.lastModified()), ZoneOffset.UTC);
        if(feedEntries.get(0).getCreated().isBefore(lastModified)) {
            return;
        }

        //Atom feed
        SyndFeed atomFeed = new SyndFeedImpl();
        atomFeed.setFeedType("atom_1.0");
        atomFeed.setTitle("eBadatelna Atom feed");
        atomFeed.setLink(DISCOVERY_WEB_ENDPOINT + "/");
        atomFeed.setDescription("This feed has been created using ROME (Java syndication utilities and description should be filled in here.");

        //Rss feed
        SyndFeed rssFeed = new SyndFeedImpl();
        rssFeed.setFeedType("rss_2.0");
        rssFeed.setTitle("eBadatelna RSS feed");
        rssFeed.setLink(DISCOVERY_WEB_ENDPOINT + "/");
        rssFeed.setDescription("This feed has been created using ROME (Java syndication utilities and description should be filled in here.");

        //Create entries
        List<SyndEntry> entries = new ArrayList<>();
        for (FeedEntry feedEntry : feedEntries) {
            SyndEntry entry = new SyndEntryImpl();
            entry.setTitle(feedEntry.getTitle());
            entry.setLink(feedEntry.getLink());
            entry.setPublishedDate(Date.from(feedEntry.getCreated().atZone(ZoneId.systemDefault()).toInstant()));
            SyndContent description = new SyndContentImpl();
            description.setType("text/plain");
            description.setValue(feedEntry.getDescription());
            entry.setDescription(description);
            entries.add(entry);
        }
        atomFeed.setEntries(entries);
        rssFeed.setEntries(entries);

        //Write feeds to files
        Writer atomFileWriter = new FileWriter(atomFile);
        Writer rssFileWriter = new FileWriter(FEED_FOLDER + "rss.xml");
        SyndFeedOutput output = new SyndFeedOutput();
        output.output(atomFeed, atomFileWriter);
        output.output(rssFeed, rssFileWriter);
    }
}
