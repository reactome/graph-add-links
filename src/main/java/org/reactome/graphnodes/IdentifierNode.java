package org.reactome.graphnodes;

import java.util.*;

import static org.reactome.utils.StringUtils.quote;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public abstract class IdentifierNode extends GraphNode {
    protected String identifier;
    private ReferenceDatabase referenceDatabase;

    public IdentifierNode(String identifier) {
        setIdentifier(identifier);
    }

    public IdentifierNode(long dbId, String identifier) {
        super(dbId);
        setIdentifier(identifier);
    }

    protected void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    protected void setReferenceDatabase(ReferenceDatabase referenceDatabase) {
        this.referenceDatabase = referenceDatabase;
    }

    protected void setReferenceDatabase(String referenceDatabaseName) {
        this.referenceDatabase = ReferenceDatabase.parseReferenceDatabase(referenceDatabaseName);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return this.referenceDatabase;
    }

    public String getDisplayName() {
        return getReferenceDatabaseDisplayName() + ":" + getIdentifier();
    }

    @Override
    protected Set<String> getAttributeKeyValueStrings() {
        Set<String> attributeKeyValueStrings = new LinkedHashSet<>();
        attributeKeyValueStrings.addAll(super.getAttributeKeyValueStrings());
        attributeKeyValueStrings.addAll(
            Arrays.asList(
                "databaseName: " + quote(getReferenceDatabaseDisplayName()),
                "identifier: " + quote(getIdentifier()),
                "url: " + quote(getUrl())
            )
        );
        return attributeKeyValueStrings;
    }

    public String getReferenceDatabaseDisplayName() {
        return getReferenceDatabase() != null ?
            getReferenceDatabase().getDisplayName() : "N/A";
    }

    public String getUrl() {
        if (getReferenceDatabase() == null) {
            return "N/A";
        }

        return getReferenceDatabase().getAccessURL().replace("###ID###", getIdentifier());
    }

}
