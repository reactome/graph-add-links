package org.reactome.graphnodes;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/30/2023
 */
public class Complex extends IdentifierNode {
    public Complex(long dbId, String identifier) {
        super(dbId, identifier);
    }

    @Override
    public String getSchemaClass() {
        return "Complex";
    }

    @Override
    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(super.getLabels());
        labels.add(getSchemaClass());
        return labels;
    }

}
