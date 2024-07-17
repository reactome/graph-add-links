package org.reactome.otheridentifiers.retrievers;

import org.reactome.DownloadInfo;
import org.reactome.resource.SingleRetriever;

import java.io.IOException;
import java.util.List;

import static org.reactome.otheridentifiers.EnsEMBLBioMartUtils.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLBioMartUniProtRetriever extends SingleRetriever {

    public EnsEMBLBioMartUniProtRetriever(DownloadInfo.Downloadable downloadable) {
        super(downloadable);
    }

    @Override
    public void downloadFile() throws IOException {
        for (String uniProtSearchTerm : getDownloadable().getSearchTerms()) {
            List<String> bioMartResponse = queryBioMart(getDownloadable().getSpecies(), uniProtSearchTerm);
            saveFile(getDownloadable().getLocalFilePath(), bioMartResponse);
        }
    }
}
