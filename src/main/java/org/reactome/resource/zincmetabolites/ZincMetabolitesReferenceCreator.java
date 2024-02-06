package org.reactome.resource.zincmetabolites;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/30/2024
 */
public class ZincMetabolitesReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public ZincMetabolitesReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("ZincMetabolites", uniProtToResourceIdentifiers);
    }
}
