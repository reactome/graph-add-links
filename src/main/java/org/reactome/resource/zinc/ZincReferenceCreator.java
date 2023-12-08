package org.reactome.resource.zinc;

import org.reactome.referencecreators.ChEBIDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/6/2023
 */
public class ZincReferenceCreator extends ChEBIDatabaseIdentifierReferenceCreator {
    public ZincReferenceCreator(Map<String, Set<String>> chebiIdentifierToReferenceIdentifiers) {
        super("Zinc", chebiIdentifierToReferenceIdentifiers);
    }
}
