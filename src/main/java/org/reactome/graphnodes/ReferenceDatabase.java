package org.reactome.graphnodes;

import org.json.JSONObject;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.reactome.graphdb.ReactomeGraphDatabase;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.reactome.utils.ResourceJSONParser.convertJSONArrayToStringList;
import static org.reactome.utils.ResourceJSONParser.getResourceJSONObject;
import static org.reactome.utils.StringUtils.getLongest;
import static org.reactome.utils.StringUtils.quote;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 3/29/2022
 */
public class ReferenceDatabase extends GraphNode {
    private List<String> names;
    private String accessURL;
    private String url;
    private String resourceIdentifier;

    private ReferenceDatabase(long dbId) {
        super(dbId);
    }

    private ReferenceDatabase(List<String> names, String accessURL, String url, String resourceIdentifier) {
        this.names = names;
        this.accessURL = accessURL;
        this.url = url;
        this.resourceIdentifier = resourceIdentifier;
    }

    public static ReferenceDatabase createReferenceDatabaseFromGraphDb(
        long dbId, ReferenceDatabase parsedReferenceDatabase) {

        ReferenceDatabase referenceDatabase = new ReferenceDatabase(dbId);
        referenceDatabase.names = parsedReferenceDatabase.getNames();
        referenceDatabase.accessURL = parsedReferenceDatabase.getAccessURL();
        referenceDatabase.url = parsedReferenceDatabase.getUrl();
        referenceDatabase.resourceIdentifier = parsedReferenceDatabase.getResourceIdentifier();
        return referenceDatabase;
    }

    public static ReferenceDatabase parseReferenceDatabase(String referenceName) throws IllegalArgumentException {
        JSONObject referenceDatabaseJSONObject = getResourceJSONObject(referenceName).getJSONObject("referenceDatabase");

        List<String> names = convertJSONArrayToStringList(referenceDatabaseJSONObject.optJSONArray("names"));
        String url = referenceDatabaseJSONObject.getString("url");
        String accessURL = referenceDatabaseJSONObject.getString("accessURL");
        String resourceIdentifier = referenceDatabaseJSONObject.has("resourceIdentifier") ?
            referenceDatabaseJSONObject.getString("resourceIdentifier") : null;

        ReferenceDatabase.ReferenceDatabaseBuilder referenceDatabaseBuilder =
            new ReferenceDatabase.ReferenceDatabaseBuilder(names, url, accessURL);
        if (resourceIdentifier != null) {
            referenceDatabaseBuilder.withResourceIdentifier(resourceIdentifier);
        }

        return referenceDatabaseBuilder.build();
    }

    @Override
    public String getSchemaClass() {
        return "ReferenceDatabase";
    }

    @Override
    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(super.getLabels());
        labels.add(getSchemaClass());
        return labels;
    }

    @Override
    public Set<String> getAttributeKeyValueStrings() {
        Set<String> attributeKeyValueStrings = new LinkedHashSet<>();
        attributeKeyValueStrings.addAll(super.getAttributeKeyValueStrings());
        attributeKeyValueStrings.addAll(
            Arrays.asList(
                "accessUrl: " + quote(getAccessURL()),
                "name: " + "[" + getListOfQuotedNames() + "]",
                "url: " + quote(getUrl())
            )
        );
        return attributeKeyValueStrings;
    }

    @Override
    public String getDisplayName() {
        return this.names.get(0);
    }

    public String getLongestName() {
        return getNames().stream()
            .sorted((name1, name2) -> Integer.compare(name2.length(), name1.length()))
            .findFirst()
            .orElse("");
    }

    public List<String> getNames() {
        return this.names;
    }

    public String getAccessURL() {
        return this.accessURL;
    }

    public String getUrl() {
        return this.url;
    }

    public String getResourceIdentifier() {
        return this.resourceIdentifier;
    }

    public Long getDbIdInGraphDatabase() {
        String referenceDatabasesQuery =
            "MATCH (rd:ReferenceDatabase) RETURN rd.dbId as dbId, rd.name as names";

        Result referenceDatabasesQueryResult = ReactomeGraphDatabase.getSession().run(referenceDatabasesQuery);
        Map<String, Long> referenceDatabaseLongestNameToDbId = referenceDatabasesQueryResult
            .stream()
            .collect(
                toMap(
                    record -> getLongest(record.get("names").asList(Value::asString)),
                    record -> record.get("dbId").asLong(),
                    (first, second) -> first
                )
            );

        return referenceDatabaseLongestNameToDbId.get(this.getLongestName());
    }



    private String getListOfQuotedNames() {
        return getNames().stream().map(name -> quote(name)).collect(Collectors.joining(","));
    }

    public static class ReferenceDatabaseBuilder {
        private List<String> names;
        private String accessURL;
        private String url;
        private String resourceIdentifier;

        public ReferenceDatabaseBuilder(List<String> names, String url, String accessURL) {
            if (names == null || names.isEmpty()) {
                throw new IllegalArgumentException(
                    "Non-null and non-empty names list required for reference database. Got: " + names
                );
            }

            if (url == null) {
                throw new IllegalArgumentException("URL required for reference database");
            }

            if (accessURL == null) {
                throw new IllegalArgumentException("Access URL required for reference database");
            }

            this.names = names;
            this.url = url;
            this.accessURL = accessURL;

            // Initialize empty value as default
            this.resourceIdentifier = "";
        }

        public ReferenceDatabaseBuilder withResourceIdentifier(String resourceIdentifier) {
            if (resourceIdentifier == null) {
                throw new IllegalArgumentException("Non-null resource identifier required for reference database");
            }

            this.resourceIdentifier = resourceIdentifier;
            return this;
        }

        public ReferenceDatabase build() {
            return new ReferenceDatabase(this.names, this.accessURL, this.url, this.resourceIdentifier);
        }
    }
}
