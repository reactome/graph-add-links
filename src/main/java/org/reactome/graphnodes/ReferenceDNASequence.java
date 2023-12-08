package org.reactome.graphnodes;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public class ReferenceDNASequence extends ReferenceSequence {

    public ReferenceDNASequence(String identifier, ReferenceDatabase referenceDatabase, List<String> geneNames) {
        super(identifier, referenceDatabase, geneNames);
    }

    @Override
    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(super.getLabels());
        labels.add("ReferenceDNASequence");
        return labels;
    }

    @Override
    public String getSchemaClass() {
        return "ReferenceDNASequence";
    }
}
