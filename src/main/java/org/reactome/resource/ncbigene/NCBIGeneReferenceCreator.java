package org.reactome.resource.ncbigene;

import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class NCBIGeneReferenceCreator extends ReferenceSequenceReferenceCreator {

    public NCBIGeneReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("NCBIGene", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }

//    public void loadCSV(Path file) {
//        String query = "LOAD CSV WITH HEADERS FROM 'file:///" + file.toString() + "' AS row" +
//            "MATCH (rgp:ReferenceGeneProduct) {identifier: row.From}" +
//            "MERGE (rpg)-[:referenceGene]-(:ReferenceDNASequence:ReferenceSequence:ReferenceEntity {identifier: row.To, geneNames: rgp.geneNames, })";
//
//        ReactomeGraphDatabase.getSession().writeTransaction(transaction -> {
//            transaction.run(query);
//            return null;
//        });
//    }
}
