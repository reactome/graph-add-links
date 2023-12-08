package org.reactome.resource.opentargets;

import org.reactome.referencecreators.DatabaseIdentifierReferenceCreator;
import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class OpenTargetsReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public OpenTargetsReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("OpenTargets", uniProtToResourceIdentifiers);
    }
}
