package org.reactome.referencecreators;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

//    @Override
//    public void insertIdentifiers() {
//        System.out.println("Creating reference sequence map");
//
//        Map<IdentifierNode, List<ReferenceSequence>> identifierNodeToReferenceSequencesMap =
//            this.createReferenceSequences();
//
//        getReferenceDatabase().insertNode();
//
//        for (IdentifierNode identifierNode : identifierNodeToReferenceSequencesMap.keySet()) {
//            List<ReferenceSequence> referenceSequences = identifierNodeToReferenceSequencesMap.get(identifierNode);
//
//            System.out.println(identifierNode);
//            System.out.println(referenceSequences);
//            for (ReferenceSequence referenceSequence : referenceSequences) {
//                createNodesWithRelationship(identifierNode, referenceSequence, getReferenceSequenceRelationship());
//                createNodesWithRelationship(referenceSequence, getReferenceDatabase(), new GraphNode.Relationship("referenceDatabase"));
//                createNodesWithRelationship(referenceSequence, InstanceEdit.get(), new GraphNode.Relationship("created"));
//
//            }
//        }
//    }

    @Override
    public void writeCSV() throws IOException, URISyntaxException {
        Path resourceDirectory = getReferenceCreatorCSVDirectory();

        Path referenceSequenceCSVFilePath = resourceDirectory.resolve(getResourceName() + "_ReferenceSequences.csv");
        writeReferenceCSVHeader(referenceSequenceCSVFilePath);

        Path relationshipCSVFilePath = resourceDirectory.resolve(getResourceName() + "_Relationships.csv");
        writeRelationshipCSVHeader(relationshipCSVFilePath);

        System.out.println("Creating source to reference sequences map...");

        Map<IdentifierNode, List<ReferenceSequence>> identifierNodeToReferenceSequencesMap =
            this.createReferenceSequences();

        System.out.println("Source to reference sequences map created");

        System.out.println("Writing CSV data for identifier nodes");
        for (IdentifierNode identifierNode : identifierNodeToReferenceSequencesMap.keySet()) {
            List<ReferenceSequence> referenceSequences = identifierNodeToReferenceSequencesMap.get(identifierNode);

            for (ReferenceSequence referenceSequence : referenceSequences) {
                writeReferenceSequenceLine(referenceSequenceCSVFilePath, referenceSequence);
                writeRelationshipLine(relationshipCSVFilePath, identifierNode.getDbId(), referenceSequence.getDbId(),
                    getReferenceDatabase().getDbId(), InstanceEdit.get().getDbId());
            }
        }
        System.out.println("CSV data complete");
    }

    @Override
    public void readCSV() throws URISyntaxException {
        final String csvDirectory = getReferenceCreatorCSVDirectory().toString().replace("\\","/");

        System.out.println("Creating reference sequences...");
        ReactomeGraphDatabase.getSession().writeTransaction(tx -> {
            tx.run("LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_ReferenceSequences.csv' AS row " +
                "CREATE (:ReferenceEntity:ReferenceSequence:Reference" + getReferenceSequenceType().name() + "Sequence:DatabaseObject " +
                "{dbId: toInteger(row.DbId), displayName: row.DisplayName, schemaClass: row.SchemaClass, " +
                "identifier: row.Identifier, referenceDatabase: row.ReferenceDbName, url: row.URL})");
            return null;
        });

        System.out.println("Creating relationships...");
        ReactomeGraphDatabase.getSession().run(
            "LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_Relationships.csv' AS row " +
            "MATCH (do:DatabaseObject {dbId: toInteger(row.SourceDbId)}) " +
            "MATCH (rs:ReferenceSequence {dbId: toInteger(row.ReferenceSequenceDbId)}) " +
            "MATCH (rd:ReferenceDatabase {dbId: toInteger(row.ReferenceDatabaseDbId)}) " +
            "MATCH (ie:InstanceEdit {dbId: toInteger(row.InstanceEditDbId)}) " +
            "CREATE (do)-" + getReferenceSequenceRelationship() + "->(rs) " +
            "CREATE (rs)-[:referenceDatabase]->(rd) " +
            "CREATE (rs)-[:created]->(ie)"
        );
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

    private void writeRelationshipCSVHeader(Path sourceToDatabaseIdentifierCSVFilePath) throws IOException {
        final String header = String.join(",",
            "SourceDbId","ReferenceSequenceDbId","ReferenceDatabaseDbId","InstanceEditDbId"
        ).concat(System.lineSeparator());

        Files.write(
            sourceToDatabaseIdentifierCSVFilePath,
            header.getBytes(),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private void writeReferenceSequenceLine(Path referenceSequenceCSVFilePath, ReferenceSequence referenceSequence)
        throws IOException {

        final String line = String.join(",",
            String.valueOf(referenceSequence.getDbId()),
            referenceSequence.getDisplayName(),
            referenceSequence.getSchemaClass(),
            referenceSequence.getIdentifier(),
            referenceSequence.getReferenceDatabaseDisplayName(),
            referenceSequence.getUrl()
        ).concat(System.lineSeparator());

        Files.write(
            referenceSequenceCSVFilePath,
            line.getBytes(),
            StandardOpenOption.APPEND
        );
    }
}
