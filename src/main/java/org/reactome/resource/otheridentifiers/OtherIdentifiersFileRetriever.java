package org.reactome.resource.otheridentifiers;

import org.reactome.DownloadInfo;
import org.reactome.resource.otheridentifiers.helpers.MultipleFileEnsEMBLBioMartOtherIdentifierRetriever;
import org.reactome.resource.MultipleFileEnsEMBLBioMartUniProtRetriever;
import org.reactome.resource.Retriever;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/11/2024
 */
public class OtherIdentifiersFileRetriever implements Retriever {
    private DownloadInfo downloadInfo;

    private MultipleFileEnsEMBLBioMartUniProtRetriever multipleFileEnsEMBLBioMartUniProtRetriever;
    private MultipleFileEnsEMBLBioMartOtherIdentifierRetriever multipleFileEnsEMBLBioMartOtherIdentifierRetriever;

    public OtherIdentifiersFileRetriever() {
        final String resourceName = "OtherIdentifiers";
        this.downloadInfo = new DownloadInfo(resourceName);
        this.multipleFileEnsEMBLBioMartUniProtRetriever =
            new MultipleFileEnsEMBLBioMartUniProtRetriever(resourceName);
        this.multipleFileEnsEMBLBioMartOtherIdentifierRetriever =
            new MultipleFileEnsEMBLBioMartOtherIdentifierRetriever(resourceName);
    }

    @Override
    public void downloadFiles() throws IOException {
        this.multipleFileEnsEMBLBioMartUniProtRetriever.downloadFiles();
        this.multipleFileEnsEMBLBioMartOtherIdentifierRetriever.downloadFiles();
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }
}
