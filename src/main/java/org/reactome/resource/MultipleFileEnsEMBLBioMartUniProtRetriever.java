package org.reactome.resource;

import org.reactome.DownloadInfo;
import org.reactome.otheridentifiers.retrievers.EnsEMBLBioMartUniProtRetriever;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.reactome.otheridentifiers.EnsEMBLBioMartUtils.getBioMartSpeciesNames;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/9/2024
 */
public class MultipleFileEnsEMBLBioMartUniProtRetriever implements Retriever {
    private DownloadInfo downloadInfo;

    private List<EnsEMBLBioMartUniProtRetriever> ensEMBLBioMartUniProtRetrievers;

    public MultipleFileEnsEMBLBioMartUniProtRetriever(String resourceName) {
        this.downloadInfo = new DownloadInfo(resourceName);
        this.ensEMBLBioMartUniProtRetrievers = new ArrayList<>();
        for (String bioMartSpeciesName : getBioMartSpeciesNames()) {
            this.ensEMBLBioMartUniProtRetrievers.add(
                new EnsEMBLBioMartUniProtRetriever(bioMartSpeciesName, getDownloadInfo().getDownloadables().get(0)));
        }
    }

    @Override
    public void downloadFiles() throws IOException {
        for (EnsEMBLBioMartUniProtRetriever ensEMBLBioMartUniProtRetriever : this.ensEMBLBioMartUniProtRetrievers) {
            ensEMBLBioMartUniProtRetriever.downloadFile();
        }
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }
}
