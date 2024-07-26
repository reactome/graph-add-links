package org.reactome.resource.ucsc;

import org.reactome.DownloadInfo;
import org.reactome.retrievers.Retriever;
import org.reactome.retrievers.SelfReferringUniProtRetriever;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class UCSCFileRetriever implements Retriever {
    private DownloadInfo downloadInfo;

    private SelfReferringUniProtRetriever selfReferringUniProtRetriever;

    public UCSCFileRetriever() {
        this.downloadInfo = new DownloadInfo("UCSC");
        this.selfReferringUniProtRetriever = new SelfReferringUniProtRetriever(getDownloadInfo().getDownloadables().get(0));
    }

    @Override
    public void downloadFiles() throws IOException {
        this.selfReferringUniProtRetriever.downloadFileIfNeeded();
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }
}
