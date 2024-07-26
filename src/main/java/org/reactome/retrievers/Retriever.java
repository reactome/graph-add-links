package org.reactome.retrievers;

import org.reactome.DownloadInfo;

import java.io.IOException;

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
