package org.reactome.resource.xenbase;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class XenbaseReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public XenbaseReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("Xenbase", uniProtToResourceIdentifiers);
    }
}
