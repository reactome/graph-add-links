package org.reactome.graphnodes;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public class DatabaseIdentifier extends IdentifierNode {
    private static Map<ReferenceDatabase, Map<String, DatabaseIdentifier>> refDbToIdentifierToDatabaseIdentifier;

    public DatabaseIdentifier(String identifier, ReferenceDatabase referenceDatabase) {
        super(identifier);
        setReferenceDatabase(referenceDatabase);
    }

    public static DatabaseIdentifier fetchOrCreate(String databaseIdentifierValue, ReferenceDatabase referenceDatabase) {
        if (refDbToIdentifierToDatabaseIdentifier == null) {
            refDbToIdentifierToDatabaseIdentifier = new HashMap<>();
        }

        return refDbToIdentifierToDatabaseIdentifier
            .computeIfAbsent(referenceDatabase, k -> new HashMap<>())
            .computeIfAbsent(databaseIdentifierValue,
                k -> new DatabaseIdentifier(databaseIdentifierValue, referenceDatabase));
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
