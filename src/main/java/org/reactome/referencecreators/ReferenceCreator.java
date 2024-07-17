package org.reactome.referencecreators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.Result;
import org.reactome.IdentifierCreator;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.*;

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
 *         Created 4/5/2022
 */
public abstract class ReferenceCreator implements IdentifierCreator {
    private static final Logger logger = LogManager.getLogger();

    private String resourceName;
    private ReferenceDatabase referenceDatabase;
    private Map<String, Set<String>> sourceIdentifierToReferenceIdentifiers;

    private Set<String> existingIdentifiers;

    public ReferenceCreator(String resourceName, Map<String, Set<String>> uniprotIdentifierToReferenceIdentifiers)
        throws IllegalArgumentException {

        this.resourceName = resourceName;
        this.referenceDatabase = ReferenceDatabase.parseReferenceDatabase(resourceName);
        this.sourceIdentifierToReferenceIdentifiers = uniprotIdentifierToReferenceIdentifiers;
    }

    public String getResourceName() {
        return this.resourceName;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return this.referenceDatabase;
    }

    public Map<String, Set<String>> getSourceIdentifierToReferenceIdentifiers() {
        return this.sourceIdentifierToReferenceIdentifiers;
    }

    public void insertIdentifiers() throws IOException, URISyntaxException {
        createReferenceDatabaseNode();
        createInstanceEditNode();
        writeCSV();
        readCSV();
    }

    public void writeCSV() throws IOException, URISyntaxException {
        Path resourceDirectory = getReferenceCreatorCSVDirectory();

        Files.createDirectories(resourceDirectory);

        writeReferenceCSVHeader(getIdentifierCSVFilePath());
        writeRelationshipCSVHeader(getRelationshipCSVFilePath());

        logger.info("Creating source database object to external identifiers map...");

        Map<IdentifierNode, List<? extends IdentifierNode>> sourceToExternalIdentifiersMap =
            this.createIdentifiers();

        logger.info("Source to database identifiers map created");

        logger.info("Writing CSV");
        for (IdentifierNode sourceIdentifierNode : sourceToExternalIdentifiersMap.keySet()) {
            List<? extends IdentifierNode> externalIdentifiers = sourceToExternalIdentifiersMap.get(sourceIdentifierNode);
            logger.debug("Identifier Node: " + sourceIdentifierNode);
            logger.debug("External Identifiers: " + externalIdentifiers);

            for (IdentifierNode externalIdentifier : externalIdentifiers) {
                //if (!existsInDatabase(externalIdentifier)) {
                    writeCSVForIdentifier(externalIdentifier, sourceIdentifierNode);
                //}
            }
        }
        logger.info("CSV data complete");
    }

    public abstract void readCSV() throws IOException, URISyntaxException;

    public void createReferenceDatabaseNode() {
        getReferenceDatabase().insertNode();
    }

    public void createInstanceEditNode() {
        InstanceEdit.get().insertNode();
    }


    protected Set<String> getIdentifierValues(IdentifierNode identifierNode) {
        return getIdentifierValues(identifierNode.getIdentifier());
    }

    protected Set<String> getIdentifierValues(String sourceIdentifier) {
        return getSourceIdentifierToReferenceIdentifiers()
            .computeIfAbsent(sourceIdentifier, k -> new HashSet<>());
    }

    protected abstract List<? extends IdentifierNode> createExternalIdentifiersForIdentifierNode(
        IdentifierNode identifierNode);

    protected abstract List<IdentifierNode> getIdentifierNodes();

    Path getReferenceCreatorCSVDirectory() throws URISyntaxException {
        return Paths.get(new File("src/main/resources/reference_creator_csv").getAbsolutePath());
        //Path resourceDirectory = Paths.get(ReferenceCreator.class.getClassLoader().getResource(".").toURI());
        //return resourceDirectory.resolve("reference_creator_csv");

    }

    private boolean existsInDatabase(IdentifierNode identifierNode) {
        if (this.existingIdentifiers == null) {
            String identifiersQuery =
                "MATCH (i)-[:referenceDatabase]->(rd:ReferenceDatabase)" +
                    " WHERE rd.displayName = \"" + getReferenceDatabase().getDisplayName() + "\"" +
                    " RETURN i.identifier";

            Result identifiersQueryResult = ReactomeGraphDatabase.getSession().run(identifiersQuery);
            this.existingIdentifiers = identifiersQueryResult
                .stream()
                .map(record -> record.get(0).asString())
                .collect(Collectors.toSet());
        }

        return this.existingIdentifiers.contains(identifierNode.getIdentifier());
    }

    private Map<IdentifierNode, List<? extends IdentifierNode>> createIdentifiers() {
        Map<IdentifierNode, List<? extends IdentifierNode>> sourceToExternalIdentifiers = new LinkedHashMap<>();

        for (IdentifierNode identifierNode : getIdentifierNodes()) {
            sourceToExternalIdentifiers.put(
                identifierNode,
                createExternalIdentifiersForIdentifierNode(identifierNode)
            );
        }

        return sourceToExternalIdentifiers;
    }

//    protected static void createNodesWithRelationship(
//        GraphNode fromNode, GraphNode toNode, GraphNode.Relationship relationship) {
//
//        fromNode.insertNode();
//        toNode.insertNode();
//
//        String createQuery = "MATCH (fn:" + fromNode.getSchemaClass() + "{dbId: " + fromNode.getDbId() + "})" +
//            " MATCH (tn: " + toNode.getSchemaClass() + "{dbId: " + toNode.getDbId() + "})" +
//            " CREATE (fn)-"+ relationship + "->(tn)";
//        logger.info("Create node query\n" + createQuery);
//        ReactomeGraphDatabase.getSession().run(createQuery);
//    }

    private void writeCSVForIdentifier(IdentifierNode externalIdentifier, IdentifierNode sourceNodeIdentifier)
        throws IOException, URISyntaxException {

        writeExternalIdentifierLine(externalIdentifier);
        writeRelationshipLine(sourceNodeIdentifier.getDbId(), externalIdentifier.getDbId(),
            getReferenceDatabase().getDbId(), InstanceEdit.get().getDbId());
    }


    private void writeReferenceCSVHeader(Path referenceCSVFilePath) throws IOException {
        final String header = String.join(",",
            "DbId", "DisplayName", "SchemaClass", "Identifier", "ReferenceDbName", "URL"
        ).concat(System.lineSeparator());

        Files.write(
            referenceCSVFilePath,
            header.getBytes(),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private void writeRelationshipCSVHeader(Path sourceToDatabaseIdentifierCSVFilePath) throws IOException {
        final String header = String.join(",",
            "SourceDbId","ExternalIdentifierDbId","ReferenceDatabaseDbId","InstanceEditDbId"
        ).concat(System.lineSeparator());

        Files.write(
            sourceToDatabaseIdentifierCSVFilePath,
            header.getBytes(),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private void writeRelationshipLine(Long... dbIds) throws IOException, URISyntaxException {
        final String line = Arrays.stream(dbIds)
            .map(String::valueOf).collect(Collectors.joining(",")).concat(System.lineSeparator());

        Files.write(
            getRelationshipCSVFilePath(),
            line.getBytes(),
            StandardOpenOption.APPEND
        );
    }

    private void writeExternalIdentifierLine(IdentifierNode externalIdentifier)
        throws IOException, URISyntaxException {

        final String line = String.join(",",
            String.valueOf(externalIdentifier.getDbId()),
            externalIdentifier.getDisplayName(),
            externalIdentifier.getSchemaClass(),
            externalIdentifier.getIdentifier(),
            externalIdentifier.getReferenceDatabaseDisplayName(),
            externalIdentifier.getUrl()
        ).concat(System.lineSeparator());

        Files.write(
            getIdentifierCSVFilePath(),
            line.getBytes(),
            StandardOpenOption.APPEND
        );
    }

    private Path getIdentifierCSVFilePath() throws URISyntaxException {
        return getReferenceCreatorCSVDirectory().resolve(getResourceName() + "_Identifiers.csv");
    }

    private Path getRelationshipCSVFilePath() throws URISyntaxException {
        return getReferenceCreatorCSVDirectory().resolve(getResourceName() + "_Relationships.csv");
    }
}
