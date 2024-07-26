package org.reactome.resource.dbsnpgene;


import org.reactome.retrievers.SingleFileUniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class DbSNPGeneFileRetriever extends SingleFileUniProtMapperRetriever {

    public DbSNPGeneFileRetriever() {
        super("DbSNPGene");
    }
}
