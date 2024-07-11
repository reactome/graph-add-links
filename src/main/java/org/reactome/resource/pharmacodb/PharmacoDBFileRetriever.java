package org.reactome.resource.pharmacodb;


import org.reactome.DownloadInfo;
import org.reactome.resource.BasicFileRetriever;
import org.reactome.resource.Retriever;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class PharmacoDBFileRetriever implements Retriever {
    private DownloadInfo downloadInfo;

    private List<BasicFileRetriever> basicFileRetrievers;

    public PharmacoDBFileRetriever() {
        this.downloadInfo = new DownloadInfo("PharmacoDB");

        this.basicFileRetrievers = new ArrayList<>();
        for (DownloadInfo.Downloadable downloadable : getDownloadInfo().getDownloadables()) {
            this.basicFileRetrievers.add(new BasicFileRetriever(downloadable));
        }
    }

    @Override
    public void downloadFiles() throws IOException {
        for (BasicFileRetriever basicFileRetriever : this.basicFileRetrievers) {
            basicFileRetriever.downloadFile();
        }
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }
}
