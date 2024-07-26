package org.reactome.resource.otheridentifiers.helpers;

import org.reactome.DownloadInfo;
import org.reactome.retrievers.SingleRetriever;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.reactome.otheridentifiers.EnsEMBLBioMartUtils.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLBioMartOtherIdentifiersRetriever extends SingleRetriever {

    public EnsEMBLBioMartOtherIdentifiersRetriever(DownloadInfo.Downloadable downloadable) {
        super(downloadable);
    }

    @Override
    public void downloadFile() throws IOException {
        for (String otherIdentifierSearchTerm : getOtherIdentifierSearchTerms()) {
            List<String> bioMartResponse = queryBioMart(getDownloadable().getSpecies(), otherIdentifierSearchTerm);
            saveFile(getFilePath(), bioMartResponse);
        }
    }

    public Path getFilePath() {
        return getOutputDirectory().resolve(getDownloadable().getSpecies() + "_microarray_go_ncbi_ids");
    }

    private List<String> getOtherIdentifierSearchTerms() throws IOException {
        List<String> otherIdentifierSearchTerms = new ArrayList<>();
        otherIdentifierSearchTerms.addAll(getMicroarrayTypesBySpecies());
        otherIdentifierSearchTerms.addAll(getDownloadable().getSearchTerms());
        return otherIdentifierSearchTerms;
    }

    private List<String> getMicroarrayTypesBySpecies() throws IOException {
        return getContentFromURL(getMicroarrayTypesURL());
    }

    private URL getMicroarrayTypesURL() throws MalformedURLException {
        return new URL(getBaseBioMartURL() + getMicroarrayTypesURLParameters());
    }

    private String getMicroarrayTypesURLParameters() {
        return String.join("&",
            "type=listAttributes",
            "mart=ENSEMBL_MART_ENSEMBL",
            "virtualSchema=default",
            "dataset=" + getDownloadable().getSpecies() + "_gene_ensembl",
            "interface=default",
            "attributePage=feature_page",
            "attributeGroup=external",
            "attributeCollection=microarray"
        );
    }
}
