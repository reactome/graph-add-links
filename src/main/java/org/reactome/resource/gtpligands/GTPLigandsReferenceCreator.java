package org.reactome.resource.gtpligands;

import org.reactome.referencecreators.ChEBIDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class GTPLigandsReferenceCreator extends ChEBIDatabaseIdentifierReferenceCreator {

    public GTPLigandsReferenceCreator(Map<String, Set<String>> chEBIToResourceIdentifiers) {
        super("GTPLigands", chEBIToResourceIdentifiers);
    }
}
