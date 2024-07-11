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
//      TODO: Move commented out methods to another interface?
//    default void downloadFiles() throws IOException {
//        for (DownloadInfo.Downloadable downloadable : getDownloadInfo().getDownloadables()) {
//            if (shouldDownloadFile(downloadable)) {
//                downloadFile(downloadable);
//            }
//        }
//    };
//

    void downloadFiles() throws IOException;

    //DownloadInfo.Downloadable getDownloadable();
    DownloadInfo getDownloadInfo();
}
