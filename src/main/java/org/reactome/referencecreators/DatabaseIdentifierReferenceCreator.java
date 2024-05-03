package org.reactome.referencecreators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public abstract class DatabaseIdentifierReferenceCreator extends ReferenceCreator {
    private static final Logger logger = LogManager.getLogger();

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
    public void readCSV() throws URISyntaxException, IOException {
        final String csvDirectory = getReferenceCreatorCSVDirectory().toString().replace("\\","/");

        logger.info("Creating database identifiers...");
        String nodeCreationQuery = "LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_Identifiers.csv' AS row\n" +
            "CREATE (:DatabaseIdentifier:DatabaseObject " +
            "{dbId: toInteger(row.DbId), displayName: row.DisplayName, schemaClass: row.SchemaClass, " +
            "identifier: row.Identifier, referenceDatabase: row.ReferenceDbName, url: row.URL})";
        logger.info("Running query \n" + nodeCreationQuery);
        ReactomeGraphDatabase.getSession().writeTransaction(tx -> {
            tx.run(nodeCreationQuery);
            return null;
        });

        logger.info("Creating relationships...");
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

        String relationshipCreationQuery =
            "USING PERIODIC COMMIT 100\n" +
            "LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_Relationships.csv' AS row\n" +
            "MATCH (do:DatabaseObject {dbId: toInteger(row.SourceDbId)})\n" +
            "MATCH (di:DatabaseIdentifier {dbId: toInteger(row.ExternalIdentifierDbId)})\n" +
            "MATCH (rd:ReferenceDatabase {dbId: toInteger(row.ReferenceDatabaseDbId)})\n" +
            "MATCH (ie:InstanceEdit {dbId: toInteger(row.InstanceEditDbId)})\n" +
            "CREATE (do)-[:crossReference]->(di)\n" +
            "CREATE (di)-[:referenceDatabase]->(rd)\n" +
            "CREATE (di)-[:created]->(ie) ";

        logger.info("Running query \n" + relationshipCreationQuery);
        //StatementResult statementResult =
            ReactomeGraphDatabase.getSession().run(relationshipCreationQuery);
//        if (statementResult.hasNext()) {
//            System.out.println(statementResult.next());
//        }
    }

//    protected void writeDatabaseIdentifierLine(DatabaseIdentifier databaseIdentifier)
//        throws IOException, URISyntaxException {
//
//        final String line = String.join(",",
//            String.valueOf(databaseIdentifier.getDbId()),
//            databaseIdentifier.getDisplayName(),
//            databaseIdentifier.getSchemaClass(),
//            databaseIdentifier.getIdentifier(),
//            databaseIdentifier.getReferenceDatabaseDisplayName(),
//            databaseIdentifier.getUrl()
//        ).concat(System.lineSeparator());
//
//        Files.write(
//            getDatabaseIdentifierCSVFilePath(),
//            line.getBytes(),
//            StandardOpenOption.APPEND
//        );
//    }

//    private Path getDatabaseIdentifierCSVFilePath() throws URISyntaxException {
//        return getReferenceCreatorCSVDirectory().resolve(getResourceName() + "_DatabaseIdentifiers.csv");
//    }

    @Override
    protected List<? extends IdentifierNode> createExternalIdentifiersForIdentifierNode(IdentifierNode identifierNode) {
        List<DatabaseIdentifier> databaseIdentifiers = new ArrayList<>();
        for (String databaseIdentifierValue : getIdentifierValues(identifierNode)) {
            databaseIdentifiers.add(new DatabaseIdentifier(databaseIdentifierValue, getReferenceDatabase()));
        }
        return databaseIdentifiers;
    }
}
