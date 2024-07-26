package org.reactome.resource.otheridentifiers.helpers;

import org.reactome.DownloadInfo;
import org.reactome.retrievers.Retriever;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/11/2024
 */
public class MultipleFileEnsEMBLBioMartOtherIdentifierRetriever implements Retriever {
    private DownloadInfo downloadInfo;

    private List<EnsEMBLBioMartOtherIdentifiersRetriever> ensEMBLBioMartOtherIdentifiersRetrievers;

    public MultipleFileEnsEMBLBioMartOtherIdentifierRetriever(String resourceName) {
        this.downloadInfo = new DownloadInfo(resourceName);
        this.ensEMBLBioMartOtherIdentifiersRetrievers = new ArrayList<>();
        for (DownloadInfo.Downloadable downloadable : getDownloadInfo().getDownloadables()) {
            if (downloadable.getLocalFileName().contains("microarray_go_ncbi_ids")) {
                this.ensEMBLBioMartOtherIdentifiersRetrievers.add(
                    new EnsEMBLBioMartOtherIdentifiersRetriever(downloadable));
            }
        }
    }

    @Override
    public void downloadFiles() throws IOException {
        for (EnsEMBLBioMartOtherIdentifiersRetriever ensEMBLBioMartOtherIdentifiersRetriever :
            this.ensEMBLBioMartOtherIdentifiersRetrievers) {

            ensEMBLBioMartOtherIdentifiersRetriever.downloadFile();
        }
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }
}
