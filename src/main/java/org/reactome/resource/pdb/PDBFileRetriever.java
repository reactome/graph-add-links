package org.reactome.resource.pdb;

import org.reactome.resource.SingleFileUniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class PDBFileRetriever extends SingleFileUniProtMapperRetriever {

    public PDBFileRetriever() {
        super("PDB");
    }
}
