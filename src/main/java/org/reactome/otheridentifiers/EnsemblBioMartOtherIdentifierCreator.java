package org.reactome.otheridentifiers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.IdentifierNode;
import org.reactome.graphnodes.InstanceEdit;
import org.reactome.graphnodes.ReferenceGeneProduct;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 6/17/2024
 */
public class EnsemblBioMartOtherIdentifierCreator {

    private static final Logger logger = LogManager.getLogger();

    private final Map<String, Set<String>> uniprotToResourceIdentifiers;
    private Map<Long, List<String>> referenceGeneProductDbIdToOtherIdentifiers;

    public EnsemblBioMartOtherIdentifierCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        this.uniprotToResourceIdentifiers = uniProtToResourceIdentifiers;
    }

    public void insertIdentifiers() throws IOException, URISyntaxException {
        writeCSV();
        readCSV();
    }

    void writeCSV() throws IOException, URISyntaxException {
        Path resourceDirectory = getReferenceCreatorCSVDirectory();

        Files.createDirectories(resourceDirectory);

        writeOtherIdentifierCSVHeader(getOtherIdentifierCSVFilePath());

        logger.info("Creating source database object to external identifiers map...");

        Map<IdentifierNode, Set<String>> sourceToOtherIdentifiersMap =
            getReferenceGeneProductToOtherIdentifiers();

        logger.info("Source to database identifiers map created");

        logger.info("Writing CSV");
        for (IdentifierNode sourceIdentifierNode : sourceToOtherIdentifiersMap.keySet()) {
            Set<String> otherIdentifiers = sourceToOtherIdentifiersMap.get(sourceIdentifierNode);
            logger.debug("Identifier Node: " + sourceIdentifierNode);
            logger.debug("Other Identifiers: " + otherIdentifiers);

            String otherIdentifiersNotInDatabase =
                otherIdentifiers.stream()
                //.filter(otherIdentifier -> !existsInDatabase(sourceIdentifierNode, otherIdentifier))
                .sorted()
                .collect(Collectors.joining(";"));

            writeCSVForIdentifier(sourceIdentifierNode, otherIdentifiersNotInDatabase);

        }
        logger.info("CSV data complete");
    }

    void readCSV() throws URISyntaxException {
        final String csvDirectory = getReferenceCreatorCSVDirectory().toString().replace("\\","/");

        logger.info("Inserting other identifiers...");
        String otherIdentifiersInsertionQuery =
            "USING PERIODIC COMMIT 100\n" +
            "LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/OtherIdentifiers.csv' AS row\n" +
            "MATCH (rgp:ReferenceGeneProduct {dbId: toInteger(row.SourceDbId)})\n" +
            "SET rgp.otherIdentifier = split(row.OtherIdentifiers, ';')";
        ReactomeGraphDatabase.getSession().writeTransaction(tx -> {
            tx.run(otherIdentifiersInsertionQuery);
            return null;
        });

    }

    Path getReferenceCreatorCSVDirectory() throws URISyntaxException {
        return Paths.get(new File("src/main/resources/reference_creator_csv").getAbsolutePath());
    }

    private Map<IdentifierNode, Set<String>> getReferenceGeneProductToOtherIdentifiers() {
        Map<IdentifierNode, Set<String>> referenceGeneProductToOtherIdentifiers = new HashMap<>();
        List<IdentifierNode> uniProtNodes = getIdentifierNodes();
        for (IdentifierNode uniProtNode : uniProtNodes) {
            Set<String> otherIdentifiers = this.uniprotToResourceIdentifiers.get(uniProtNode.getIdentifier());
            if (otherIdentifiers != null) {
                referenceGeneProductToOtherIdentifiers.put(uniProtNode, otherIdentifiers);
            }
        }
        return referenceGeneProductToOtherIdentifiers;
    }

    private List<IdentifierNode> getIdentifierNodes() {
        return new ArrayList<>(
            ReferenceGeneProduct.fetchReferenceGeneProductsForUniProtIdentifiers(getUniProtIdentifiers()).values()
        );
    }

    private Set<String> getUniProtIdentifiers() {
        return this.uniprotToResourceIdentifiers.keySet();
    }

    private boolean existsInDatabase(IdentifierNode sourceIdentifierNode, String otherIdentifier) {
        if (this.referenceGeneProductDbIdToOtherIdentifiers == null) {
            this.referenceGeneProductDbIdToOtherIdentifiers = new HashMap<>();

            String otherIdentifiersQuery =
                "MATCH (rgp:ReferenceGeneProduct)" +
                    " RETURN rgp.dbId AS dbId, rgp.otherIdentifier AS otherIdentifier";

            Result otherIdentifiersQueryResult = ReactomeGraphDatabase.getSession().run(otherIdentifiersQuery);
            while (otherIdentifiersQueryResult.hasNext()) {
                Record otherIdentifiersQueryRecord = otherIdentifiersQueryResult.next();
                //System.out.println(otherIdentifiersQueryRecord.get("dbId"));
                long dbId = otherIdentifiersQueryRecord.get("dbId").asLong();
                List<String> otherIdentifiers = getOtherIdentifiersFromRecord(otherIdentifiersQueryRecord);

                this.referenceGeneProductDbIdToOtherIdentifiers.put(dbId, otherIdentifiers);
            }
//            Set<String> otherIdentifiers = otherIdentifiersQueryResult
//                .stream()
//                .map(record -> record.get(0).asString())
//                .collect(Collectors.toSet());
        }

        return this.referenceGeneProductDbIdToOtherIdentifiers
            .computeIfAbsent(sourceIdentifierNode.getDbId(), k -> new ArrayList<>())
            .contains(otherIdentifier);
    }

    private List<String> getOtherIdentifiersFromRecord(Record otherIdentifiersQueryRecord) {
        Value otherIdentifiersValue = otherIdentifiersQueryRecord.get("otherIdentifier");
        if (otherIdentifiersValue.isNull()) {
            return new ArrayList<>();
        }

        return otherIdentifiersValue.asList(Values.ofString());
    }


    private void writeOtherIdentifierCSVHeader(Path otherIdentifierCSVFilePath) throws IOException {
        final String header = String.join(",",
            "SourceDbId", "OtherIdentifiers"
        ).concat(System.lineSeparator());

        Files.write(
            otherIdentifierCSVFilePath,
            header.getBytes(),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private void writeCSVForIdentifier(IdentifierNode sourceIdentifierNode, String otherIdentifier)
        throws IOException, URISyntaxException {

        final String line = String.join(",",
            String.valueOf(sourceIdentifierNode.getDbId()),
            otherIdentifier
        ).concat(System.lineSeparator());

        Files.write(
            getOtherIdentifierCSVFilePath(),
            line.getBytes(),
            StandardOpenOption.APPEND
        );
    }


    private Path getOtherIdentifierCSVFilePath() throws URISyntaxException {
        return getReferenceCreatorCSVDirectory().resolve("OtherIdentifiers.csv");
    }
}
