package org.reactome.resource.zfin;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class ZFINReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public ZFINReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("ZFIN", uniProtToResourceIdentifiers);
    }
}
