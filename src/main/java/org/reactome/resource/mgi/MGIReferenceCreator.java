package org.reactome.resource.mgi;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class MGIReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public MGIReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("MGI", uniProtToResourceIdentifiers);
    }
}
