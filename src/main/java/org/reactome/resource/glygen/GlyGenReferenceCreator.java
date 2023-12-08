package org.reactome.resource.glygen;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class GlyGenReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public GlyGenReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("GlyGen", uniProtToResourceIdentifiers);
    }
}
