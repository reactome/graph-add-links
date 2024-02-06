package org.reactome.resource.zincfdaapproved;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/30/2024
 */
public class ZincFDAApprovedReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public ZincFDAApprovedReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("ZincFDAApproved", uniProtToResourceIdentifiers);
    }
}
