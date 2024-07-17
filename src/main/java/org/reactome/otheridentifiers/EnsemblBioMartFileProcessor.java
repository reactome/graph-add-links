package org.reactome.otheridentifiers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 6/6/2024
 */
public class EnsemblBioMartFileProcessor implements FileProcessor {
    private static Logger logger = LogManager.getLogger();

    private List<Path> uniProtFilePaths;
    private List<Path> otherIdentifiersFilePaths;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public EnsemblBioMartFileProcessor(List<Path> uniProtFilePaths, List<Path> otherIdentifiersFilePaths) {
        this.uniProtFilePaths = uniProtFilePaths;
        this.otherIdentifiersFilePaths = otherIdentifiersFilePaths;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        return getUniProtIdentifiersToOtherIdentifiers();
    }

    private Map<String, Set<String>> getUniProtIdentifiersToOtherIdentifiers() throws IOException {
        logger.info("Getting UniProt to Other Identifiers mapping");
        Map<String, Set<String>> uniProtIdentifiersToOtherIdentifiers = new HashMap<>();

        Map<String, Set<String>> uniProtIdentifiersToEnsEMBLGeneIdentifiers =
            getUniProtIdentifiersToEnsEMBLGeneIdentifiers();

        Set<String> ensEMBLGeneIdentifiers = getEnsEMBLGeneIdentifiers(uniProtIdentifiersToEnsEMBLGeneIdentifiers);
        Map<String, Set<String>> ensEMBLGeneIdentifiersToOtherIdentifiers =
            getEnsEMBLGeneIdentifiersToOtherIdentifiers(ensEMBLGeneIdentifiers);


        for (String uniProtIdentifier : uniProtIdentifiersToEnsEMBLGeneIdentifiers.keySet()) {
            for (String ensEMBLGeneIdentifier : uniProtIdentifiersToEnsEMBLGeneIdentifiers.get(uniProtIdentifier)) {
                for (String otherIdentifier : ensEMBLGeneIdentifiersToOtherIdentifiers.computeIfAbsent(ensEMBLGeneIdentifier, k -> new HashSet<>())) {
                    uniProtIdentifiersToOtherIdentifiers.computeIfAbsent(uniProtIdentifier, k -> new HashSet<>()).add(otherIdentifier);
                }
            }
//            logger.info(uniProtIdentifier);
//            for (String ensemblProteinIdentifier : getUniProtIdentifiersToEnsemblGeneIdentifiers().get(uniProtIdentifier)) {
//                logger.info(ensemblProteinIdentifier);
//                for (String ensemblTranscriptIdentifier : getEnsemblProteinIdentifiersToEnsemblTranscriptIdentifiers().computeIfAbsent(ensemblProteinIdentifier, k -> new ArrayList<>())) {
//                    logger.info(ensemblTranscriptIdentifier);

             //   }
            //}
        }
        logger.info("Finished getting UniProt to Other Identifiers mapping");
        return uniProtIdentifiersToOtherIdentifiers;
    }

    private Map<String, Set<String>> getEnsEMBLGeneIdentifiersToOtherIdentifiers(Set<String> ensEMBLGeneIdentifiers)
        throws IOException {

        Map<String, Set<String>> ensEMBLGeneIdentifiersToOtherIdentifiers = new HashMap<>();

        for (Path otherIdentifiersFilePath : getOtherIdentifiersFilePaths()) {
            Files.lines(otherIdentifiersFilePath).forEach(otherIdentifierFileLine -> {
                String ensEMBLGeneIdentifier = parseEnsEMBLGeneIdentifierFromFileLine(otherIdentifierFileLine);
                String otherIdentifier = parseOtherIdentifierFromFileLine(otherIdentifierFileLine);

                if (ensEMBLGeneIdentifiers.contains(ensEMBLGeneIdentifier)) {
                    ensEMBLGeneIdentifiersToOtherIdentifiers.
                        computeIfAbsent(ensEMBLGeneIdentifier, k -> new HashSet<>()).add(otherIdentifier);
                }
            });
        }

        return ensEMBLGeneIdentifiersToOtherIdentifiers;
    }

//    private Map<String, List<String>> getEnsemblProteinIdentifiersToEnsemblTranscriptIdentifiers() throws IOException {
//        Map<String, List<String>> ensemblProteinIdentifiersToEnsemblTranscriptIdentifiers = new HashMap<>();
//
//        List<String> otherIdentifiersFileLines = Files.readAllLines(getOtherIdentifiersFilePath());
//        for (String otherIdentifierFileLine : otherIdentifiersFileLines) {
//            String ensemblProteinIdentifier = getEnsemblGeneIdentifier(otherIdentifierFileLine);
//            String ensemblTranscriptIdentifier = getEnsemblTranscriptIdentifier(otherIdentifierFileLine);
//
//            ensemblProteinIdentifiersToEnsemblTranscriptIdentifiers.
//                computeIfAbsent(ensemblProteinIdentifier, k -> new ArrayList<>()).add(ensemblTranscriptIdentifier);
//        }
//
//        return ensemblProteinIdentifiersToEnsemblTranscriptIdentifiers;
//    }

    private Map<String, Set<String>> getUniProtIdentifiersToEnsEMBLGeneIdentifiers() throws IOException {
        Map<String, Set<String>> uniProtIdentifiersToEnsEMBLGeneIdentifiers = new HashMap<>();

        for (Path uniProtFilePath : getUniProtFilePaths()) {
            Files.lines(uniProtFilePath).forEach(uniProtFileLine -> {
                String uniProtIdentifier = parseUniProtIdentifierFromFileLine(uniProtFileLine);
                String ensEMBLGeneIdentifier = parseEnsEMBLGeneIdentifierFromFileLine(uniProtFileLine);

                uniProtIdentifiersToEnsEMBLGeneIdentifiers.
                    computeIfAbsent(uniProtIdentifier, k -> new HashSet<>()).add(ensEMBLGeneIdentifier);
            });
        }

        return uniProtIdentifiersToEnsEMBLGeneIdentifiers;
    }

    private Set<String> getEnsEMBLGeneIdentifiers(Map<String, Set<String>> uniProtIdentifiersToEnsEMBLGeneIdentifiers) {
        return uniProtIdentifiersToEnsEMBLGeneIdentifiers
            .values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    private String parseEnsEMBLGeneIdentifierFromFileLine(String fileLine) {
        final int ensEMBLGeneIndex = 0;
        return fileLine.split("\t")[ensEMBLGeneIndex];
    }

//    private String getEnsemblTranscriptIdentifier(String line) {
//        final int ensemblTranscriptIndex = 1;
//        return line.split("\t")[ensemblTranscriptIndex];
//    }

    private String parseUniProtIdentifierFromFileLine(String fileLine) {
        final int uniProtIndex = 3;
        return fileLine.split("\t")[uniProtIndex];
    }

    private String parseOtherIdentifierFromFileLine(String fileLine) {
        final int otherIdentifierIndex = 3;
        return fileLine.split("\t")[otherIdentifierIndex];
    }

    private List<Path> getUniProtFilePaths() {
        return this.uniProtFilePaths;
    }

    private List<Path> getOtherIdentifiersFilePaths() {
        return this.otherIdentifiersFilePaths;
    }
}
