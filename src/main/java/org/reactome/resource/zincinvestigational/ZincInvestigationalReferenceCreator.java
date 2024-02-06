package org.reactome.resource.zincinvestigational;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/30/2024
 */
public class ZincInvestigationalReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public ZincInvestigationalReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("ZincInvestigational", uniProtToResourceIdentifiers);
    }
}
