package org.reactome.referencecreators;

import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

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
    public void insertIdentifiers() {
        System.out.println("Creating database identifiers map");

        Map<IdentifierNode, List<DatabaseIdentifier>> identifierNodeToDatabaseIdentifiersMap =
            this.createDatabaseIdentifiers();

        getReferenceDatabase().insertNode();

        for (IdentifierNode identifierNode : identifierNodeToDatabaseIdentifiersMap.keySet()) {
            List<DatabaseIdentifier> databaseIdentifiers = identifierNodeToDatabaseIdentifiersMap.get(identifierNode);

            //System.out.println(identifierNode);
            //System.out.println(databaseIdentifiers);
            //ReactomeGraphDatabase.getSession().beginTransaction();
            for (DatabaseIdentifier databaseIdentifier : databaseIdentifiers) {
                createNodesWithRelationship(identifierNode, databaseIdentifier, new GraphNode.Relationship("crossReference"));
                createNodesWithRelationship(databaseIdentifier, getReferenceDatabase(), new GraphNode.Relationship("referenceDatabase"));
                createNodesWithRelationship(databaseIdentifier, InstanceEdit.get(), new GraphNode.Relationship("created"));

                //referenceGeneProduct.connectTo(databaseIdentifier, new GraphNode.Relationship("crossReference"));
            }
//            if (ReactomeGraphDatabase.getCurrentTransaction() != null) {
//                ReactomeGraphDatabase.commit();
//            }
        }
    }

    public void writeCSV() throws IOException {
        Path databaseIdentifierCSVFilePath = Paths.get("FlyBase_DatabaseIdentifiers.csv");
        writeDatabaseIdentifierCSVHeader(databaseIdentifierCSVFilePath);

        Path relationshipCSVFilePath = Paths.get("FlyBase_Relationships.csv");
        writeRelationshipCSVHeader(relationshipCSVFilePath);
//        Path databaseIdentifierToReferenceDatabaseCSVFilePath = Paths.get("FlyBase_DIToRefDb.csv");
//        Files.write(
//            databaseIdentifierToReferenceDatabaseCSVFilePath,
//            String.join(",",
//                "FirstNodeDbId","SecondNodeDbId"
//            ).concat(System.lineSeparator()).getBytes(),
//            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
//        );
//        Path databaseIdentifierToInstanceEditCSVFilePath = Paths.get("FlyBase_DIToInstanceEdit.csv");
//        Files.write(
//            databaseIdentifierToInstanceEditCSVFilePath,
//            String.join(",",
//                "FirstNodeDbId","SecondNodeDbId"
//            ).concat(System.lineSeparator()).getBytes(),
//            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
//        );


        System.out.println("Creating reference gene product to database identifiers map");

        Map<IdentifierNode, List<DatabaseIdentifier>> identifierNodeToDatabaseIdentifiersMap =
            this.createDatabaseIdentifiers();
        for (IdentifierNode identifierNode : identifierNodeToDatabaseIdentifiersMap.keySet()) {
            List<DatabaseIdentifier> databaseIdentifiers = identifierNodeToDatabaseIdentifiersMap.get(identifierNode);

            for (DatabaseIdentifier databaseIdentifier : databaseIdentifiers) {
                writeDatabaseIdentifierLine(databaseIdentifierCSVFilePath, databaseIdentifier);
                writeRelationshipLine(relationshipCSVFilePath, identifierNode.getDbId(), databaseIdentifier.getDbId(),
                    getReferenceDatabase().getDbId(), InstanceEdit.get().getDbId());


//                Files.write(
//                    databaseIdentifierToReferenceDatabaseCSVFilePath,
//                    String.join(",",
//                        String.valueOf(databaseIdentifier.getDbId()),
//                        String.valueOf(getReferenceDatabase().getDbId())
//                    ).concat(System.lineSeparator()).getBytes(),
//                    StandardOpenOption.APPEND
//                );
//                Files.write(
//                    databaseIdentifierToInstanceEditCSVFilePath,
//                    String.join(",",
//                        String.valueOf(databaseIdentifier.getDbId()),
//                        String.valueOf(InstanceEdit.get().getDbId())
//                    ).concat(System.lineSeparator()).getBytes(),
//                    StandardOpenOption.APPEND
//                );
            }
//            if (ReactomeGraphDatabase.getCurrentTransaction() != null) {
//                ReactomeGraphDatabase.commit();
//            }
        }
    }

    @Override
    public void readCSV() {
        final String csvDirectory = "C:/Users/admin/IdeaProjects/graph-add-links/";

        System.out.println("Creating database identifiers...");
        ReactomeGraphDatabase.getSession().writeTransaction(tx -> {
            tx.run("LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "FlyBase_DatabaseIdentifiers.csv' AS row " +
            "CREATE (:DatabaseIdentifier:DatabaseObject " +
                "{dbId: toInteger(row.DbId), displayName: row.DisplayName, schemaClass: row.SchemaClass, " +
                "identifier: row.Identifier, referenceDatabase: row.ReferenceDbName, url: row.URL})");
            return null;
        });

        System.out.println("Creating relationships...");
        ReactomeGraphDatabase.getSession().writeTransaction(tx -> {
            tx.run("LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/FlyBase_Relationships.csv' AS row " +
                "WITH row LIMIT 100 " +
                "MATCH (rgp:ReferenceGeneProduct {dbId: toInteger(row.RGPDbId)}) " +
                "MATCH (di:DatabaseIdentifier {dbId: toInteger(row.DatabaseIdentifierDbId)}) " +
                "MATCH (rd:ReferenceDatabase {dbId: toInteger(row.ReferenceDatabaseDbId)}) " +
                "MATCH (ie:InstanceEdit {dbId: toInteger(row.InstanceEditDbId)}) " +
                "CREATE (rgp)-[:crossReference]->(di) " +
                "CREATE (di)-[:referenceDatabase]->(rd) " +
                "CREATE (di)-[:created]->(ie)"
            );
            return null;
        });

//        System.out.println("Creating DatabaseIdentifier to ReferenceDatabase relationships");
//        ReactomeGraphDatabase.getSession().writeTransaction(tx -> {
//            tx.run("LOAD CSV WITH HEADERS FROM 'file:///C:/Users/admin/IdeaProjects/graph-add-links/FlyBase_DIToRefDb.csv' AS row " +
//                "WITH row LIMIT 100 " +
//                "MATCH (fn:DatabaseIdentifier {dbId: toInteger(row.FirstNodeDbId)}) " +
//                "MATCH (sn:ReferenceDatabase {dbId: toInteger(row.SecondNodeDbId)}) " +
//                "CREATE (fn)-[:referenceDatabase]->(sn)");
//            return null;
//        });
//
//        System.out.println("Creating DatabaseIdentifier to InstanceEdit relationships");
//        ReactomeGraphDatabase.getSession().writeTransaction(tx -> {
//            tx.run("LOAD CSV WITH HEADERS FROM 'file:///C:/Users/admin/IdeaProjects/graph-add-links/FlyBase_DIToInstanceEdit.csv' AS row " +
//                "WITH row LIMIT 100 " +
//                "MATCH (fn:DatabaseIdentifier {dbId: toInteger(row.FirstNodeDbId)}) " +
//                "MATCH (sn:InstanceEdit {dbId: toInteger(row.SecondNodeDbId)}) " +
//                "CREATE (fn)-[:created]->(sn)");
//            return null;
//        });
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

//        DatabaseIdentifier.DatabaseIdentifierBuilder databaseIdentifierBuilder =
//            new DatabaseIdentifier.DatabaseIdentifierBuilder(getReferenceDatabase());

        List<DatabaseIdentifier> databaseIdentifiers = new ArrayList<>();
        for (String databaseIdentifierValue : getIdentifierValues(identifierNode)) {
            databaseIdentifiers.add(new DatabaseIdentifier(databaseIdentifierValue, getReferenceDatabase()));
        }
        return databaseIdentifiers;
    }

    private void writeDatabaseIdentifierCSVHeader(Path databaseIdentifierCSVFilePath) throws IOException {
        final String header = String.join(",",
            "DbId", "DisplayName", "SchemaClass", "Identifier", "ReferenceDbName", "URL"
        ).concat(System.lineSeparator());

        Files.write(
            databaseIdentifierCSVFilePath,
            header.getBytes(),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private void writeRelationshipCSVHeader(Path rgpToDatabaseIdentifierCSVFilePath) throws IOException {
        final String header = String.join(",",
            "RGPDbId","DatabaseIdentifierDbId","ReferenceDatabaseDbId","InstanceEditDbId"
        ).concat(System.lineSeparator());

        Files.write(
            rgpToDatabaseIdentifierCSVFilePath,
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

    private void writeRelationshipLine(Path relationshipCSVFilePath, Long ...dbIds) throws IOException {
        final String line = Arrays.stream(dbIds)
            .map(String::valueOf).collect(Collectors.joining(",")).concat(System.lineSeparator());

        Files.write(
            relationshipCSVFilePath,
            line.getBytes(),
            StandardOpenOption.APPEND
        );
    }
}
