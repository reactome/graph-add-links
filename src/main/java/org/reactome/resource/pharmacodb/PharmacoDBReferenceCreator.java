package org.reactome.resource.pharmacodb;

import org.reactome.referencecreators.GuideToPharmacologyDatabaseIdentifierReferenceCreator;
import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class PharmacoDBReferenceCreator extends GuideToPharmacologyDatabaseIdentifierReferenceCreator {

    public PharmacoDBReferenceCreator(Map<String, Set<String>> guideToPharmacologyToResourceIdentifiers) {
        super("PharmacoDB", guideToPharmacologyToResourceIdentifiers);
    }
}
