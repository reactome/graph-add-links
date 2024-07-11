package org.reactome.resource.ctdgene;

import org.reactome.DownloadInfo;
import org.reactome.resource.BasicFileRetriever;
import org.reactome.resource.Retriever;
import org.reactome.resource.UniProtMapperRetriever;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class CTDGeneFileRetriever implements Retriever {
    private DownloadInfo downloadInfo;

    private UniProtMapperRetriever uniProtMapperRetriever;
    private BasicFileRetriever basicFileRetriever;

    public CTDGeneFileRetriever() {
        this.downloadInfo = new DownloadInfo("CTDGene");
        this.uniProtMapperRetriever = new UniProtMapperRetriever(this.downloadInfo.getDownloadables().get(0));
        this.basicFileRetriever = new BasicFileRetriever(this.downloadInfo.getDownloadables().get(1));
    }

    @Override
    public void downloadFiles() throws IOException {
        this.uniProtMapperRetriever.downloadFile();
        this.basicFileRetriever.downloadFile();
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }
}
