package org.reactome.resource.zincpredictionspurchasable;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/30/2024
 */
public class ZincPredictionsPurchasableReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public ZincPredictionsPurchasableReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("ZincPredictionsPurchasable", uniProtToResourceIdentifiers);
    }
}
