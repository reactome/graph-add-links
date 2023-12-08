package org.reactome.resource.vgnc;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class VGNCReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public VGNCReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("VGNC", uniProtToResourceIdentifiers);
    }
}
