package org.reactome.resource.pharosligands;

import org.reactome.referencecreators.GuideToPharmacologyDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class PharosLigandsReferenceCreator extends GuideToPharmacologyDatabaseIdentifierReferenceCreator {

    public PharosLigandsReferenceCreator(Map<String, Set<String>> guideToPharmacologyToResourceIdentifiers) {
        super("PharosLigands", guideToPharmacologyToResourceIdentifiers);
    }
}
