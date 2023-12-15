package org.reactome.resource;

import org.reactome.DownloadInfo;
import org.reactome.utils.ConfigParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public interface Retriever {
    default void downloadFiles() throws IOException {
        for (DownloadInfo.Downloadable downloadable : getDownloadInfo().getDownloadables()) {
            downloadFile(downloadable);
        }
    };

    void downloadFile(DownloadInfo.Downloadable downloadable) throws IOException;

    List<Path> getLocalFilePaths();

    default Path getLocalFilePath(DownloadInfo.Downloadable downloadable) {
        return ConfigParser.getDownloadDirectoryPath().resolve(downloadable.getLocalFileName());
    }

    DownloadInfo getDownloadInfo();
}
