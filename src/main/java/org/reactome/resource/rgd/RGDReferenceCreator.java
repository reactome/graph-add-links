package org.reactome.resource.rgd;

import org.neo4j.driver.Result;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.DatabaseIdentifier;
import org.reactome.graphnodes.IdentifierNode;
import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class RGDReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {
//    private Set<String> existingRGDIdentifiers;

    public RGDReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("RGD", uniProtToResourceIdentifiers);
    }

//    @Override
//    protected void writeCSVForDatabaseIdentifier(DatabaseIdentifier databaseIdentifier, IdentifierNode identifierNode)
//        throws IOException, URISyntaxException {
//
//        if (!existsInDatabase(databaseIdentifier)) {
//            super.writeCSVForDatabaseIdentifier(databaseIdentifier, identifierNode);
//        }
//    }

//    private boolean existsInDatabase(DatabaseIdentifier databaseIdentifier) {
//        if (this.existingRGDIdentifiers == null || this.existingRGDIdentifiers.isEmpty()) {
//            String rgdIdentifiersQuery =
//                "MATCH (di:DatabaseIdentifier)-[:referenceDatabase]->(rd:ReferenceDatabase)" +
//                    " WHERE rd.displayName = \"RGD\"" +
//                    " RETURN di.identifier";
//            Result rgdIdentifiersQueryResult = ReactomeGraphDatabase.getSession().run(rgdIdentifiersQuery);
//            this.existingRGDIdentifiers = rgdIdentifiersQueryResult
//                .stream()
//                .map(record -> record.get(0).asString())
//                .collect(Collectors.toSet());
//        }
//
//        return this.existingRGDIdentifiers.contains(databaseIdentifier.getIdentifier());
//    }
}
