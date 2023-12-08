package org.reactome.graphnodes;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/6/2023
 */
public class Reaction extends IdentifierNode {
    public Reaction(long dbId, String identifier) {
        super(dbId, identifier);
    }

    @Override
    public String getSchemaClass() {
        return "Reaction";
    }

    @Override
    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(super.getLabels());
        labels.add(getSchemaClass());
        return labels;
    }
}
