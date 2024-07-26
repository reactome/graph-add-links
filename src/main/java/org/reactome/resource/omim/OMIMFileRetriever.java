package org.reactome.resource.omim;

import org.reactome.DownloadInfo;
import org.reactome.retrievers.BasicFileRetriever;
import org.reactome.retrievers.Retriever;
import org.reactome.retrievers.UniProtMapperRetriever;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class OMIMFileRetriever implements Retriever {
    private DownloadInfo downloadInfo;

    private UniProtMapperRetriever uniProtMapperRetriever;
    private BasicFileRetriever basicFileRetriever;

    public OMIMFileRetriever() {
        this.downloadInfo = new DownloadInfo("OMIM");
        this.uniProtMapperRetriever = new UniProtMapperRetriever(this.downloadInfo.getDownloadables().get(0));
        this.basicFileRetriever = new BasicFileRetriever(this.downloadInfo.getDownloadables().get(1));
    }

    @Override
    public void downloadFiles() throws IOException {
        this.uniProtMapperRetriever.downloadFileIfNeeded();
        this.basicFileRetriever.downloadFileIfNeeded();
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }
}
