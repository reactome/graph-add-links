package org.reactome.otheridentifiers.retrievers;

import org.reactome.DownloadInfo;
import org.reactome.resource.Retriever;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.reactome.otheridentifiers.EnsEMBLBioMartUtils.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLBioMartUniProtRetriever implements Retriever {
    private DownloadInfo downloadInfo;

    public EnsEMBLBioMartUniProtRetriever(String resourceName) {
        this.downloadInfo = new DownloadInfo(resourceName);
    }

    @Override
    public void downloadFile(DownloadInfo.Downloadable downloadable) throws IOException {
        for (String uniProtSearchTerm : downloadable.getSearchTerms()) {
            List<String> bioMartResponse = queryBioMart(bioMartSpeciesName, uniProtSearchTerm);
            saveFile(getFilePath(downloadable, bioMartSpeciesName), bioMartResponse);
        }
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }

    private Path getFilePath(DownloadInfo.Downloadable downloadable, String bioMartSpeciesName) {
        return getOutputDirectory().resolve(downloadable.getLocalFileName().replace("\\w+", bioMartSpeciesName));
    }

}
