package org.reactome.otheridentifiers;

import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 6/6/2024
 */
public class EnsemblBioMartFileProcessor implements FileProcessor {
    private Path uniProtFilePath;
    private Path otherIdentifiersFilePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public EnsemblBioMartFileProcessor(Path uniProtFilePath, Path otherIdentifiersFilePath) {
        this.uniProtFilePath = uniProtFilePath;
        this.otherIdentifiersFilePath = otherIdentifiersFilePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        return getUniProtIdentifiersToOtherIdentifiers();
    }

    private Map<String, Set<String>> getUniProtIdentifiersToOtherIdentifiers() throws IOException {
        Map<String, Set<String>> uniProtIdentifiersToOtherIdentifiers = new HashMap<>();
        for (String uniProtIdentifier : getUniProtIdentifiersToEnsemblProteinIdentifiers().keySet()) {
            for (String ensemblProteinIdentifier : getUniProtIdentifiersToEnsemblProteinIdentifiers().get(uniProtIdentifier)) {
                for (String ensemblTranscriptIdentifier : getEnsemblProteinIdentifiersToEnsemblTranscriptIdentifiers().computeIfAbsent(ensemblProteinIdentifier, k -> new ArrayList<>())) {
                    for (String otherIdentifier : getEnsemblTranscriptIdentifiersToOtherIdentifiers().computeIfAbsent(ensemblTranscriptIdentifier, k -> new ArrayList<>())) {
                        uniProtIdentifiersToOtherIdentifiers.computeIfAbsent(uniProtIdentifier, k -> new HashSet<>()).add(otherIdentifier);
                    }
                }
            }
        }
        return uniProtIdentifiersToOtherIdentifiers;
    }

    private Map<String, List<String>> getEnsemblTranscriptIdentifiersToOtherIdentifiers() throws IOException {
        Map<String, List<String>> ensemblTranscriptIdentifiersToOtherIdentifiers = new HashMap<>();

        List<String> otherIdentifiersFileLines = Files.readAllLines(getOtherIdentifiersFilePath());
        for (String otherIdentifierFileLine : otherIdentifiersFileLines) {
            String ensemblTranscriptIdentifier = getEnsemblTranscriptIdentifier(otherIdentifierFileLine);
            String otherIdentifier = getOtherIdentifier(otherIdentifierFileLine);

            ensemblTranscriptIdentifiersToOtherIdentifiers.
                computeIfAbsent(ensemblTranscriptIdentifier, k -> new ArrayList<>()).add(otherIdentifier);
        }

        return ensemblTranscriptIdentifiersToOtherIdentifiers;
    }

    private Map<String, List<String>> getEnsemblProteinIdentifiersToEnsemblTranscriptIdentifiers() throws IOException {
        Map<String, List<String>> ensemblProteinIdentifiersToEnsemblTranscriptIdentifiers = new HashMap<>();

        List<String> otherIdentifiersFileLines = Files.readAllLines(getOtherIdentifiersFilePath());
        for (String otherIdentifierFileLine : otherIdentifiersFileLines) {
            String ensemblProteinIdentifier = getEnsemblProteinIdentifier(otherIdentifierFileLine);
            String ensemblTranscriptIdentifier = getEnsemblTranscriptIdentifier(otherIdentifierFileLine);

            ensemblProteinIdentifiersToEnsemblTranscriptIdentifiers.
                computeIfAbsent(ensemblProteinIdentifier, k -> new ArrayList<>()).add(ensemblTranscriptIdentifier);
        }

        return ensemblProteinIdentifiersToEnsemblTranscriptIdentifiers;
    }

    private Map<String, List<String>> getUniProtIdentifiersToEnsemblProteinIdentifiers() throws IOException {
        Map<String, List<String>> uniProtIdentifiersToEnsemblProteinIdentifiers = new HashMap<>();

        List<String> uniProtFileLines = Files.readAllLines(getUniProtFilePath());
        for (String uniProtFileLine : uniProtFileLines) {
            String uniProtIdentifier = getUniProtIdentifier(uniProtFileLine);
            String ensemblProteinIdentifier = getEnsemblProteinIdentifier(uniProtFileLine);

            uniProtIdentifiersToEnsemblProteinIdentifiers.
                computeIfAbsent(uniProtIdentifier, k -> new ArrayList<>()).add(ensemblProteinIdentifier);
        }

        return uniProtIdentifiersToEnsemblProteinIdentifiers;
    }

    private String getEnsemblProteinIdentifier(String line) {
        final int ensemblProteinIndex = 2;
        return line.split("\t")[ensemblProteinIndex];
    }

    private String getEnsemblTranscriptIdentifier(String line) {
        final int ensemblTranscriptIndex = 1;
        return line.split("\t")[ensemblTranscriptIndex];
    }

    private String getUniProtIdentifier(String line) {
        final int uniProtIndex = 3;
        return line.split("\t")[uniProtIndex];
    }

    private String getOtherIdentifier(String line) {
        final int otherIdentifierIndex = 3;
        return line.split("\t")[otherIdentifierIndex];
    }

    private Path getUniProtFilePath() {
        return this.uniProtFilePath;
    }

    private Path getOtherIdentifiersFilePath() {
        return this.otherIdentifiersFilePath;
    }
}
