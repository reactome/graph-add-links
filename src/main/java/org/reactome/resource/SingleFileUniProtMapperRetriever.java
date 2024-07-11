package org.reactome.resource;

import org.reactome.DownloadInfo;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/9/2024
 */
public class SingleFileUniProtMapperRetriever implements Retriever {
    private DownloadInfo downloadInfo;

    private UniProtMapperRetriever uniProtMapperRetriever;

    public SingleFileUniProtMapperRetriever(String resourceName) {
        this.downloadInfo = new DownloadInfo(resourceName);
        this.uniProtMapperRetriever = new UniProtMapperRetriever(getDownloadInfo().getDownloadables().get(0));
    }

    @Override
    public void downloadFiles() throws IOException {
        this.uniProtMapperRetriever.downloadFile();
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }
}
