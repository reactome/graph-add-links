package org.reactome.referencecreators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.*;

import java.io.IOException;
import java.net.URISyntaxException;
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

    @Override
    public void readCSV() throws URISyntaxException, IOException {
        final String csvDirectory = getReferenceCreatorCSVDirectory().toString().replace("\\","/");

        logger.info("Creating database identifiers...");
        String nodeCreationQuery = "LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_Identifiers.csv' AS row\n" +
            "CREATE (:DatabaseIdentifier:DatabaseObject " +
            "{dbId: toInteger(row.DbId), displayName: row.DisplayName, schemaClass: row.SchemaClass, " +
            "identifier: row.Identifier, databaseName: row.ReferenceDbName, url: row.URL})";
        logger.info("Running query \n" + nodeCreationQuery);
        ReactomeGraphDatabase.getSession().writeTransaction(tx -> {
            tx.run(nodeCreationQuery);
            return null;
        });

        logger.info("Creating relationships...");

        String relationshipCreationQuery =
            "USING PERIODIC COMMIT 100\n" +
            "LOAD CSV WITH HEADERS FROM 'file:///" + csvDirectory + "/" + getResourceName() + "_Relationships.csv' AS row\n" +
            "MATCH (do:DatabaseObject {dbId: toInteger(row.SourceDbId)})\n" +
            "MATCH (di:DatabaseIdentifier {dbId: toInteger(row.ExternalIdentifierDbId)})\n" +
            "MATCH (rd:ReferenceDatabase {dbId: toInteger(row.ReferenceDatabaseDbId)})\n" +
            "MATCH (ie:InstanceEdit {dbId: toInteger(row.InstanceEditDbId)})\n" +
            "MERGE (do)-[crr:crossReference]->(di)\n" +
            "ON CREATE SET crr.order = 0, crr.stoichiometry = 1\n" +
            "MERGE (di)-[rdr:referenceDatabase]->(rd)\n" +
            "ON CREATE SET rdr.order = 0, rdr.stoichiometry = 1\n" +
            "MERGE (di)-[cr:created]->(ie)\n" +
            "ON CREATE SET cr.order = 0, cr.stoichiometry = 1\n";

        logger.info("Running query \n" + relationshipCreationQuery);

        ReactomeGraphDatabase.getSession().run(relationshipCreationQuery);
    }

    @Override
    protected List<? extends IdentifierNode> fetchExternalIdentifiersForSourceIdentifierNode(IdentifierNode sourceNode) {
        List<DatabaseIdentifier> databaseIdentifiers = new ArrayList<>();
        for (String databaseIdentifierValue : getIdentifierValues(sourceNode)) {
            databaseIdentifiers.add(DatabaseIdentifier.fetchOrCreate(databaseIdentifierValue, getReferenceDatabase()));
        }
        return databaseIdentifiers;
    }
}
