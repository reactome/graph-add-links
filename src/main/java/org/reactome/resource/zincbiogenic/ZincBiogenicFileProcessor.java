package org.reactome.resource.zincbiogenic;

import org.reactome.resource.FileProcessor;
import org.reactome.utils.zinc.ZincFileProcessorHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/30/2024
 */
public class ZincBiogenicFileProcessor implements FileProcessor {

    private ZincFileProcessorHelper zincFileProcessorHelper;

    public ZincBiogenicFileProcessor(Path filePath) throws IOException {
        this.zincFileProcessorHelper = ZincFileProcessorHelper.get("ZincBiogenic", filePath);
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        return getZincFileProcessorHelper().getSourceToResourceIdentifiers();
    }

    private ZincFileProcessorHelper getZincFileProcessorHelper() {
        return this.zincFileProcessorHelper;
    }
}
