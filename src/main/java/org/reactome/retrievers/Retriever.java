package org.reactome.retrievers;

import org.reactome.DownloadInfo;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public interface Retriever {
    void downloadFiles() throws IOException;

    DownloadInfo getDownloadInfo();
}
