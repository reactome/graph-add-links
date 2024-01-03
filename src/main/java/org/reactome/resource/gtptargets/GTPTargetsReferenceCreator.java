package org.reactome.resource.gtptargets;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class GuideToPharmacologyTargetsReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public GuideToPharmacologyTargetsReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("GTPTargets", uniProtToResourceIdentifiers);
    }
}
