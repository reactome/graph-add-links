package org.reactome.graphnodes;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public class DatabaseIdentifier extends IdentifierNode {
    public DatabaseIdentifier(String identifier, ReferenceDatabase referenceDatabase) {
        super(identifier);
        setReferenceDatabase(referenceDatabase);
    }

    @Override
    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(super.getLabels());
        labels.add("DatabaseIdentifier");
        return labels;
    }

    @Override
    public String getSchemaClass() {
        return "DatabaseIdentifier";
    }
}
