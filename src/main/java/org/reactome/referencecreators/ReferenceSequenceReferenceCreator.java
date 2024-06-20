package org.reactome.referencecreators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.*;
import org.reactome.graphnodes.ReferenceSequence.ReferenceSequenceType;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public class ReferenceSequenceReferenceCreator extends ReferenceCreator {
    private static final Logger logger = LogManager.getLogger();

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

//    @Override
//    public void writeCSV() throws IOException, URISyntaxException {
//        Path resourceDirectory = getReferenceCreatorCSVDirectory();
//
//        Files.createDirectories(resourceDirectory);
//
//        writeReferenceCSVHeader(getIdentifierCSVFilePath());
//        writeRelationshipCSVHeader(getRelationshipCSVFilePath());
//
//        logger.info("Creating source to reference sequences map...");
//
//        Map<IdentifierNode, List<? extends IdentifierNode>> identifierNodeToReferenceSequencesMap =
//            this.createIdentifiers();
//
//        logger.info("Source to reference sequences map created");
//
//        logger.info("Writing CSV data for identifier nodes");
//        for (IdentifierNode identifierNode : identifierNodeToReferenceSequencesMap.keySet()) {
//            List<? extends IdentifierNode> referenceSequences = identifierNodeToReferenceSequencesMap.get(identifierNode);
//            logger.debug("Identifier Node: " + identifierNode);
//            logger.debug("Reference Sequences: " + referenceSequences);
//
//            for (ReferenceSequence referenceSequence : referenceSequences) {
//                writeExternalIdentifierLine(referenceSequence);
//                writeRelationshipLine(identifierNode.getDbId(), referenceSequence.getDbId(),
//                    getReferenceDatabase().getDbId(), InstanceEdit.get().getDbId());
//            }
//        }
//        logger.info("CSV data complete");
//    }

    @Override
    public void readCSV() throws URISyntaxException {
        final String csvDirectory = getReferenceCreatorCSVDirectory().toString().replace("\\","/");

//        System.out.println("Creating indexes...");
//        ReactomeGraphDatabase.getSession().run("CREATE INDEX ON :ReferenceSequence(dbId) IF NOT EXISTS");
//        ReactomeGraphDatabase.getSession().run("CREATE INDEX ON :DatabaseObject(dbId) IF NOT EXISTS");
//        ReactomeGraphDatabase.getSession().run("CREATE INDEX ON :ReferenceDatabase(dbId) IF NOT EXISTS");
//        ReactomeGraphDatabase.getSession().run("CREATE INDEX ON :InstanceEdit(dbId) IF NOT EXISTS");

        logger.info("Creating reference sequences...");
        String nodeCreationQuery = "LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_Identifiers.csv' AS row\n" +
            "CREATE (:ReferenceEntity:ReferenceSequence:Reference" + getReferenceSequenceType().name() + "Sequence:DatabaseObject " +
            "{dbId: toInteger(row.DbId), displayName: row.DisplayName, schemaClass: row.SchemaClass, " +
            "identifier: row.Identifier, databaseName: row.ReferenceDbName, url: row.URL})";
        ReactomeGraphDatabase.getSession().writeTransaction(tx -> {
            tx.run(nodeCreationQuery);
            return null;
        });

        logger.info("Creating relationships...");

        String relationshipCreationQuery =
            "USING PERIODIC COMMIT 100\n" +
            "LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_Relationships.csv' AS row\n" +
            "\tMATCH (do:DatabaseObject {dbId: toInteger(row.SourceDbId)})\n" +
            "\tMATCH (rs:ReferenceSequence {dbId: toInteger(row.ExternalIdentifierDbId)})\n" +
            "\tMATCH (rd:ReferenceDatabase {dbId: toInteger(row.ReferenceDatabaseDbId)})\n" +
            "\tMATCH (ie:InstanceEdit {dbId: toInteger(row.InstanceEditDbId)})\n" +
            "\tCREATE (do)-" + getReferenceSequenceRelationship() + "->(rs)\n" +
            "\tCREATE (rs)-[:referenceDatabase]->(rd)\n" +
            "\tCREATE (rs)-[:created]->(ie)";
        logger.info("Running query\n" + relationshipCreationQuery);
        ReactomeGraphDatabase.getSession().run(relationshipCreationQuery);
    }

    @Override
    protected List<IdentifierNode> getIdentifierNodes() {
        return new ArrayList<>(
            ReferenceGeneProduct.fetchReferenceGeneProductsForUniProtIdentifiers(getUniProtIdentifiers()).values()
        );
    }

    @Override
    protected List<? extends IdentifierNode> createExternalIdentifiersForIdentifierNode(IdentifierNode identifierNode) {

//        DatabaseIdentifier.DatabaseIdentifierBuilder databaseIdentifierBuilder =
//            new DatabaseIdentifier.DatabaseIdentifierBuilder(getReferenceDatabase());

        logger.info("Creating reference sequence for " + identifierNode.getIdentifier());
        List<ReferenceSequence> referenceSequences = new ArrayList<>();
        for (String referenceSequenceValue : getIdentifierValues(identifierNode)) {
            referenceSequences.add(createReferenceSequence(referenceSequenceValue));
        }
        return referenceSequences;
    }

//    private Map<IdentifierNode, List<ReferenceSequence>> createReferenceSequences() {
//        Map<IdentifierNode, List<ReferenceSequence>> identifierNodeToReferenceSequences =
//            new LinkedHashMap<>();
//
//        for (IdentifierNode identifierNode : getIdentifierNodes()) {
//            identifierNodeToReferenceSequences.put(
//                identifierNode,
//                createReferenceSequencesForReferenceGeneProduct(identifierNode)
//            );
//        }
//
//        return identifierNodeToReferenceSequences;
//    }

    private Set<String> getUniProtIdentifiers() {
        return getSourceIdentifierToReferenceIdentifiers().keySet();
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

//    private Path getReferenceSequenceCSVFilePath() throws URISyntaxException {
//        return getReferenceCreatorCSVDirectory().resolve(getResourceName() + "_ReferenceSequences.csv");
//    }
}
