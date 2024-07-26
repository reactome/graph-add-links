package org.reactome.resource.zincpredictionspurchasable;

import org.reactome.fileprocessors.FileProcessor;
import org.reactome.utils.zinc.ZincFileProcessorHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/30/2024
 */
public class ZincPredictionsPurchasableFileProcessor implements FileProcessor {
    private ZincFileProcessorHelper zincFileProcessorHelper;

    public ZincPredictionsPurchasableFileProcessor(Path filePath) throws IOException {
        this.zincFileProcessorHelper = ZincFileProcessorHelper.get("ZincPredictionsPurchasable", filePath);
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        return getZincFileProcessorHelper().getSourceToResourceIdentifiers();
    }

    private ZincFileProcessorHelper getZincFileProcessorHelper() {
        return this.zincFileProcessorHelper;
    }}
