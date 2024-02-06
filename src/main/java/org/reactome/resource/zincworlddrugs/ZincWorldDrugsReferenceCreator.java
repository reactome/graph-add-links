package org.reactome.resource.zincworlddrugs;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/30/2024
 */
public class ZincWorldDrugsReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public ZincWorldDrugsReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("ZincWorldDrugs", uniProtToResourceIdentifiers);
    }
}
