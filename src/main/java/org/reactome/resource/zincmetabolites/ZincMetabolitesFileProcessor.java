package org.reactome.resource.zincmetabolites;

import org.reactome.fileprocessors.FileProcessor;
import org.reactome.utils.zinc.ZincFileProcessorHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/30/2024
 */
public class ZincMetabolitesFileProcessor implements FileProcessor {
    private ZincFileProcessorHelper zincFileProcessorHelper;

    public ZincMetabolitesFileProcessor(Path filePath) throws IOException {
        this.zincFileProcessorHelper = ZincFileProcessorHelper.get("ZincMetabolites", filePath);
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        return getZincFileProcessorHelper().getSourceToResourceIdentifiers();
    }

    private ZincFileProcessorHelper getZincFileProcessorHelper() {
        return this.zincFileProcessorHelper;
    }
}
