package org.reactome.resource.genecards;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class GeneCardsReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public GeneCardsReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("GeneCards", uniProtToResourceIdentifiers);
    }
}
