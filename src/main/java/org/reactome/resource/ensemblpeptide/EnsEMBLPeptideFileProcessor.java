package org.reactome.resource.ensemblpeptide;

import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLPeptideFileProcessor implements FileProcessor {
    private Path[] uniProtFilePaths;

    public EnsEMBLPeptideFileProcessor(Path ...uniProtFilePaths) {
        this.uniProtFilePaths = uniProtFilePaths;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        Map<String, Set<String>> uniProtToEnsEMBLIdentifiers = new HashMap<>();

        for (Map<String, Set<String>> speciesUniProtToEnsEMBLIdentifiers : getListOfSpeciesUniProtToEnsEMBLIdentifiers()) {
            uniProtToEnsEMBLIdentifiers.putAll(speciesUniProtToEnsEMBLIdentifiers);
        }

        return uniProtToEnsEMBLIdentifiers;
    }

    private List<Map<String, Set<String>>> getListOfSpeciesUniProtToEnsEMBLIdentifiers() throws IOException {
        List<Map<String, Set<String>>> listOfSpeciesUniProtToEnsEMBLIdentifiers = new ArrayList<>();

        for (Path speciesUniProtFilePath : this.uniProtFilePaths) {
            Map<String, Set<String>> speciesUniProtToEnsEMBLIdentifiers =
                getSpeciesUniProtToEnsEMBLIdentifiers(speciesUniProtFilePath);
            listOfSpeciesUniProtToEnsEMBLIdentifiers.add(speciesUniProtToEnsEMBLIdentifiers);
        }

        return listOfSpeciesUniProtToEnsEMBLIdentifiers;
    }

    private Map<String, Set<String>> getSpeciesUniProtToEnsEMBLIdentifiers(Path speciesUniProtFilePath)
        throws IOException {

        Map<String, Set<String>> uniProtToEnsEMBLIdentifiers = new HashMap<>();
        Files.lines(speciesUniProtFilePath).forEach(fileLine -> {
            String uniProtIdentifier = parseUniProtFromFileLine(fileLine);
            String ensEMBLPeptideIdentifier = parseEnsEMBLPeptideFromFileLine(fileLine);

            uniProtToEnsEMBLIdentifiers.computeIfAbsent(uniProtIdentifier,
                k -> new HashSet<>()).add(ensEMBLPeptideIdentifier);
        });

        return uniProtToEnsEMBLIdentifiers;
    }

    private String parseEnsEMBLPeptideFromFileLine(String fileLine) {
        final int ensEMBLPeptideIdentifierIndex = 2;
        return fileLine.split("\t")[ensEMBLPeptideIdentifierIndex];
    }

    private String parseUniProtFromFileLine(String fileLine) {
        final int uniProtIdentifierIndex = 3;
        return fileLine.split("\t")[uniProtIdentifierIndex];
    }
}
