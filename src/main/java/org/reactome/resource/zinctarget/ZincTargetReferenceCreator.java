package org.reactome.resource.zinctarget;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/30/2024
 */
public class ZincTargetReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public ZincTargetReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("ZincTarget", uniProtToResourceIdentifiers);
    }
}
