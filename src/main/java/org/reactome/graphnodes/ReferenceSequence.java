package org.reactome.graphnodes;

import org.reactome.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public abstract class ReferenceSequence extends IdentifierNode {
    private List<String> geneNames;

    protected ReferenceSequence(long dbId, String identifier, String referenceDatabaseName, List<String> geneNames) {
        this(dbId, identifier, geneNames);
        setReferenceDatabase(referenceDatabaseName);
    }

    protected ReferenceSequence(long dbId, String identifier, ReferenceDatabase referenceDatabase, List<String> geneNames) {
        this(dbId, identifier, geneNames);
        setReferenceDatabase(referenceDatabase);
    }

    protected ReferenceSequence(String identifier, ReferenceDatabase referenceDatabase, List<String> geneNames) {
        this(identifier, geneNames);
        setReferenceDatabase(referenceDatabase);
    }

    private ReferenceSequence(long dbId, String identifier, List<String> geneNames) {
        super(dbId, identifier);
        this.identifier = identifier;
        this.geneNames = geneNames != null ? geneNames : new ArrayList<>();
    }

    private ReferenceSequence(String identifier, List<String> geneNames) {
        super(identifier);
        this.identifier = identifier;
        this.geneNames = geneNames != null ? geneNames : new ArrayList<>();
    }

    public List<String> getGeneNames() {
        return this.geneNames;
    }


    public String getDisplayName() {
        String displayName = super.getDisplayName();

        if (getGeneNames() != null && !getGeneNames().isEmpty()) {
            displayName += " " + getGeneNames().get(0);
        }

        return displayName;
    }

    @Override
    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(super.getLabels());
        labels.add("ReferenceEntity");
        labels.add("ReferenceSequence");
        return labels;
    }

    @Override
    protected Set<String> getAttributeKeyValueStrings() {
        Set<String> attributeKeyValueStrings = new LinkedHashSet<>();
        attributeKeyValueStrings.addAll(super.getAttributeKeyValueStrings());
        if (!getGeneNames().isEmpty()){
            attributeKeyValueStrings.add("geneName: " +
                getGeneNames().stream().map(StringUtils::quote).collect(Collectors.toList()));
        }
        return attributeKeyValueStrings;
    }

    public enum ReferenceSequenceType {
        DNA,
        RNA
    }
}
