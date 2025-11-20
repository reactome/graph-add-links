package org.reactome.graphnodes;

import org.reactome.graphdb.ReactomeGraphDatabase;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/6/2023
 */
public class ReferenceTherapeutic extends IdentifierNode {
    private static Set<ReferenceTherapeutic> referenceTherapeuticCache = new LinkedHashSet<>();

    public static Map<String, ReferenceTherapeutic> fetchReferenceTherapeuticForGuideToPharmacologyIdentifiers(
        Set<String> guideToPharmacologyIdentifiers) {

        referenceTherapeuticCache.addAll(fetchNonCachedReferenceTherapeutics(guideToPharmacologyIdentifiers));

        return referenceTherapeuticCache.stream().collect(Collectors.toMap(
            rt -> rt.getIdentifier(),
            rt -> rt
        ));
    }

    public ReferenceTherapeutic(long dbId, String identifier) {
        super(dbId, identifier);
    }

    @Override
    public String getSchemaClass() {
        return "ReferenceTherapeutic";
    }

    @Override
    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(super.getLabels());
        labels.add(getSchemaClass());
        return labels;
    }

    private static Set<ReferenceTherapeutic> fetchNonCachedReferenceTherapeutics(Set<String> guideToPharmacologyIdentifiers) {
        final List<String> cachedReferenceTherapeuticIdentifiers =
            referenceTherapeuticCache.stream().map(rgp -> rgp.getIdentifier()).collect(Collectors.toList());

        Set<String> unfetchedReferenceTherapeuticIdentifiers = new LinkedHashSet<>();
        for (String guideToPharmacologyIdentifier : guideToPharmacologyIdentifiers) {
            if (!cachedReferenceTherapeuticIdentifiers.contains(guideToPharmacologyIdentifier)) {
                unfetchedReferenceTherapeuticIdentifiers.add(guideToPharmacologyIdentifier);
            }
        }

        return queryReferenceTherapeutics(unfetchedReferenceTherapeuticIdentifiers);
    }

    private static Set<ReferenceTherapeutic> queryReferenceTherapeutics(Set<String> guideToPharmacologyIdentifiers) {
        return ReactomeGraphDatabase.getSession()
            .run(getReferenceTherapeuticDataQuery(guideToPharmacologyIdentifiers))
            .stream()
            .map(record -> {
                long dbId = record.get("dbId").asLong();
                String identifier = record.get("identifier").asString();

                return new ReferenceTherapeutic(dbId, identifier);
            })
            //.peek(System.out::println)
            .collect(Collectors.toSet());

    }

    private static String getReferenceTherapeuticDataQuery(Set<String> guideToPharmacologyIdentifiers) {
        return "MATCH (rt:ReferenceTherapeutic)-[:referenceDatabase]-(rd:ReferenceDatabase) " +
            "WHERE rd.displayName = 'Guide to Pharmacology - Ligands' " +
            getFilterStatementForGuideToPharmacologyIdentifiers(guideToPharmacologyIdentifiers) +
            " RETURN rt.dbId as dbId, rt.identifier as identifier";
    }

    private static String getFilterStatementForGuideToPharmacologyIdentifiers(
        Collection<String> guideToPharmacologyIdentifiers) {

        final String noFilter = "";

        return guideToPharmacologyIdentifiers != null && !guideToPharmacologyIdentifiers.isEmpty() ?
            String.format("AND rt.identifier IN %s", formatAsCypherList(guideToPharmacologyIdentifiers)) :
            noFilter;
    }

    private static String formatAsCypherList(Collection<String> stringList) {
        return "[" + String.join(", ", stringList.stream().map(string -> "\"" + string + "\"").collect(Collectors.toList())) + "]";
    }
}
