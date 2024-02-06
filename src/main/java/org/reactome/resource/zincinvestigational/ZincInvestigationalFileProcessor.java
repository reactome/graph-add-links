package org.reactome.resource.zincinvestigational;

import org.reactome.resource.FileProcessor;
import org.reactome.utils.zinc.ZincFileProcessorHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/30/2024
 */
public class ZincInvestigationalFileProcessor implements FileProcessor {
    private ZincFileProcessorHelper zincFileProcessorHelper;

    public ZincInvestigationalFileProcessor(Path filePath) throws IOException {
        this.zincFileProcessorHelper = ZincFileProcessorHelper.get("ZincInvestigational", filePath);
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        return getZincFileProcessorHelper().getSourceToResourceIdentifiers();
    }

    private ZincFileProcessorHelper getZincFileProcessorHelper() {
        return this.zincFileProcessorHelper;
    }
}
