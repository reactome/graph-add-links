package org.reactome.referencecreators;

import java.io.IOException;
import java.util.*;

import org.neo4j.driver.v1.Transaction;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.*;
import org.reactome.graphnodes.ReferenceSequence.ReferenceSequenceType;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public class ReferenceSequenceReferenceCreator extends ReferenceCreator {
    private ReferenceSequence.ReferenceSequenceType referenceSequenceType;

    public ReferenceSequenceReferenceCreator(
        String referenceName,
        Map<String, Set<String>> uniprotIdentifierToReferenceIdentifiers,
        ReferenceSequenceType referenceSequenceType) throws IllegalArgumentException {

        super(referenceName, uniprotIdentifierToReferenceIdentifiers);
        this.referenceSequenceType = referenceSequenceType;
    }

//    public List<ReferenceSequence> createReferenceSequenceForReferenceGeneProduct(
//        ReferenceGeneProduct referenceGeneProduct) {
//
//        List<ReferenceSequence> referenceSequences = new ArrayList<>();
//        for (String referenceSequenceValue : getReferenceSequenceValues(referenceGeneProduct)) {
//            referenceSequences.add(createReferenceSequence(referenceSequenceValue));
//        }
//        return referenceSequences;
//    }

    private GraphNode.Relationship getReferenceSequenceRelationship() {
        if (getReferenceSequenceType().equals(ReferenceSequenceType.DNA)) {
            return new GraphNode.Relationship("referenceGene");
        } else if (getReferenceSequenceType().equals(ReferenceSequenceType.RNA)) {
            return new GraphNode.Relationship("referenceTranscript");
        } else {
            throw new IllegalStateException("Unknown reference relationship for type: " + getReferenceSequenceType());
        }
    }

    private ReferenceSequenceType getReferenceSequenceType() {
        return this.referenceSequenceType;
    }

    @Override
    public void insertIdentifiers() {
        System.out.println("Creating reference sequence map");

        Map<IdentifierNode, List<ReferenceSequence>> identifierNodeToReferenceSequencesMap =
            this.createReferenceSequences();

        getReferenceDatabase().insertNode();

        //Transaction transaction = ReactomeGraphDatabase.getSession().beginTransaction();
        for (IdentifierNode identifierNode : identifierNodeToReferenceSequencesMap.keySet()) {
            List<ReferenceSequence> referenceSequences = identifierNodeToReferenceSequencesMap.get(identifierNode);

            System.out.println(identifierNode);
            System.out.println(referenceSequences);
            for (ReferenceSequence referenceSequence : referenceSequences) {
                createNodesWithRelationship(identifierNode, referenceSequence, getReferenceSequenceRelationship());
                createNodesWithRelationship(referenceSequence, getReferenceDatabase(), new GraphNode.Relationship("referenceDatabase"));
                createNodesWithRelationship(referenceSequence, InstanceEdit.get(), new GraphNode.Relationship("created"));

                //referenceGeneProduct.connectTo(databaseIdentifier, new GraphNode.Relationship("crossReference"));
            }
        }
//        if (ReactomeGraphDatabase.getCurrentTransaction() != null) {
//            ReactomeGraphDatabase.commit();
//        }
    }

    @Override
    public void writeCSV() throws IOException {

    }

    @Override
    public void readCSV() {

    }

    private Map<IdentifierNode, List<ReferenceSequence>> createReferenceSequences() {
        Map<IdentifierNode, List<ReferenceSequence>> identifierNodeToReferenceSequences =
            new LinkedHashMap<>();

        for (IdentifierNode identifierNode : getIdentifierNodes()) {
            identifierNodeToReferenceSequences.put(
                identifierNode,
                createReferenceSequencesForReferenceGeneProduct(identifierNode)
            );
        }

        return identifierNodeToReferenceSequences;
    }

    @Override
    protected List<IdentifierNode> getIdentifierNodes() {
        return new ArrayList<>(
            ReferenceGeneProduct.fetchReferenceGeneProductsForUniProtIdentifiers(getUniProtIdentifiers()).values()
        );
    }

    private Set<String> getUniProtIdentifiers() {
        return getSourceIdentifierToReferenceIdentifiers().keySet();
    }

    private List<ReferenceSequence> createReferenceSequencesForReferenceGeneProduct(IdentifierNode identifierNode) {

//        DatabaseIdentifier.DatabaseIdentifierBuilder databaseIdentifierBuilder =
//            new DatabaseIdentifier.DatabaseIdentifierBuilder(getReferenceDatabase());

        System.out.println("Creating reference sequence for " + identifierNode.getIdentifier());
        List<ReferenceSequence> referenceSequences = new ArrayList<>();
        for (String referenceSequenceValue : getIdentifierValues(identifierNode)) {
            referenceSequences.add(createReferenceSequence(referenceSequenceValue));
        }
        return referenceSequences;
    }

    private ReferenceSequence createReferenceSequence(String identifier) {
        List<String> geneNames = new ArrayList<>();
        if (getReferenceSequenceType().equals(ReferenceSequenceType.DNA)) {
            return new ReferenceDNASequence(identifier, getReferenceDatabase(), geneNames);
        } else if (getReferenceSequenceType().equals(ReferenceSequenceType.RNA)) {
            return new ReferenceRNASequence(identifier, getReferenceDatabase(), geneNames);
        } else {
            throw new IllegalStateException("Unknown reference sequence type: " + getReferenceSequenceType());
        }
    }
}
