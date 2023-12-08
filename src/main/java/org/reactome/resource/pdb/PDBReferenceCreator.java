package org.reactome.resource.pdb;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class PDBReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public PDBReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("PDB", uniProtToResourceIdentifiers);
    }
}
