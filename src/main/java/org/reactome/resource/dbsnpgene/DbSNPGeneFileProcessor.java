package org.reactome.resource.dbsnpgene;

import org.reactome.resource.UniProtFileProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.reactome.graphnodes.ReferenceGeneProduct.fetchHumanReferenceGeneProducts;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public class DbSNPGeneFileProcessor extends UniProtFileProcessor {
    private Set<String> humanReferenceGeneProducts;

    public DbSNPGeneFileProcessor(Path filePath) throws IOException {
        super(filePath);
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        Map<String, Set<String>> uniProtToResourceIdentifiers = super.getSourceToResourceIdentifiers();
        uniProtToResourceIdentifiers.entrySet().removeIf(entry -> !isHuman(entry));
        return uniProtToResourceIdentifiers;
    }

    private boolean isHuman(Map.Entry<String, Set<String>> uniProtToResourceIdentifierEntry) {
        String uniProtAccession = uniProtToResourceIdentifierEntry.getKey();
        if (this.humanReferenceGeneProducts == null || this.humanReferenceGeneProducts.isEmpty()) {
            this.humanReferenceGeneProducts = fetchHumanReferenceGeneProducts().keySet();
        }

        return this.humanReferenceGeneProducts.contains(uniProtAccession);
    }
}
