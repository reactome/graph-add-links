package org.reactome.graphnodes;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.utils.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.reactome.utils.StringUtils.quote;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/12/2022
 */
public class ReferenceGeneProduct extends ReferenceSequence {
    private static final String REFERENCE_DATABASE_NAME = "UniProt";

    private static boolean allRGPsFetched;
    private static Set<ReferenceGeneProduct> rgpCache = new LinkedHashSet<>();
    private static ReferenceDatabase referenceDatabase;

    private String speciesName;

    protected ReferenceGeneProduct(long dbId, String identifier, String speciesName) {
        this(dbId, identifier, Collections.emptyList(), speciesName);
    }

    protected ReferenceGeneProduct(long dbId, String identifier, List<String> geneNames, String speciesName) {
        super(dbId, identifier, fetchReferenceDatabase(), geneNames);
        this.speciesName = speciesName;
    }

    public static Map<String, ReferenceGeneProduct> fetchReferenceGeneProductsForUniProtIdentifiers(
        Set<String> uniprotIdentifiers) {

        rgpCache.addAll(fetchNonCachedReferenceGeneProducts(uniprotIdentifiers));

        return rgpCache.stream().collect(Collectors.toMap(
            rgp -> rgp.getIdentifier(),
            rgp -> rgp
        ));
    }

    public static Map<String, ReferenceGeneProduct> fetchAllReferenceGeneProducts() {
        if (!allRGPsFetched) {
            String referenceGeneProductVariableName = "rgp";
            String speciesVariableName = "species";

            ReactomeGraphDatabase.getSession()
                .run(getReferenceGeneProductDataQuery(referenceGeneProductVariableName, speciesVariableName))
                .stream()
                .forEach(record -> {
                    long dbId = record.get("dbId").asLong();
                    String identifier = record.get("identifier").asString();
                    List<String> geneNames = !record.get("geneNames").isNull() ?
                        record.get("geneNames").asList(Value::asString) : new ArrayList<>();
                    String speciesName = record.get("speciesName").asString();

                    rgpCache.add(new ReferenceGeneProduct(dbId, identifier, geneNames, speciesName));
                });
            allRGPsFetched = true;
        }

        return rgpCache.stream().collect(Collectors.toMap(
            rgp -> rgp.getIdentifier(),
            rgp -> rgp
        ));
    }

//    public static Map<String, ReferenceGeneProduct> fetchHumanReferenceGeneProducts() {
//        return fetchReferenceGeneProductsForSpecies("Homo sapiens");
//    }

//    public static Map<String, ReferenceGeneProduct> fetchReferenceGeneProductsForSpecies(String speciesName) {
//        return fetchAllReferenceGeneProducts()
//            .entrySet()
//            .stream()
//            .filter(entry -> entry.getValue().getSpeciesName().equals(speciesName))
//            .collect(Collectors.toMap(
//                Map.Entry::getKey,
//                Map.Entry::getValue
//            ));
//    }

    public String getSpeciesName() {
        return this.speciesName;
    }

    @Override
    public String getSchemaClass() {
        return "ReferenceGeneProduct";
    }

    @Override
    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(super.getLabels());
        labels.add(getSchemaClass());
        return labels;
    }

    @Override
    protected Set<String> getAttributeKeyValueStrings() {
        Set<String> attributeKeyValueStrings = new LinkedHashSet<>();
        attributeKeyValueStrings.addAll(super.getAttributeKeyValueStrings());
        if (!getSpeciesName().isEmpty()){
            attributeKeyValueStrings.add("speciesName: " + quote(getSpeciesName()));
        }
        return attributeKeyValueStrings;
    }

    @Override
    public boolean equals(Object otherRGP) {
        if (otherRGP == null || !(otherRGP instanceof ReferenceGeneProduct)) {
            return false;
        }

        return this.getIdentifier().equals(((ReferenceGeneProduct) otherRGP).getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getIdentifier());
    }

    @Override
    protected void setIdentifier(String identifier) {
        if (!isValidUniProtIdentifier(identifier)) {
        //    throw new IllegalArgumentException(identifier + " is not a valid UniProt Identifier.  " +
        //        "It must be a 6 or 10 character alphanumeric string (e.g. P12345, A0A3Q9U766).");
        }
        super.setIdentifier(identifier);
    }

    private static Set<ReferenceGeneProduct> fetchNonCachedReferenceGeneProducts(Set<String> uniProtIdentifiers) {
        final List<String> cachedRGPIdentifiers =
            rgpCache.stream().map(rgp -> rgp.getIdentifier()).collect(Collectors.toList());

        Set<String> unfetchedReferenceGeneProductIdentifiers = new LinkedHashSet<>();
        for (String uniProtIdentifier : uniProtIdentifiers) {
            if (!cachedRGPIdentifiers.contains(uniProtIdentifier)) {
                unfetchedReferenceGeneProductIdentifiers.add(uniProtIdentifier);
            }
        }

        return queryReferenceGeneProducts(unfetchedReferenceGeneProductIdentifiers);
    }

    private static Set<ReferenceGeneProduct> queryReferenceGeneProducts(Set<String> uniProtIdentifiers) {
        final String referenceGeneProductVariableName = "rgp";
        final String speciesVariableName = "species";

        Set<ReferenceGeneProduct> referenceGeneProducts = new LinkedHashSet<>();

        //int count = 0;
        for (Collection<String> subSetUniProtIdentifiers : CollectionUtils.split(uniProtIdentifiers, 100)) {
            Set<ReferenceGeneProduct> subSetReferenceGeneProducts =
                ReactomeGraphDatabase.getSession()
                    .run(getReferenceGeneProductDataQuery(referenceGeneProductVariableName, speciesVariableName, subSetUniProtIdentifiers))
                    .stream()
                    .map(record -> {
                        long dbId = record.get("dbId").asLong();
                        String identifier = record.get("identifier").asString();
                        List<String> geneNames = !record.get("geneNames").isNull() ?
                            record.get("geneNames").asList(Value::asString) :
                            new ArrayList<>();
                        String speciesName = record.get("speciesName").asString();

                        return new ReferenceGeneProduct(dbId, identifier, geneNames, speciesName);
                    })
                    //.peek(System.out::println)
                    .collect(Collectors.toSet());

            referenceGeneProducts.addAll(subSetReferenceGeneProducts);
            //count++;
            //System.out.println(count);
        }
        return referenceGeneProducts;
    }

    private static ReferenceDatabase fetchReferenceDatabase() {
        if (referenceDatabase == null) {
            StatementResult referenceDBMatchResult =
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

    private static ReferenceGeneProduct parseGraphDatabaseRecord(Value referenceGeneProductRecord, String speciesName) {
        long dbId = referenceGeneProductRecord.get("dbId").asLong();
        String identifier = referenceGeneProductRecord.get("identifier").asString();
        List<String> geneNames = referenceGeneProductRecord.get("geneName").asList(Value::asString);

        return new ReferenceGeneProduct(dbId, identifier, geneNames, speciesName);
    }

    private static String getReferenceGeneProductDataQuery(
        String referenceGeneProductVariable, String speciesVariableName) {

        final Set<String> emptyUniProtIdentifiersSet = new HashSet<>();

        return getReferenceGeneProductDataQuery(
            referenceGeneProductVariable, speciesVariableName, emptyUniProtIdentifiersSet
        );
    }

    //TODO Consider making generic query runner class and class for parser
    private static String getReferenceGeneProductDataQuery(
        String referenceGeneProductVariable, String speciesVariableName, Collection<String> uniProtIdentifiers) {

        String dbIdVariable = referenceGeneProductVariable + ".dbId";
        String identifierVariable = referenceGeneProductVariable + ".identifier";
        String geneNameVariable = referenceGeneProductVariable + ".geneName";
        String speciesNameVariable = speciesVariableName + ".displayName";

        return String.format(
            "MATCH (%s:ReferenceGeneProduct)-[:species]->(%s:Species) " +
            "MATCH (%s)-[:referenceDatabase]->(rd:ReferenceDatabase) " +
            "WHERE rd.displayName = \"UniProt\" " +
            getFilterStatementForUniProtIdentifiers(identifierVariable, uniProtIdentifiers) +
            " RETURN %s as dbId, %s as identifier,%s as geneNames,%s as speciesName",
            referenceGeneProductVariable,
            speciesVariableName,
            referenceGeneProductVariable,
            dbIdVariable,
            identifierVariable,
            geneNameVariable,
            speciesNameVariable
        );
    }

    private static String getFilterStatementForUniProtIdentifiers(
        String identifierVariable, Collection<String> uniProtIdentifiers) {

        final String noFilter = "";

        return uniProtIdentifiers != null && !uniProtIdentifiers.isEmpty() ?
            String.format("AND %s IN %s", identifierVariable, formatAsCypherList(uniProtIdentifiers)) :
            noFilter;
    }

    private static String formatAsCypherList(Collection<String> stringList) {
        return "[" + String.join(", ", stringList.stream().map(string -> "\"" + string + "\"").collect(Collectors.toList())) + "]";
    }

    private static String getReferenceDatabaseQuery() {
        return "MATCH (rd:ReferenceDatabase) WHERE rd.displayName = '" + REFERENCE_DATABASE_NAME + "'RETURN rd";
    }

    private boolean isValidUniProtIdentifier(String identifier) {
        return identifier.matches("^\\w+$") &&
            (identifier.length() == 6 || identifier.length() == 10);
    }
}
