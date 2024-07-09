package org.reactome.otheridentifiers;

import org.reactome.utils.ConfigParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
 * Created 7/8/2024
 */
public class EnsEMBLBioMartUtils {

    public static List<String> queryBioMart(String bioMartSpeciesName, String searchTerm) throws IOException {
        String bioMartQuery = URLEncoder.encode(getBioMartQuery(bioMartSpeciesName, searchTerm), "UTF-8");
        return getContentFromURL(new URL(getBaseBioMartQueryURL() + bioMartQuery));
    }

    public static void saveFile(Path filePath, List<String> fileContent) throws IOException {
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

    public static List<String> getBioMartSpeciesNames() {
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

    public static Path getOutputDirectory() {
        return ConfigParser.getDownloadDirectoryPath().resolve("biomart");
    }

    public static List<String> getContentFromURL(URL url) throws IOException {
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

    public static String getBaseBioMartURL() {
        return "https://www.ensembl.org/biomart/martservice?";
    }

    private static boolean hasIdentifier(String line) {
        final int identifierColumnIndex = 3;
        List<String> lineColumns = Arrays.asList(line.split("\t"));
        return lineColumns.size() > identifierColumnIndex && !lineColumns.get(identifierColumnIndex).isEmpty();
    }

    private static String getBaseBioMartQueryURL() {
        return getBaseBioMartURL() + "query=";
    }

    private static String getBioMartQuery(String bioMartSpeciesName, String searchTerm) {
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
}
