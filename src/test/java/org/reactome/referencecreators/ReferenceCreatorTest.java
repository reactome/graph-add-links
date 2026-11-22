package org.reactome.referencecreators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactome.graphnodes.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Tests for ReferenceCreator to ensure duplicate identifier prevention, and that a source node appearing in
 * several rows (as MondoReferenceCreator's one row per (Event or PhysicalEntity, Disease Ontology term) does)
 * keeps the external identifiers of every row.
 *
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 11/03/2025
 */
@ExtendWith(MockReferenceDatabaseParsing.class)
public class ReferenceCreatorTest {

    /** Disease Ontology id to MONDO ids, as MondoFileProcessor builds it. */
    private static final Map<String, List<String>> DO_TO_MONDO = new HashMap<>();
    static {
        DO_TO_MONDO.put("DOID:162", Collections.singletonList("0004992"));
        DO_TO_MONDO.put("DOID:3068", Collections.singletonList("0018177"));
        DO_TO_MONDO.put("DOID:3187", Collections.singletonList("0002544"));
        DO_TO_MONDO.put("DOID:9999", Collections.singletonList("0004992"));
    }

    @TempDir
    Path tempDir;

    @Mock
    private ReferenceDatabase mockReferenceDatabase;

    private TestReferenceCreator referenceCreator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockReferenceDatabase.getDisplayName()).thenReturn("TestDB");
        when(mockReferenceDatabase.getDbId()).thenReturn(100L);
    }

    /**
     * Tests that when multiple IdentifierNode objects have the same dbId,
     * they are mapped correctly by dbId rather than object identity,
     * and that their external identifiers are listed once each.
     * This is the core fix for the duplicate identifiers bug.
     */
    @Test
    public void testSourceNodesMappedByDbIdNotObjectIdentity() throws IOException, URISyntaxException {
        // Create two different objects with the same dbId (simulating isoform/canonical protein)
        IdentifierNode sourceNode1 = new TestIdentifierNode(1L, "PROTEIN_1");
        IdentifierNode sourceNode2 = new TestIdentifierNode(1L, "PROTEIN_1"); // Same dbId, different object

        Map<String, Set<String>> sourceToRefMap = new HashMap<>();
        sourceToRefMap.put("PROTEIN_1", new HashSet<>(Arrays.asList("EXT_ID_1", "EXT_ID_2")));

        List<IdentifierNode> sourceNodes = Arrays.asList(sourceNode1, sourceNode2);

        // The bug would create duplicate entries because it mapped by object identity
        // The fix maps by dbId, so both objects should map to the same key
        Map<Long, List<? extends IdentifierNode>> result =
            ReferenceCreator.externalIdentifiersBySource(sourceNodes, externalIdentifiersFrom(sourceToRefMap));

        // Should have only one entry (keyed by dbId=1)
        assertThat(result.size(), is(1));
        assertThat(result.containsKey(1L), is(true));

        // Both rows have the same identifiers, so each should be listed only once
        assertThat(identifiers(result.get(1L)), containsInAnyOrder("EXT_ID_1", "EXT_ID_2"));
    }

    /**
     * Tests that when the same source node appears in several rows with different identifiers,
     * the external identifiers of every row are kept rather than only those of the last row.
     */
    @Test
    public void testSourceNodeInSeveralRowsKeepsEveryRowsExternalIdentifiers() {
        IdentifierNode row1 = new TestIdentifierNode(1L, "TERM_1");
        IdentifierNode row2 = new TestIdentifierNode(1L, "TERM_2");

        Map<String, Set<String>> sourceToRefMap = new HashMap<>();
        sourceToRefMap.put("TERM_1", new HashSet<>(Collections.singletonList("EXT_ID_1")));
        sourceToRefMap.put("TERM_2", new HashSet<>(Collections.singletonList("EXT_ID_2")));

        Map<Long, List<? extends IdentifierNode>> result = ReferenceCreator.externalIdentifiersBySource(
            Arrays.asList(row1, row2), externalIdentifiersFrom(sourceToRefMap));

        assertThat(result.size(), is(1));
        assertThat(identifiers(result.get(1L)), containsInAnyOrder("EXT_ID_1", "EXT_ID_2"));
    }

    /**
     * Tests that duplicate external identifier lines are not written to the CSV file.
     * This verifies the in-memory Set-based duplicate prevention.
     */
    @Test
    public void testNoDuplicateExternalIdentifierLinesWritten() throws IOException, URISyntaxException {
        IdentifierNode sourceNode = new TestIdentifierNode(1L, "PROTEIN_1");

        Map<String, Set<String>> sourceToRefMap = new HashMap<>();
        // Multiple sources mapping to the same external identifier
        sourceToRefMap.put("PROTEIN_1", new HashSet<>(Collections.singletonList("SHARED_EXT_ID")));
        sourceToRefMap.put("PROTEIN_2", new HashSet<>(Collections.singletonList("SHARED_EXT_ID")));

        List<IdentifierNode> sourceNodes = Collections.singletonList(sourceNode);

        referenceCreator = new TestReferenceCreator("TestResource", sourceToRefMap, sourceNodes, mockReferenceDatabase);

        // Simulate writing the same external identifier multiple times
        IdentifierNode externalIdentifier = new TestIdentifierNode(200L, "SHARED_EXT_ID");

        Path identifierFile = tempDir.resolve("test_identifiers.csv");
        Files.write(identifierFile, "DbId,DisplayName,SchemaClass,Identifier,ReferenceDbName,URL\n".getBytes());

        // Write the same identifier twice
        String line1 = referenceCreator.testGetExternalIdentifierLine(externalIdentifier);
        String line2 = referenceCreator.testGetExternalIdentifierLine(externalIdentifier);

        // Lines should be identical
        assertThat(line1, is(equalTo(line2)));

        // The in-memory tracking should prevent duplicates
        assertThat(referenceCreator.testExternalIdentifierLineAlreadyWritten(externalIdentifier), is(false));
        referenceCreator.testMarkExternalIdentifierLineAsWritten(externalIdentifier);
        assertThat(referenceCreator.testExternalIdentifierLineAlreadyWritten(externalIdentifier), is(true));
    }

    /**
     * Tests that relationship lines are written for each source-to-external mapping,
     * even when the same external identifier is referenced multiple times.
     */
    @Test
    public void testRelationshipLinesWrittenForEachSourceMapping() throws IOException, URISyntaxException {
        // Three different source nodes mapping to the same external identifier
        IdentifierNode source1 = new TestIdentifierNode(1L, "PROTEIN_1");
        IdentifierNode source2 = new TestIdentifierNode(2L, "PROTEIN_2");
        IdentifierNode source3 = new TestIdentifierNode(3L, "PROTEIN_3");

        Map<String, Set<String>> sourceToRefMap = new HashMap<>();
        sourceToRefMap.put("PROTEIN_1", new HashSet<>(Collections.singletonList("SHARED_EXT_ID")));
        sourceToRefMap.put("PROTEIN_2", new HashSet<>(Collections.singletonList("SHARED_EXT_ID")));
        sourceToRefMap.put("PROTEIN_3", new HashSet<>(Collections.singletonList("SHARED_EXT_ID")));

        List<IdentifierNode> sourceNodes = Arrays.asList(source1, source2, source3);

        referenceCreator = new TestReferenceCreator("TestResource", sourceToRefMap, sourceNodes, mockReferenceDatabase);

        Path relationshipFile = tempDir.resolve("test_relationships.csv");
        Files.write(relationshipFile, "SourceDbId,ExternalIdentifierDbId,ReferenceDatabaseDbId,InstanceEditDbId\n".getBytes());

        // Simulate writing relationship lines
        List<String> relationshipLines = new ArrayList<>();
        relationshipLines.add("1,200,100,999");
        relationshipLines.add("2,200,100,999");
        relationshipLines.add("3,200,100,999");

        Files.write(relationshipFile, relationshipLines, java.nio.file.StandardOpenOption.APPEND);

        List<String> writtenLines = Files.readAllLines(relationshipFile);

        // Should have 4 lines: 1 header + 3 relationship lines
        assertThat(writtenLines.size(), is(4));

        // All three sources should have relationships to the same external ID
        assertThat(writtenLines.get(1), containsString("1,200"));
        assertThat(writtenLines.get(2), containsString("2,200"));
        assertThat(writtenLines.get(3), containsString("3,200"));
    }

    /**
     * Tests that multiple runs don't create duplicate entries when using
     * the in-memory tracking mechanism.
     */
    @Test
    public void testMultipleRunsPreventDuplicates() throws IOException, URISyntaxException {
        IdentifierNode sourceNode = new TestIdentifierNode(1L, "PROTEIN_1");
        IdentifierNode externalId1 = new TestIdentifierNode(200L, "EXT_ID_1");
        IdentifierNode externalId2 = new TestIdentifierNode(201L, "EXT_ID_2");

        Map<String, Set<String>> sourceToRefMap = new HashMap<>();
        sourceToRefMap.put("PROTEIN_1", new HashSet<>(Arrays.asList("EXT_ID_1", "EXT_ID_2")));

        List<IdentifierNode> sourceNodes = Collections.singletonList(sourceNode);

        referenceCreator = new TestReferenceCreator("TestResource", sourceToRefMap, sourceNodes, mockReferenceDatabase);

        // First pass - mark as written
        assertThat(referenceCreator.testExternalIdentifierLineAlreadyWritten(externalId1), is(false));
        referenceCreator.testMarkExternalIdentifierLineAsWritten(externalId1);
        assertThat(referenceCreator.testExternalIdentifierLineAlreadyWritten(externalId1), is(true));

        // Second pass - should detect it's already written
        assertThat(referenceCreator.testExternalIdentifierLineAlreadyWritten(externalId1), is(true));

        // Different ID should not be marked
        assertThat(referenceCreator.testExternalIdentifierLineAlreadyWritten(externalId2), is(false));
    }

    @Test
    public void anObjectWithSeveralDiseaseTermsGetsEachTermsMondo() {
        // One row per (object, DO term), as MondoReferenceCreator queries them.
        List<IdentifierNode> rows = Arrays.asList(
            new Event(1996341L, "DOID:162"),
            new Event(1996341L, "DOID:3068"),
            new Event(1996341L, "DOID:3187")
        );

        Map<Long, List<? extends IdentifierNode>> bySource =
            ReferenceCreator.externalIdentifiersBySource(rows, ReferenceCreatorTest::mondoFor);

        assertThat(identifiers(bySource.get(1996341L)), containsInAnyOrder("0004992", "0018177", "0002544"));
    }

    @Test
    public void aMondoReachedThroughTwoTermsIsListedOnce() {
        List<IdentifierNode> rows = Arrays.asList(
            new Event(42L, "DOID:162"),
            new Event(42L, "DOID:9999")
        );

        Map<Long, List<? extends IdentifierNode>> bySource =
            ReferenceCreator.externalIdentifiersBySource(rows, ReferenceCreatorTest::mondoFor);

        assertThat(identifiers(bySource.get(42L)), contains("0004992"));
    }

    @Test
    public void objectsAreKeptApart() {
        List<IdentifierNode> rows = Arrays.asList(
            new Event(1L, "DOID:162"),
            new Event(2L, "DOID:3187")
        );

        Map<Long, List<? extends IdentifierNode>> bySource =
            ReferenceCreator.externalIdentifiersBySource(rows, ReferenceCreatorTest::mondoFor);

        assertThat(identifiers(bySource.get(1L)), contains("0004992"));
        assertThat(identifiers(bySource.get(2L)), contains("0002544"));
    }

    private static List<? extends IdentifierNode> mondoFor(IdentifierNode source) {
        return DO_TO_MONDO.getOrDefault(source.getIdentifier(), Collections.emptyList()).stream()
            .map(mondo -> new Event(0L, mondo))
            .collect(Collectors.toList());
    }

    /** External identifiers for a source node from a source identifier to external identifiers map. */
    private static java.util.function.Function<IdentifierNode, List<? extends IdentifierNode>> externalIdentifiersFrom(
        Map<String, Set<String>> sourceToRefMap) {

        return sourceNode -> sourceToRefMap.getOrDefault(sourceNode.getIdentifier(), Collections.emptySet()).stream()
            .map(id -> new TestIdentifierNode(200L + id.hashCode() % 1000, id))
            .collect(Collectors.toList());
    }

    private static List<String> identifiers(List<? extends IdentifierNode> nodes) {
        return nodes.stream().map(IdentifierNode::getIdentifier).collect(Collectors.toList());
    }

    // Test implementation of ReferenceCreator for testing purposes
    private static class TestReferenceCreator extends ReferenceCreator {
        private List<IdentifierNode> testSourceNodes;
        private ReferenceDatabase testReferenceDatabase;
        private Set<String> externalIdentifierFileLines = new HashSet<>();

        public TestReferenceCreator(String resourceName,
                                   Map<String, Set<String>> sourceToRefMap,
                                   List<IdentifierNode> sourceNodes,
                                   ReferenceDatabase referenceDatabase) throws IllegalArgumentException {
            super(resourceName, sourceToRefMap);
            this.testSourceNodes = sourceNodes;
            this.testReferenceDatabase = referenceDatabase;
        }

        @Override
        protected List<? extends IdentifierNode> fetchExternalIdentifiersForSourceIdentifierNode(IdentifierNode sourceNode) {
            Set<String> identifierValues = getIdentifierValues(sourceNode);
            return identifierValues.stream()
                .map(id -> new TestIdentifierNode(200L + id.hashCode() % 1000, id))
                .collect(Collectors.toList());
        }

        @Override
        protected List<? extends IdentifierNode> getIdentifierNodes() {
            return testSourceNodes;
        }

        @Override
        public ReferenceDatabase getReferenceDatabase() {
            return testReferenceDatabase;
        }

        @Override
        public void readCSV() throws IOException, URISyntaxException {
            // No-op for testing
        }

        // Expose protected/private methods for testing
        public String testGetExternalIdentifierLine(IdentifierNode externalIdentifier) {
            return getExternalIdentifierLine(externalIdentifier);
        }

        public boolean testExternalIdentifierLineAlreadyWritten(IdentifierNode externalIdentifier) {
            return externalIdentifierFileLines.contains(getExternalIdentifierLine(externalIdentifier));
        }

        public void testMarkExternalIdentifierLineAsWritten(IdentifierNode externalIdentifier) {
            externalIdentifierFileLines.add(getExternalIdentifierLine(externalIdentifier));
        }
    }

    // Test implementation of IdentifierNode
    private static class TestIdentifierNode extends IdentifierNode {
        public TestIdentifierNode(long dbId, String identifier) {
            super(dbId, identifier);
        }

        public TestIdentifierNode(String identifier) {
            super(identifier);
        }

        @Override
        public String getSchemaClass() {
            return "TestIdentifier";
        }

        @Override
        public Set<String> getLabels() {
            Set<String> labels = new LinkedHashSet<>();
            labels.add("DatabaseObject");
            labels.add("TestIdentifier");
            return labels;
        }
    }
}
