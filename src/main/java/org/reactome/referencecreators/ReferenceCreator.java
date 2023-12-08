package org.reactome.referencecreators;

import org.neo4j.driver.v1.Transaction;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.GraphNode;
import org.reactome.graphnodes.IdentifierNode;
import org.reactome.graphnodes.ReferenceDatabase;
import org.reactome.graphnodes.ReferenceGeneProduct;

import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/5/2022
 */
public abstract class ReferenceCreator {
    private ReferenceDatabase referenceDatabase;
    private Map<String, Set<String>> sourceIdentifierToReferenceIdentifiers;

    public ReferenceCreator(String referenceName, Map<String, Set<String>> uniprotIdentifierToReferenceIdentifiers)
        throws IllegalArgumentException {

        this.referenceDatabase = ReferenceDatabase.parseReferenceDatabase(referenceName);
        this.sourceIdentifierToReferenceIdentifiers = uniprotIdentifierToReferenceIdentifiers;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return this.referenceDatabase;
    }

    public Map<String, Set<String>> getSourceIdentifierToReferenceIdentifiers() {
        return this.sourceIdentifierToReferenceIdentifiers;
    }

    public abstract void insertIdentifiers();

//    protected Set<String> getUniProtIdentifiers() {
//        return getSourceIdentifierToReferenceIdentifiers().keySet();
//    }

    protected Set<String> getIdentifierValues(IdentifierNode identifierNode) {
        return getIdentifierValues(identifierNode.getIdentifier());
    }

    protected Set<String> getIdentifierValues(String sourceIdentifier) {
        return getSourceIdentifierToReferenceIdentifiers()
            .computeIfAbsent(sourceIdentifier, k -> new HashSet<>());
    }

    protected abstract List<IdentifierNode> getIdentifierNodes();

//    protected List<ReferenceGeneProduct> getReferenceGeneProducts() {
//      //  System.out.println(getUniProtIdentifiers().size());
//      //  return new ArrayList<>();
//        return new ArrayList<>(
//            ReferenceGeneProduct.fetchReferenceGeneProductsForUniProtIdentifiers(getUniProtIdentifiers()).values()
//        );
//    }

    protected static void createNodesWithRelationship(
        GraphNode fromNode, GraphNode toNode, GraphNode.Relationship relationship) {

        fromNode.insertNode();
        toNode.insertNode();

        String createQuery = "MATCH (fn:" + fromNode.getSchemaClass() + "{dbId: " + fromNode.getDbId() + "})" +
            " MATCH (tn: " + toNode.getSchemaClass() + "{dbId: " + toNode.getDbId() + "})" +
            " CREATE (fn)-"+ relationship + "->(tn)";
        System.out.println(createQuery);
        ReactomeGraphDatabase.queue(createQuery);
    }
}
