package org.reactome.resource.omim;

import org.reactome.fileprocessors.UniProtFileProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public class OMIMFileProcessor extends UniProtFileProcessor {
    private Set<String> mim2GeneIdentifiers;

    public OMIMFileProcessor(Path ...filePaths) throws IOException {
        super(filePaths[0]);
        this.mim2GeneIdentifiers = getMim2GeneIdentifiers(filePaths[1]);
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        Map<String, Set<String>> uniProtToOMIMIdentifiers = super.getSourceToResourceIdentifiers();

        Map<String, Set<String>> filteredUniProtToOMIMIdentifiers = new HashMap<>();
        for (String uniProtIdentifier : uniProtToOMIMIdentifiers.keySet()) {
            Set<String> omimIdentifiers = uniProtToOMIMIdentifiers.get(uniProtIdentifier);
            for (String omimIdentifier : omimIdentifiers) {
                if (this.mim2GeneIdentifiers.contains(omimIdentifier)) {
                    filteredUniProtToOMIMIdentifiers.computeIfAbsent(uniProtIdentifier, k -> new HashSet<>())
                        .add(omimIdentifier);
                }
            }
        }
        return filteredUniProtToOMIMIdentifiers;
    }

    private Set<String> getMim2GeneIdentifiers(Path mim2GeneFilePath) throws IOException {
        return Files.lines(mim2GeneFilePath)
            .filter(line -> !line.startsWith("#"))
            .filter(line -> identifierType(line).equals("gene"))
            .map(this::mimIdentifier)
            .collect(Collectors.toSet());
    }

    private String identifierType(String line) {
        return columnValue(line, 1);
    }

    private String mimIdentifier(String line) {
        return columnValue(line, 0);
    }

    private String columnValue(String line, int index) {
        return line.split("\t")[index];
    }
}
