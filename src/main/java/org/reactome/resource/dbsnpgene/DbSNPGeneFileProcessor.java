package org.reactome.resource.dbsnpgene;

import org.reactome.resource.UniProtFileProcessor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public class DbSNPGeneFileProcessor extends UniProtFileProcessor {

    public DbSNPGeneFileProcessor(Path filePath) throws IOException {
        super(filePath);
    }
}
