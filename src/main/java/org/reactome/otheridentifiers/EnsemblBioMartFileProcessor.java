package org.reactome.otheridentifiers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static Logger logger = LogManager.getLogger();

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
        logger.info("Getting UniProt to Other Identifiers mapping");
        Map<String, Set<String>> uniProtIdentifiersToOtherIdentifiers = new HashMap<>();

        Map<String, Set<String>> uniProtIdentifiersToEnsEMBLGeneIdentifiers =
            getUniProtIdentifiersToEnsemblGeneIdentifiers();
        Map<String, Set<String>> ensemblGeneIdentifiersToOtherIdentifiers =
            getEnsemblGeneIdentifiersToOtherIdentifiers();


        for (String uniProtIdentifier : uniProtIdentifiersToEnsEMBLGeneIdentifiers.keySet()) {
            for (String ensemblGeneIdentifier : uniProtIdentifiersToEnsEMBLGeneIdentifiers.get(uniProtIdentifier)) {
                for (String otherIdentifier : ensemblGeneIdentifiersToOtherIdentifiers.computeIfAbsent(ensemblGeneIdentifier, k -> new HashSet<>())) {
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

    private Map<String, Set<String>> getEnsemblGeneIdentifiersToOtherIdentifiers() throws IOException {
        Map<String, Set<String>> ensemblGeneIdentifiersToOtherIdentifiers = new HashMap<>();

        Files.lines(getOtherIdentifiersFilePath()).forEach(otherIdentifierFileLine -> {
            String ensemblGeneIdentifier = getEnsemblGeneIdentifier(otherIdentifierFileLine);
            String otherIdentifier = getOtherIdentifier(otherIdentifierFileLine);

            ensemblGeneIdentifiersToOtherIdentifiers.
                computeIfAbsent(ensemblGeneIdentifier, k -> new HashSet<>()).add(otherIdentifier);
        });

        return ensemblGeneIdentifiersToOtherIdentifiers;
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

    private Map<String, Set<String>> getUniProtIdentifiersToEnsemblGeneIdentifiers() throws IOException {
        Map<String, Set<String>> uniProtIdentifiersToEnsemblGeneIdentifiers = new HashMap<>();

        Files.lines(getUniProtFilePath()).forEach(uniProtFileLine -> {
            String uniProtIdentifier = getUniProtIdentifier(uniProtFileLine);
            String ensemblGeneIdentifier = getEnsemblGeneIdentifier(uniProtFileLine);

            uniProtIdentifiersToEnsemblGeneIdentifiers.
                computeIfAbsent(uniProtIdentifier, k -> new HashSet<>()).add(ensemblGeneIdentifier);
        });

        return uniProtIdentifiersToEnsemblGeneIdentifiers;
    }

    private String getEnsemblGeneIdentifier(String line) {
        final int ensemblGeneIndex = 0;
        return line.split("\t")[ensemblGeneIndex];
    }

//    private String getEnsemblTranscriptIdentifier(String line) {
//        final int ensemblTranscriptIndex = 1;
//        return line.split("\t")[ensemblTranscriptIndex];
//    }

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
