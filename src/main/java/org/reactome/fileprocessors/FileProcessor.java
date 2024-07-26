package org.reactome.fileprocessors;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public interface FileProcessor {
    Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException;
}
