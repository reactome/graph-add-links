package org.reactome.referencecreators;

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
    }

    @Override
    public void readCSV() throws URISyntaxException {
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
        ReactomeGraphDatabase.getSession().run(
            "LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_Relationships.csv' AS row " +
            "MATCH (do:DatabaseObject {dbId: toInteger(row.SourceDbId)}) " +
            "MATCH (di:DatabaseIdentifier {dbId: toInteger(row.DatabaseIdentifierDbId)}) " +
            "MATCH (rd:ReferenceDatabase {dbId: toInteger(row.ReferenceDatabaseDbId)}) " +
            "MATCH (ie:InstanceEdit {dbId: toInteger(row.InstanceEditDbId)}) " +
            "CREATE (do)-[:crossReference]->(di) " +
            "CREATE (di)-[:referenceDatabase]->(rd) " +
            "CREATE (di)-[:created]->(ie)"
        );
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
