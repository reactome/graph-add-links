package org.reactome.resource;

import org.reactome.DownloadInfo;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/9/2024
 */
public class SingleFileBasicFileRetriever implements Retriever {
    private DownloadInfo downloadInfo;

    private BasicFileRetriever basicFileRetriever;

    public SingleFileBasicFileRetriever(String resourceName) {
        this.downloadInfo = new DownloadInfo(resourceName);
        this.basicFileRetriever = new BasicFileRetriever(getDownloadInfo().getDownloadables().get(0));
    }

    @Override
    public void downloadFiles() throws IOException {
        this.basicFileRetriever.downloadFile();
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }
}
