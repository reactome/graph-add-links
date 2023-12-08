package org.reactome.graphnodes;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public class ReferenceRNASequence extends ReferenceSequence {

    public ReferenceRNASequence(String identifier, ReferenceDatabase referenceDatabase, List<String> geneNames) {
        super(identifier, referenceDatabase, geneNames);
    }

    @Override
    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(super.getLabels());
        labels.add("ReferenceRNASequence");
        return labels;
    }

    @Override
    public String getSchemaClass() {
        return "ReferenceRNASequence";
    }
}
