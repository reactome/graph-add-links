package org.reactome.referencecreators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.*;
import org.reactome.graphnodes.ReferenceSequence.ReferenceSequenceType;

import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public class ReferenceSequenceReferenceCreator extends ReferenceCreator {
    private static final Logger logger = LogManager.getLogger();
    private static Map<ReferenceDatabase, Map<String, ReferenceSequence>>
        referenceDatabaseToIdentifierToReferenceSequence = new HashMap<>();

    private ReferenceSequence.ReferenceSequenceType referenceSequenceType;

    public ReferenceSequenceReferenceCreator(
        String referenceName,
        Map<String, Set<String>> uniprotIdentifierToReferenceIdentifiers,
        ReferenceSequenceType referenceSequenceType) throws IllegalArgumentException {

        super(referenceName, uniprotIdentifierToReferenceIdentifiers);
        this.referenceSequenceType = referenceSequenceType;
    }

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
    public void readCSV() throws URISyntaxException {
        final String csvDirectory = getReferenceCreatorCSVDirectory().toString().replace("\\","/");

        logger.info("Creating reference sequences...");
        String nodeCreationQuery = "LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_Identifiers.csv' AS row\n" +
            "CREATE (:ReferenceEntity:ReferenceSequence:Reference" + getReferenceSequenceType().name() + "Sequence:DatabaseObject " +
            "{dbId: toInteger(row.DbId), displayName: row.DisplayName, schemaClass: row.SchemaClass, " +
            "identifier: row.Identifier, geneName: split(row.GeneNames, ';'), databaseName: row.ReferenceDbName, url: row.URL})";
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
    protected List<ReferenceGeneProduct> getIdentifierNodes() {
        return ReferenceGeneProduct.fetchReferenceGeneProductsForUniProtIdentifiers(getUniProtIdentifiers())
            .values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    protected List<ReferenceSequence> fetchExternalIdentifiersForSourceIdentifierNode(IdentifierNode sourceNode) {
        List<String> geneNames = getGeneNames(sourceNode);

        logger.info("Creating reference sequence for " + sourceNode.getIdentifier());
        List<ReferenceSequence> referenceSequences = new ArrayList<>();
        for (String referenceSequenceValue : getIdentifierValues(sourceNode)) {
            referenceSequences.add(fetchReferenceSequence(referenceSequenceValue, geneNames));
        }
        return referenceSequences;
    }

    @Override
    protected String getReferenceCSVHeader() {
        return String.join(",",
            "DbId", "DisplayName", "SchemaClass", "Identifier", "GeneNames", "ReferenceDbName", "URL"
        ).concat(System.lineSeparator());
    }

    @Override
    protected String getExternalIdentifierLine(IdentifierNode externalIdentifier) {
        return String.join(",",
            String.valueOf(externalIdentifier.getDbId()),
            externalIdentifier.getDisplayName(),
            externalIdentifier.getSchemaClass(),
            externalIdentifier.getIdentifier(),
            String.join(";", ((ReferenceSequence) externalIdentifier).getGeneNames()),
            externalIdentifier.getReferenceDatabaseDisplayName(),
            externalIdentifier.getUrl()
        ).concat(System.lineSeparator());
    }

    private Set<String> getUniProtIdentifiers() {
        return getSourceIdentifierToReferenceIdentifiers().keySet();
    }

    private ReferenceSequence fetchReferenceSequence(String identifier, List<String> geneNames) {
        if (!referenceSequenceExistsForIdentifier(identifier)) {
            ReferenceSequence referenceSequence;
            if (getReferenceSequenceType().equals(ReferenceSequenceType.DNA)) {
                referenceSequence = new ReferenceDNASequence(identifier, getReferenceDatabase(), geneNames);
            } else if (getReferenceSequenceType().equals(ReferenceSequenceType.RNA)) {
                referenceSequence = new ReferenceRNASequence(identifier, getReferenceDatabase(), geneNames);
            } else {
                throw new IllegalStateException("Unknown reference sequence type: " + getReferenceSequenceType());
            }
            referenceDatabaseToIdentifierToReferenceSequence
                .computeIfAbsent(getReferenceDatabase(), k -> new HashMap<>())
                .put(identifier, referenceSequence);
        }

        return referenceDatabaseToIdentifierToReferenceSequence
            .get(getReferenceDatabase())
            .get(identifier);
    }

    private boolean referenceSequenceExistsForIdentifier(String identifier) {
        return referenceDatabaseToIdentifierToReferenceSequence
            .computeIfAbsent(getReferenceDatabase(), k -> new HashMap<>())
            .containsKey(identifier);
    }

    private List<String> getGeneNames(IdentifierNode sourceNode) {
        if (!(sourceNode instanceof ReferenceSequence)) {
            return new ArrayList<>();
        }
        return ((ReferenceSequence) sourceNode).getGeneNames();
    }
}
