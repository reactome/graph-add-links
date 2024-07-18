package org.reactome.resource;

import org.reactome.DownloadInfo;
import org.reactome.utils.ConfigParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/9/2024
 */
public abstract class SingleRetriever {
    private DownloadInfo.Downloadable downloadable;

    public SingleRetriever(DownloadInfo.Downloadable downloadable) {
        this.downloadable = downloadable;
    }

    public void downloadFileIfNeeded() throws IOException {
        if (shouldDownloadFile()) {
            downloadFile();
        }
    }

    public abstract void downloadFile() throws IOException;

    protected boolean isFileOld() {
        return isFileMoreThan2DaysOld();
    }

    protected boolean fileExists() {
        return Files.exists(ConfigParser.getDownloadDirectoryPath().resolve(downloadable.getLocalFileName()));
    }

    protected boolean isFileZeroSize() {
        File file = new File(ConfigParser.getDownloadDirectoryPath() + "/", downloadable.getLocalFileName());

        return file.length() == 0;
    }

    protected boolean shouldDownloadFile() {
        return !fileExists() || isFileZeroSize() || isFileOld();
    }


//    protected Path getLocalFilePath() {
//        return ConfigParser.getDownloadDirectoryPath().resolve(getDownloadable().getLocalFileName());
//    }

    protected DownloadInfo.Downloadable getDownloadable() {
        return this.downloadable;
    }

    private boolean isFileMoreThan2DaysOld() {
        File file = new File(ConfigParser.getDownloadDirectoryPath() + "/" + getDownloadable().getLocalFileName());

        Instant lastModifiedTime = Instant.ofEpochMilli(file.lastModified());
        Instant currentTime = Instant.now();

        Duration fileAge = Duration.between(lastModifiedTime, currentTime);

        return fileAge.toDays() > 2;
    }
}
