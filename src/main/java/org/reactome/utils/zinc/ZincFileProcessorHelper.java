package org.reactome.utils.zinc;

import org.reactome.utils.ResourceJSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.reactome.utils.UniProtUtils.isValidUniProtId;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/31/2024
 */
public class ZincFileProcessorHelper {
    private static final int ZINC_IDENTIFIER_INDEX = 0;
    private static final int UNIPROT_IDENTIFIER_INDEX = 2;

    private String zincReferenceName;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;
    private Path filePath;

    private ZincFileProcessorHelper(String zincReferenceName, Path filePath) {
        this.zincReferenceName = zincReferenceName;
        this.filePath = filePath;
    }

    public static ZincFileProcessorHelper get(String zincReferenceName, Path filePath) {
        return new ZincFileProcessorHelper(zincReferenceName, filePath);
    }

    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.lines(getFilePath(), StandardCharsets.ISO_8859_1).skip(1).forEach(line -> {
                String[] lineColumns = line.split(",");
                String zincId = lineColumns[ZINC_IDENTIFIER_INDEX];
                String uniprotId = lineColumns.length > UNIPROT_IDENTIFIER_INDEX ?
                    lineColumns[UNIPROT_IDENTIFIER_INDEX] : "";

                if (!uniprotId.isEmpty() && isValidUniProtId(uniprotId) && zincURLHasContent(zincId)) {
                    this.uniProtToResourceIdentifiers
                        .computeIfAbsent(uniprotId, k -> new HashSet<>())
                        .add(zincId);
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    public boolean zincURLHasContent(String zincId) {
        String accessURL = ResourceJSONParser.getResourceJSONObject(getZincReferenceName())
            .getJSONObject("referenceDatabase").getString("accessURL");
        accessURL = accessURL.replace("###ID###", zincId).replaceFirst("/$", ".csv");
        try {
            System.out.println(accessURL);
            List<String> contents = getURLContent(accessURL);
            System.out.println(contents);

            return contents.size() > 1;
        } catch (IOException e) {
            System.err.println("Unable to get URL content from " + accessURL + ": " + e);
            return false;
        }
    }

    private Path getFilePath() {
        return this.filePath;
    }

    private List<String> getURLContent(String accessURL) throws IOException {
        URL url = new URL(accessURL);

        List<String> urlContentLines = new ArrayList<>();
        try (InputStream inputStream = url.openStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                urlContentLines.add(line);
            }
        }
        return urlContentLines;
    }

    private String getZincReferenceName() {
        return this.zincReferenceName;
    }

}
