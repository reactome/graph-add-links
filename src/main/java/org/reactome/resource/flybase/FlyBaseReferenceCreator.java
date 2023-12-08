package org.reactome.resource.flybase;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class FlyBaseReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public FlyBaseReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("FlyBase", uniProtToResourceIdentifiers);
    }
}
