package org.reactome.referencecreators;

import org.neo4j.driver.v1.Transaction;
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
public abstract class ReferenceCreator {
    private String resourceName;
    private ReferenceDatabase referenceDatabase;
    private Map<String, Set<String>> sourceIdentifierToReferenceIdentifiers;

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

    public abstract void writeCSV() throws IOException, URISyntaxException;

    public abstract void readCSV() throws URISyntaxException;

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

    protected abstract List<IdentifierNode> getIdentifierNodes();

    protected static void createNodesWithRelationship(
        GraphNode fromNode, GraphNode toNode, GraphNode.Relationship relationship) {

        fromNode.insertNode();
        toNode.insertNode();

        String createQuery = "MATCH (fn:" + fromNode.getSchemaClass() + "{dbId: " + fromNode.getDbId() + "})" +
            " MATCH (tn: " + toNode.getSchemaClass() + "{dbId: " + toNode.getDbId() + "})" +
            " CREATE (fn)-"+ relationship + "->(tn)";
        System.out.println(createQuery);
        ReactomeGraphDatabase.getSession().run(createQuery);
    }

    protected Path getReferenceCreatorCSVDirectory() throws URISyntaxException {
        return Paths.get(new File("src/main/resources/reference_creator_csv").getAbsolutePath());
        //return Paths.get(DatabaseIdentifierReferenceCreator.class.getClassLoader().getResource("reference_creator_csv").toURI());
    }


    protected void writeReferenceCSVHeader(Path referenceCSVFilePath) throws IOException {
        final String header = String.join(",",
            "DbId", "DisplayName", "SchemaClass", "Identifier", "ReferenceDbName", "URL"
        ).concat(System.lineSeparator());

        Files.write(
            referenceCSVFilePath,
            header.getBytes(),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    protected void writeRelationshipLine(Path relationshipCSVFilePath, Long ...dbIds) throws IOException {
        final String line = Arrays.stream(dbIds)
            .map(String::valueOf).collect(Collectors.joining(",")).concat(System.lineSeparator());

        Files.write(
            relationshipCSVFilePath,
            line.getBytes(),
            StandardOpenOption.APPEND
        );
    }
}
