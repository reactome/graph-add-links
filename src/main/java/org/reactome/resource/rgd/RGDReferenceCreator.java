package org.reactome.resource.rgd;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class RGDReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public RGDReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("RGD", uniProtToResourceIdentifiers);
    }
}
