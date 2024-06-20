package org.reactome.otheridentifiers;

import org.reactome.utils.ConfigParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 5/31/2024
 */
public class EnsemblBioMartRetriever {

//    public static void main(String[] args) throws IOException {
//        EnsemblBioMartRetriever ensemblBioMartRetriever = new EnsemblBioMartRetriever();
//        ensemblBioMartRetriever.downloadFiles();
//    }

    public void downloadFiles() throws IOException {
        Files.createDirectories(getOutputDirectory());

        for (String bioMartSpeciesName : getBioMartSpeciesNames()) {
            queryAndSaveFilesForOtherIdentifiers(bioMartSpeciesName);
            queryAndSaveFilesForUniProt(bioMartSpeciesName);
        }
    }

    private void queryAndSaveFilesForOtherIdentifiers(String bioMartSpeciesName) throws IOException {
        for (String otherIdentifierSearchTerm : getOtherIdentifierSearchTerms(bioMartSpeciesName)) {
            List<String> bioMartResponse = queryBioMart(bioMartSpeciesName, otherIdentifierSearchTerm);
            saveFile(getOtherIdentifierFilePath(bioMartSpeciesName), bioMartResponse);
        }
    }

    private void queryAndSaveFilesForUniProt(String bioMartSpeciesName) throws IOException {
        for (String uniProtSearchTerm : getUniProtSearchTerms()) {
            List<String> bioMartResponse = queryBioMart(bioMartSpeciesName, uniProtSearchTerm);
            saveFile(getUniProtFilePath(bioMartSpeciesName), bioMartResponse);
        }
    }

    private List<String> queryBioMart(String bioMartSpeciesName, String searchTerm) throws IOException {
        String bioMartQuery = URLEncoder.encode(getBioMartQuery(bioMartSpeciesName, searchTerm), "UTF-8");
        return getContentFromURL(new URL(getBaseBioMartQueryURL() + bioMartQuery));
    }

    private void saveFile(Path filePath, List<String> fileContent) throws IOException {
        for (String fileContentLine : fileContent) {
            if (hasIdentifier(fileContentLine)) {
                Files.write(
                    filePath,
                    fileContentLine.concat(System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
                );
            }
        }
    }

    private boolean hasIdentifier(String line) {
        final int identifierColumnIndex = 3;
        List<String> lineColumns = Arrays.asList(line.split("\t"));
        return lineColumns.size() > identifierColumnIndex && !lineColumns.get(identifierColumnIndex).isEmpty();
    }

    private Path getOtherIdentifierFilePath(String bioMartSpeciesName) {
        return getOutputDirectory().resolve(bioMartSpeciesName + "_microarray_go_ncbi_ids");
    }

    private Path getUniProtFilePath(String bioMartSpeciesName) {
        return getOutputDirectory().resolve(bioMartSpeciesName + "_uniprot");
    }

    private Path getOutputDirectory() {
        return ConfigParser.getDownloadDirectoryPath().resolve("biomodels");
    }

    private List<String> getMicroarrayTypesBySpecies(String bioMartSpeciesName) throws IOException {
        return getContentFromURL(getMicroarrayTypesURL(bioMartSpeciesName));
    }

    private List<String> getContentFromURL(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Unable to connect to url " + url);
        }

        List<String> urlContent = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            urlContent.add(line);
        }

        return urlContent;
    }

    private URL getMicroarrayTypesURL(String bioMartSpeciesName) throws MalformedURLException {
        return new URL(getBaseBioMartURL() + getMicroarrayTypesURLParameters(bioMartSpeciesName));
    }

    private String getBaseBioMartQueryURL() {
        return getBaseBioMartURL() + "query=";
    }

    private String getBaseBioMartURL() {
        return "https://www.ensembl.org/biomart/martservice?";
    }

    private String getMicroarrayTypesURLParameters(String bioMartSpeciesName) {
        return String.join("&",
            "type=listAttributes",
            "mart=ENSEMBL_MART_ENSEMBL",
            "virtualSchema=default",
            "dataset=" + bioMartSpeciesName + "_gene_ensembl",
            "interface=default",
            "attributePage=feature_page",
            "attributeGroup=external",
            "attributeCollection=microarray"
        );
    }

    private String getBioMartQuery(String bioMartSpeciesName, String searchTerm) {
        return String.join(System.lineSeparator(),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<!DOCTYPE Query>",
            "<Query virtualSchemaName=\"default\" formatter=\"TSV\" header=\"0\" uniqueRows=\"0\" count=\"\" datasetConfigVersion=\"0.6\">",
            "<Dataset name=\"" + bioMartSpeciesName + "_gene_ensembl\" interface=\"default\">",
            "<Attribute name=\"ensembl_gene_id\" />",
            "<Attribute name=\"ensembl_transcript_id\" />",
            "<Attribute name=\"ensembl_peptide_id\" />",
            "<Attribute name=\"" + searchTerm + "\" />",
            "</Dataset>",
            "</Query>"
        );
    }

    private List<String> getOtherIdentifierSearchTerms(String bioMartSpeciesName) throws IOException {
        List<String> otherIdentifierSearchTerms = new ArrayList<>();
        otherIdentifierSearchTerms.addAll(getMicroarrayTypesBySpecies(bioMartSpeciesName));
        otherIdentifierSearchTerms.addAll(Arrays.asList("go_id", "goslim_goa_accession", "entrezgene_id"));
        return otherIdentifierSearchTerms;
    }

    private List<String> getUniProtSearchTerms() {
        return Arrays.asList("uniprotswissprot","uniprotsptrembl");
    }

    private List<String> getBioMartSpeciesNames() {
        return Arrays.asList(
            "btaurus",
            "celegans",
            "clfamiliaris",
            "dmelanogaster",
            "drerio",
            "ggallus",
            "hsapiens",
            "mmusculus",
            "rnorvegicus",
            "scerevisiae",
            "sscrofa",
            "xtropicalis"
        );
    }
}
