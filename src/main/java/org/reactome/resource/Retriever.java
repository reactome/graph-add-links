package org.reactome.resource;

import org.reactome.DownloadInfo;
import org.reactome.utils.ConfigParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public interface Retriever {
    default void downloadFiles() throws IOException {
        for (DownloadInfo.Downloadable downloadable : getDownloadInfo().getDownloadables()) {
            if (shouldDownloadFile(downloadable)) {
                downloadFile(downloadable);
            }
        }
    };

    default boolean fileIsOld(DownloadInfo.Downloadable downloadable) {
        File file = new File(ConfigParser.getDownloadDirectoryPath() + "/" + downloadable.getLocalFileName());

        Instant lastModifiedInstant = Instant.ofEpochMilli(file.lastModified());
        Instant currentTime = Instant.now();

        Duration fileAge = Duration.between(lastModifiedInstant, currentTime);

        return fileAge.toDays() > 2;
    }

    default boolean fileExists(DownloadInfo.Downloadable downloadable) {
        return Files.exists(ConfigParser.getDownloadDirectoryPath().resolve(downloadable.getLocalFileName()));
    }

    default boolean fileIsZeroSize(DownloadInfo.Downloadable downloadable) {
        File file = new File(ConfigParser.getDownloadDirectoryPath() + "/", downloadable.getLocalFileName());

        return file.length() == 0;
    }

    default boolean shouldDownloadFile(DownloadInfo.Downloadable downloadable) {
        return !fileExists(downloadable) || fileIsZeroSize(downloadable) || fileIsOld(downloadable);
    }

    void downloadFile(DownloadInfo.Downloadable downloadable) throws IOException;

    default List<Path> getLocalFilePaths() {

        return getDownloadInfo().getDownloadables().stream().map(this::getLocalFilePath).collect(Collectors.toList());
    }

    default Path getLocalFilePath(DownloadInfo.Downloadable downloadable) {
        return ConfigParser.getDownloadDirectoryPath().resolve(downloadable.getLocalFileName());
    }

    //DownloadInfo.Downloadable getDownloadable();
    DownloadInfo getDownloadInfo();
}
