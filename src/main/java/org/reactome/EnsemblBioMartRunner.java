package org.reactome;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactome.otheridentifiers.EnsemblBioMartFileProcessor;
import org.reactome.otheridentifiers.EnsemblBioMartOtherIdentifierCreator;
import org.reactome.otheridentifiers.EnsemblBioMartRetriever;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 6/21/2024
 */
public class EnsemblBioMartRunner {
    private static EnsemblBioMartRetriever ensemblBioMartRetriever;
    private static EnsemblBioMartFileProcessor ensemblBioMartFileProcessor;
    private static EnsemblBioMartOtherIdentifierCreator ensemblBioMartOtherIdentifierCreator;

    private static Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws IOException, URISyntaxException {
        downloadEnsemblBioMartMappingFiles();
        insertOtherIdentifiersIntoDatabase();
    }

    private static void downloadEnsemblBioMartMappingFiles() throws IOException {
        logger.info("Downloading EnsEMBL BioMart Mapping Files");

        ensemblBioMartRetriever = new EnsemblBioMartRetriever();
        ensemblBioMartRetriever.downloadFiles();

        logger.info("Download complete");
    }

    private static void insertOtherIdentifiersIntoDatabase() throws IOException, URISyntaxException {
        logger.info("Inserting Other Identifiers into graph database");

        for (String ensemblBioMartSpeciesName : ensemblBioMartRetriever.getBioMartSpeciesNames()) {
            insertOtherIdentifiersIntoDatabase(ensemblBioMartSpeciesName);
        }

        logger.info("Other Identifiers insertion complete");
    }

    private static void insertOtherIdentifiersIntoDatabase(String ensemblBioMartSpeciesName)
        throws IOException, URISyntaxException {

        logger.info("Inserting Other Identifiers for " + ensemblBioMartSpeciesName);

        Map<String, Set<String>> uniProtToOtherIdentifiers =
            getUniProtToOtherIdentifiersMapping(ensemblBioMartSpeciesName);
        ensemblBioMartOtherIdentifierCreator = new EnsemblBioMartOtherIdentifierCreator(uniProtToOtherIdentifiers);
        ensemblBioMartOtherIdentifierCreator.insertIdentifiers();

        logger.info("Other Identifiers insertion for " + ensemblBioMartSpeciesName + " complete");
    }

    private static Map<String, Set<String>> getUniProtToOtherIdentifiersMapping(String ensemblBioMartSpeciesName)
        throws IOException {

        Path uniProtFilePath = ensemblBioMartRetriever.getUniProtFilePath(ensemblBioMartSpeciesName);
        Path otherIdentifiersFilePath = ensemblBioMartRetriever.getOtherIdentifierFilePath(ensemblBioMartSpeciesName);
        ensemblBioMartFileProcessor = new EnsemblBioMartFileProcessor(uniProtFilePath, otherIdentifiersFilePath);
        return ensemblBioMartFileProcessor.getSourceToResourceIdentifiers();
    }
}
