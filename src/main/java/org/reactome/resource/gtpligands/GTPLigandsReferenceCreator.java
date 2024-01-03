package org.reactome.resource.gtpligands;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class GuideToPharmacologyLigandsReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public GuideToPharmacologyLigandsReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("GTPLigands", uniProtToResourceIdentifiers);
    }
}
