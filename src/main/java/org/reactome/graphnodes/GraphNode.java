package org.reactome.graphnodes;

import org.reactome.graphdb.DbIdGenerator;
import org.reactome.graphdb.ReactomeGraphDatabase;

import java.util.*;
import java.util.stream.Collectors;

import static org.reactome.utils.StringUtils.quote;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public abstract class GraphNode {
    private long dbId;
    private boolean isNodeInDatabase;

    public GraphNode() {
        this.dbId = DbIdGenerator.getNextDbId();
        this.isNodeInDatabase = false;
    }

    public GraphNode(long dbId) {
        this.dbId = dbId;
        this.isNodeInDatabase = true;
    }

    public long getDbId() {
        return this.dbId;
    }

    public boolean isNodeInDatabase() {
        return this.isNodeInDatabase;
    }

    public String getSchemaClass() {
        return "DatabaseObject";
    }

    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.add("DatabaseObject");
        return labels;
    }

    protected Set<String> getAttributeKeyValueStrings() {
        return new LinkedHashSet<>(Arrays.asList(
            "dbId: " + getDbId(),
            "displayName: " + quote(getDisplayName()),
            "schemaClass: " + quote(this.getClass().getSimpleName())
        ));
    }

    @Override
    public String toString() {
        return String.format(
            "(:%s {%s})",
            String.join(":", getLabels()),
            String.join(",", getAttributeKeyValueStrings())
        );
    }

    public boolean insertNode() {
        if (isNodeInDatabase()) {
            return false;
        }
        setDbId();
        ReactomeGraphDatabase.getSession().run(
            "MERGE " + this.toString()
        );

        setIsNodeInDatabase(true);
        return true;
    }

    protected abstract String getDisplayName();

    protected void setIsNodeInDatabase(boolean isNodeInDatabase) {
        this.isNodeInDatabase = isNodeInDatabase;
    }

    private void setDbId() {
        this.dbId = DbIdGenerator.getNextDbId();
    }

    public static class Relationship {
        private String variable;
        private String name;
        private Map<String, String> attributes;

        public Relationship(String variable, String name) {
            this.variable = variable;
            this.name = name;
            this.attributes = new HashMap<>();
        }

        public Relationship(String variable, String name, Map<String, String> attributes) {
            this(variable, name);
            this.attributes = attributes;
        }

        public String getVariable() {
            return this.variable;
        }

        public String getName() {
            return this.name;
        }

        public Map<String, String> getAttributes() {
            return new HashMap<>(this.attributes);
        }

        @Override
        public String toString() {
            return String.format("[%s:%s%s]",
                getVariable(),
                getName(),
                getAttributes() != null && !getAttributes().isEmpty()  ?
                    " {" + String.join(",", getAttributeKeyValueStrings()) + "}" : ""
            );
        }

        private Set<String> getAttributeKeyValueStrings() {
            return getAttributes()
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }
}
