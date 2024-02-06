package org.reactome.referencecreators;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public abstract class DatabaseIdentifierReferenceCreator extends ReferenceCreator {


    public DatabaseIdentifierReferenceCreator(
        String referenceName, Map<String, Set<String>> sourceIdentifierToReferenceIdentifiers)
        throws IllegalArgumentException {

        super(referenceName, sourceIdentifierToReferenceIdentifiers);
    }

//    public Map<String, List<DatabaseIdentifier>> createDatabaseIdentifiersByUniProtIdentifier() {
//        Map<String, List<DatabaseIdentifier>> uniProtToDatabaseIdentifiers = new HashMap<>();
//        for (String uniProtIdentifier : getSourceIdentifierToReferenceIdentifiers().keySet()) {
//            DatabaseIdentifier.DatabaseIdentifierBuilder databaseIdentifierBuilder =
//                new DatabaseIdentifier.DatabaseIdentifierBuilder(getReferenceDatabase());
//            List<String> databaseIdentifierValues = getSourceIdentifierToReferenceIdentifiers().get(uniProtIdentifier);
//            for (String databaseIdentifierValue : databaseIdentifierValues) {
//                DatabaseIdentifier databaseIdentifier = databaseIdentifierBuilder.build(databaseIdentifierValue);
//                uniProtToDatabaseIdentifiers
//                    .computeIfAbsent(uniProtIdentifier, k-> new ArrayList<>())
//                    .add(databaseIdentifier);
//            }
//        }
//        return uniProtToDatabaseIdentifiers;
//    }

    @Override
    public void writeCSV() throws IOException, URISyntaxException {
        Path resourceDirectory = getReferenceCreatorCSVDirectory();

        Path databaseIdentifierCSVFilePath = resourceDirectory.resolve(getResourceName() + "_DatabaseIdentifiers.csv");
        writeReferenceCSVHeader(databaseIdentifierCSVFilePath);

        Path relationshipCSVFilePath = resourceDirectory.resolve(getResourceName() + "_Relationships.csv");
        writeRelationshipCSVHeader(relationshipCSVFilePath);

        System.out.println("Creating source database object to database identifiers map...");

        Map<IdentifierNode, List<DatabaseIdentifier>> identifierNodeToDatabaseIdentifiersMap =
            this.createDatabaseIdentifiers();

        System.out.println("Map created");

        System.out.println("Writing CSV");
        for (IdentifierNode identifierNode : identifierNodeToDatabaseIdentifiersMap.keySet()) {
            List<DatabaseIdentifier> databaseIdentifiers = identifierNodeToDatabaseIdentifiersMap.get(identifierNode);
            System.out.println("Identifier Node: " + identifierNode);
            System.out.println("Database Identifiers: " + databaseIdentifiers);

            for (DatabaseIdentifier databaseIdentifier : databaseIdentifiers) {
                writeDatabaseIdentifierLine(databaseIdentifierCSVFilePath, databaseIdentifier);
                writeRelationshipLine(relationshipCSVFilePath, identifierNode.getDbId(), databaseIdentifier.getDbId(),
                    getReferenceDatabase().getDbId(), InstanceEdit.get().getDbId());
            }
        }
        System.out.println("CSV data complete");
    }

    @Override
    public void readCSV() throws URISyntaxException, IOException {
        final String csvDirectory = getReferenceCreatorCSVDirectory().toString().replace("\\","/");

        System.out.println("Creating database identifiers...");
        ReactomeGraphDatabase.getSession().writeTransaction(tx -> {
            tx.run("LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_DatabaseIdentifiers.csv' AS row " +
            "CREATE (:DatabaseIdentifier:DatabaseObject " +
                "{dbId: toInteger(row.DbId), displayName: row.DisplayName, schemaClass: row.SchemaClass, " +
                "identifier: row.Identifier, referenceDatabase: row.ReferenceDbName, url: row.URL})");
            return null;
        });

        System.out.println("Creating relationships...");
//
//        try (Transaction tx = ReactomeGraphDatabase.getSession().beginTransaction()) {
//            String createRelationshipQuery =
//                "MATCH (do:DatabaseObject {dbId: $sourceDbId}), (di:DatabaseIdentifier {dbId: $databaseIdentifierDbId}), " +
//                    "(rd:ReferenceDatabase {dbId: $referenceDatabaseDbId}), (ie:InstanceEdit {dbId: $instanceEditDbId})" +
//                    "CREATE (do)-[:crossReference]->(di) " +
//                    "CREATE (di)-[:referenceDatabase]->(rd) " +
//                    "CREATE (di)-[:created]->(ie)";
//            List<String> relationshipLines = Files.readAllLines(Paths.get(csvDirectory, getResourceName() + "_Relationships.csv"));
//            for (String relationshipLine : relationshipLines) {
//                String[] relationshipColumns = relationshipLine.split(",");
//                long sourceDbId = Long.parseLong(relationshipColumns[0]);
//                long databaseIdentifierDbId = Long.parseLong(relationshipColumns[1]);
//                long referenceDatabaseDbId = Long.parseLong(relationshipColumns[2]);
//                long instanceEditDbId = Long.parseLong(relationshipColumns[3]);
//
//                Map<String, Object> parameters = new LinkedHashMap<>();
//                parameters.put("sourceDbId", sourceDbId);
//                parameters.put("databaseIdentifierDbId", databaseIdentifierDbId);
//                parameters.put("referenceDatabaseDbId", referenceDatabaseDbId);
//                parameters.put("instanceEditDbId", instanceEditDbId);
//
//                System.out.println("Running query with parameters " + parameters);
//                tx.run(createRelationshipQuery, parameters);
//           }
//            tx.commitAsync().toCompletableFuture().join();
//        }

        String query =
            "USING PERIODIC COMMIT 100\n" +
            "LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_Relationships.csv' AS row\n" +
            "MATCH (do:DatabaseObject {dbId: toInteger(row.SourceDbId)})\n" +
            "MATCH (di:DatabaseIdentifier {dbId: toInteger(row.DatabaseIdentifierDbId)})\n" +
            "MATCH (rd:ReferenceDatabase {dbId: toInteger(row.ReferenceDatabaseDbId)})\n" +
            "MATCH (ie:InstanceEdit {dbId: toInteger(row.InstanceEditDbId)})\n" +
            "CREATE (do)-[:crossReference]->(di)\n" +
            "CREATE (di)-[:referenceDatabase]->(rd)\n" +
            "CREATE (di)-[:created]->(ie) ";

        System.out.println(query);
        StatementResult statementResult = ReactomeGraphDatabase.getSession().run(query);
        if (statementResult.hasNext()) {
            System.out.println(statementResult.next());
        }
        System.out.println("Done");
    }

    private Map<IdentifierNode, List<DatabaseIdentifier>> createDatabaseIdentifiers() {
        Map<IdentifierNode, List<DatabaseIdentifier>> identifierNodeToDatabaseIdentifiers = new LinkedHashMap<>();

        for (IdentifierNode identifierNode : getIdentifierNodes()) {
            identifierNodeToDatabaseIdentifiers.put(
                identifierNode,
                createDatabaseIdentifiersForIdentifierNode(identifierNode)
            );
        }

        return identifierNodeToDatabaseIdentifiers;
    }

    private List<DatabaseIdentifier> createDatabaseIdentifiersForIdentifierNode(IdentifierNode identifierNode) {
        List<DatabaseIdentifier> databaseIdentifiers = new ArrayList<>();
        for (String databaseIdentifierValue : getIdentifierValues(identifierNode)) {
            databaseIdentifiers.add(new DatabaseIdentifier(databaseIdentifierValue, getReferenceDatabase()));
        }
        return databaseIdentifiers;
    }

    private void writeRelationshipCSVHeader(Path sourceToDatabaseIdentifierCSVFilePath) throws IOException {
        final String header = String.join(",",
            "SourceDbId","DatabaseIdentifierDbId","ReferenceDatabaseDbId","InstanceEditDbId"
        ).concat(System.lineSeparator());

        Files.write(
            sourceToDatabaseIdentifierCSVFilePath,
            header.getBytes(),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private void writeDatabaseIdentifierLine(Path databaseIdentifierCSVFilePath, DatabaseIdentifier databaseIdentifier)
        throws IOException {

        final String line = String.join(",",
            String.valueOf(databaseIdentifier.getDbId()),
            databaseIdentifier.getDisplayName(),
            databaseIdentifier.getSchemaClass(),
            databaseIdentifier.getIdentifier(),
            databaseIdentifier.getReferenceDatabaseDisplayName(),
            databaseIdentifier.getUrl()
        ).concat(System.lineSeparator());

        Files.write(
            databaseIdentifierCSVFilePath,
            line.getBytes(),
            StandardOpenOption.APPEND
        );
    }
}
