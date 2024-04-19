package org.reactome.graphnodes;

import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.reactome.graphdb.ReactomeGraphDatabase;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/6/2023
 */
public class ReferenceMolecule extends IdentifierNode {
    private static final String REFERENCE_DATABASE_NAME = "ChEBI";

    private static Set<ReferenceMolecule> referenceMoleculeCache = new LinkedHashSet<>();
    private static ReferenceDatabase referenceDatabase;

    public static Map<String, ReferenceMolecule> fetchReferenceMoleculesForChEBIIdentifiers(
        Set<String> chebiIdentifiers) {

        referenceMoleculeCache.addAll(fetchNonCachedReferenceMolecules(chebiIdentifiers));

        return referenceMoleculeCache.stream().collect(Collectors.toMap(
            rm -> rm.getIdentifier(),
            rm -> rm
        ));
    }

    public ReferenceMolecule(long dbId, String identifier) {
        super(dbId, identifier);
        setReferenceDatabase(fetchReferenceDatabase());
    }

    @Override
    public String getSchemaClass() {
        return "ReferenceMolecule";
    }

    @Override
    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(super.getLabels());
        labels.add(getSchemaClass());
        return labels;
    }

    private static ReferenceDatabase fetchReferenceDatabase() {
        if (referenceDatabase == null) {
            Result referenceDBMatchResult =
                ReactomeGraphDatabase.getSession().run(getReferenceDatabaseQuery());

            if (!referenceDBMatchResult.hasNext()) {
                throw new IllegalStateException("Can not find reference database with displayName of " + REFERENCE_DATABASE_NAME);
            }

            Value referenceDatabaseValue = referenceDBMatchResult.next().get("rd");
            List<String> names = referenceDatabaseValue.get("name").asList(Value::asString);
            String url = referenceDatabaseValue.get("url").asString();
            String accessURL = referenceDatabaseValue.get("accessUrl").asString();
            Value resourceIdentifierValue = referenceDatabaseValue.get("resourceIdentifier");
            String resourceIdentifier = !resourceIdentifierValue.isNull() ? resourceIdentifierValue.asString() : "";

            referenceDatabase = new ReferenceDatabase.ReferenceDatabaseBuilder(names, url, accessURL)
                .withResourceIdentifier(resourceIdentifier)
                .build();
        }
        return referenceDatabase;
    }

    private static Set<ReferenceMolecule> fetchNonCachedReferenceMolecules(Set<String> chebiIdentifiers) {
        final List<String> cachedReferenceMoleculeIdentifiers =
            referenceMoleculeCache.stream().map(rgp -> rgp.getIdentifier()).collect(Collectors.toList());

        Set<String> unfetchedReferenceMoleculeIdentifiers = new LinkedHashSet<>();
        for (String chebiIdentifier : chebiIdentifiers) {
            if (!cachedReferenceMoleculeIdentifiers.contains(chebiIdentifier)) {
                unfetchedReferenceMoleculeIdentifiers.add(chebiIdentifier);
            }
        }

        return queryReferenceMolecules(unfetchedReferenceMoleculeIdentifiers);
    }

    private static Set<ReferenceMolecule> queryReferenceMolecules(Set<String> chebiIdentifiers) {
        Set<String> chebiIdentifiersWithoutPrefix = chebiIdentifiers.stream()
            .map(chebiIdentifier -> chebiIdentifier.replace("CHEBI:",""))
            .collect(Collectors.toSet());


        return ReactomeGraphDatabase.getSession()
            .run(getReferenceMoleculeDataQuery(chebiIdentifiersWithoutPrefix))
            .stream()
            .map(record -> {
                long dbId = record.get("dbId").asLong();
                String identifier = record.get("identifier").asString();

                return new ReferenceMolecule(dbId, identifier);
            })
            .collect(Collectors.toSet());
    }

    private static String getReferenceMoleculeDataQuery(Set<String> chebiIdentifiers) {
        return "MATCH (rm:ReferenceMolecule) " +
            getFilterStatementForChEBIIdentifiers(chebiIdentifiers) +
            " RETURN rm.dbId as dbId, rm.identifier as identifier";
    }

    private static String getFilterStatementForChEBIIdentifiers(Collection<String> chebiIdentifiers) {
        final String noFilter = "";

        return chebiIdentifiers != null && !chebiIdentifiers.isEmpty() ?
            String.format("WHERE rm.identifier IN %s", formatAsCypherList(chebiIdentifiers)) :
            noFilter;
    }

    private static String formatAsCypherList(Collection<String> stringList) {
        return "[" + String.join(", ", stringList.stream().map(string -> "\"" + string + "\"").collect(Collectors.toList())) + "]";
    }

    private static String getReferenceDatabaseQuery() {
        return "MATCH (rd:ReferenceDatabase) WHERE rd.displayName = '" + REFERENCE_DATABASE_NAME + "'RETURN rd";
    }
}
