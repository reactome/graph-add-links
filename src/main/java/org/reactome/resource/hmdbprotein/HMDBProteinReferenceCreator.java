package org.reactome.resource.hmdbprotein;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class HMDBProteinReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public HMDBProteinReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("HMDBProtein", uniProtToResourceIdentifiers);
    }
}
