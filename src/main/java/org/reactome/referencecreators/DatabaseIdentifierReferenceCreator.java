package org.reactome.referencecreators;

import org.neo4j.driver.v1.Transaction;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.*;

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
            if (ReactomeGraphDatabase.getCurrentTransaction() != null) {
                ReactomeGraphDatabase.commit();
            }
        }
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
}
