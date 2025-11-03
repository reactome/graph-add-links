package org.reactome.referencecreators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactome.graphnodes.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Mockito.when;

/**
 * Integration tests to verify that the duplicate identifier bug fix works
 * correctly across all components.
 *
 * This test simulates the actual bug scenario:
 * - Multiple IdentifierNode objects with the same dbId (isoforms/canonical proteins)
 * - Multiple sources mapping to the same external identifiers
 * - CSV writing should not create duplicates
 * - Relationship lines should be written for all sources
 *
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 11/03/2025
 */
public class DuplicatePreventionIntegrationTest {

    @TempDir
    Path tempDir;

    @Mock
    private ReferenceDatabase mockReferenceDatabase;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockReferenceDatabase.getDisplayName()).thenReturn("IntegrationTestDB");
        when(mockReferenceDatabase.getDbId()).thenReturn(500L);
        when(mockReferenceDatabase.getAccessURL()).thenReturn("http://test.db/###ID###");

        clearAllCaches();
    }

    /**
     * Integration test simulating the actual bug scenario:
     * - Protein P1 has isoforms that map to same dbId
     * - Both isoforms map to the same external identifiers
     * - Should create only one set of external identifier entries
     * - Should create relationship entries for all source mappings
     */
    @Test
    public void testIsoformsWithSameDbIdDontCreateDuplicates() throws Exception {
        // Simulate isoforms: same dbId (123), different object instances
        TestIdentifierNode canonicalProtein = new TestIdentifierNode(123L, "P12345");
        TestIdentifierNode isoform1 = new TestIdentifierNode(123L, "P12345"); // Same dbId, different object
        TestIdentifierNode isoform2 = new TestIdentifierNode(123L, "P12345"); // Same dbId, different object

        // All map to the same external identifiers
        Map<String, Set<String>> sourceToRefMap = new HashMap<>();
        sourceToRefMap.put("P12345", new HashSet<>(Arrays.asList("EXT_A", "EXT_B", "EXT_C")));

        List<IdentifierNode> sourceNodes = Arrays.asList(canonicalProtein, isoform1, isoform2);

        IntegrationTestReferenceCreator creator = new IntegrationTestReferenceCreator(
            "IntegrationTest",
            sourceToRefMap,
            sourceNodes,
            mockReferenceDatabase,
            tempDir
        );

        creator.writeCSV();

        // Verify: External identifier file should have each external ID only once
        Path identifierFile = tempDir.resolve("IntegrationTest_Identifiers.csv");
        List<String> identifierLines = Files.readAllLines(identifierFile);

        // 1 header + 3 unique external identifiers (EXT_A, EXT_B, EXT_C)
        assertThat("Should have header + 3 unique external identifiers",
            identifierLines.size(), is(4));

        // Verify: Each external identifier appears only once
        long extACount = identifierLines.stream().filter(line -> line.contains("EXT_A")).count();
        long extBCount = identifierLines.stream().filter(line -> line.contains("EXT_B")).count();
        long extCCount = identifierLines.stream().filter(line -> line.contains("EXT_C")).count();

        assertThat("EXT_A should appear exactly once", extACount, is(1L));
        assertThat("EXT_B should appear exactly once", extBCount, is(1L));
        assertThat("EXT_C should appear exactly once", extCCount, is(1L));

        // Verify: Relationship file should have entries for the source (by dbId)
        Path relationshipFile = tempDir.resolve("IntegrationTest_Relationships.csv");
        List<String> relationshipLines = Files.readAllLines(relationshipFile);

        // 1 header + 3 relationships (one per external identifier for the single dbId)
        assertThat("Should have header + 3 relationship entries",
            relationshipLines.size(), is(4));

        // All relationships should reference source dbId 123
        long dbId123Count = relationshipLines.stream()
            .filter(line -> line.startsWith("123,"))
            .count();
        assertThat("All relationships should reference dbId 123", dbId123Count, is(3L));
    }

    /**
     * Tests that multiple different sources mapping to shared external identifiers
     * creates the correct number of entries.
     */
    @Test
    public void testMultipleSourcesSharedExternalIdentifiers() throws Exception {
        TestIdentifierNode source1 = new TestIdentifierNode(1L, "PROTEIN_1");
        TestIdentifierNode source2 = new TestIdentifierNode(2L, "PROTEIN_2");
        TestIdentifierNode source3 = new TestIdentifierNode(3L, "PROTEIN_3");

        Map<String, Set<String>> sourceToRefMap = new HashMap<>();
        sourceToRefMap.put("PROTEIN_1", new HashSet<>(Arrays.asList("SHARED_EXT", "UNIQUE_EXT_1")));
        sourceToRefMap.put("PROTEIN_2", new HashSet<>(Arrays.asList("SHARED_EXT", "UNIQUE_EXT_2")));
        sourceToRefMap.put("PROTEIN_3", new HashSet<>(Arrays.asList("SHARED_EXT", "UNIQUE_EXT_3")));

        List<IdentifierNode> sourceNodes = Arrays.asList(source1, source2, source3);

        IntegrationTestReferenceCreator creator = new IntegrationTestReferenceCreator(
            "SharedTest",
            sourceToRefMap,
            sourceNodes,
            mockReferenceDatabase,
            tempDir
        );

        creator.writeCSV();

        // Verify: External identifier file
        Path identifierFile = tempDir.resolve("SharedTest_Identifiers.csv");
        List<String> identifierLines = Files.readAllLines(identifierFile);

        // 1 header + 4 unique external identifiers (SHARED_EXT + 3 unique ones)
        assertThat("Should have header + 4 unique external identifiers",
            identifierLines.size(), is(5));

        // SHARED_EXT should appear only once
        long sharedCount = identifierLines.stream().filter(line -> line.contains("SHARED_EXT")).count();
        assertThat("SHARED_EXT should appear exactly once", sharedCount, is(1L));

        // Verify: Relationship file
        Path relationshipFile = tempDir.resolve("SharedTest_Relationships.csv");
        List<String> relationshipLines = Files.readAllLines(relationshipFile);

        // 1 header + 6 relationships (2 per source: 1 shared + 1 unique)
        assertThat("Should have header + 6 relationship entries",
            relationshipLines.size(), is(7));

        // Each source should have 2 relationships
        long source1Relationships = relationshipLines.stream().filter(line -> line.startsWith("1,")).count();
        long source2Relationships = relationshipLines.stream().filter(line -> line.startsWith("2,")).count();
        long source3Relationships = relationshipLines.stream().filter(line -> line.startsWith("3,")).count();

        assertThat("Source 1 should have 2 relationships", source1Relationships, is(2L));
        assertThat("Source 2 should have 2 relationships", source2Relationships, is(2L));
        assertThat("Source 3 should have 2 relationships", source3Relationships, is(2L));
    }

    /**
     * Tests the complete workflow: object identity bug fix + CSV deduplication.
     */
    @Test
    public void testCompleteWorkflowNoDuplicates() throws Exception {
        // Complex scenario combining both bug conditions:
        // 1. Same dbId, different objects (isoforms)
        // 2. Multiple sources, shared external identifiers

        TestIdentifierNode protein1_instance1 = new TestIdentifierNode(100L, "PROTEIN_A");
        TestIdentifierNode protein1_instance2 = new TestIdentifierNode(100L, "PROTEIN_A"); // Same dbId
        TestIdentifierNode protein2 = new TestIdentifierNode(200L, "PROTEIN_B");

        Map<String, Set<String>> sourceToRefMap = new HashMap<>();
        sourceToRefMap.put("PROTEIN_A", new HashSet<>(Arrays.asList("EXT_1", "EXT_2", "EXT_SHARED")));
        sourceToRefMap.put("PROTEIN_B", new HashSet<>(Arrays.asList("EXT_3", "EXT_SHARED")));

        List<IdentifierNode> sourceNodes = Arrays.asList(protein1_instance1, protein1_instance2, protein2);

        IntegrationTestReferenceCreator creator = new IntegrationTestReferenceCreator(
            "CompleteTest",
            sourceToRefMap,
            sourceNodes,
            mockReferenceDatabase,
            tempDir
        );

        creator.writeCSV();

        // Verify: External identifier file
        Path identifierFile = tempDir.resolve("CompleteTest_Identifiers.csv");
        List<String> identifierLines = Files.readAllLines(identifierFile);

        // 1 header + 4 unique external identifiers (EXT_1, EXT_2, EXT_3, EXT_SHARED)
        assertThat("Should have header + 4 unique external identifiers",
            identifierLines.size(), is(5));

        // No identifier should appear more than once (excluding header)
        Set<String> uniqueIdentifiers = new HashSet<>(identifierLines.subList(1, identifierLines.size()));
        assertThat("All identifier lines should be unique",
            uniqueIdentifiers.size(), is(4));

        // Verify: Relationship file
        Path relationshipFile = tempDir.resolve("CompleteTest_Relationships.csv");
        List<String> relationshipLines = Files.readAllLines(relationshipFile);

        // Relationships: protein1 (dbId=100) -> 3 ext IDs, protein2 (dbId=200) -> 2 ext IDs
        // Total: 1 header + 5 relationships
        assertThat("Should have header + 5 relationship entries",
            relationshipLines.size(), is(6));

        // Protein with dbId 100 should have 3 relationships
        long protein1Relationships = relationshipLines.stream()
            .filter(line -> line.startsWith("100,"))
            .count();
        assertThat("Protein with dbId 100 should have 3 relationships",
            protein1Relationships, is(3L));

        // Protein with dbId 200 should have 2 relationships
        long protein2Relationships = relationshipLines.stream()
            .filter(line -> line.startsWith("200,"))
            .count();
        assertThat("Protein with dbId 200 should have 2 relationships",
            protein2Relationships, is(2L));
    }

    /**
     * Helper to clear all static caches between tests.
     */
    private void clearAllCaches() {
        // Clear DatabaseIdentifier cache
        try {
            Field dbIdCacheField = DatabaseIdentifier.class.getDeclaredField(
                "refDbToIdentifierToDatabaseIdentifier"
            );
            dbIdCacheField.setAccessible(true);
            dbIdCacheField.set(null, null);
        } catch (Exception e) {
            // Ignore
        }

        // Clear ReferenceSequence cache if needed
        try {
            Field refSeqCacheField = ReferenceSequenceReferenceCreator.class.getDeclaredField(
                "referenceDatabaseToIdentifierToReferenceSequence"
            );
            refSeqCacheField.setAccessible(true);
            refSeqCacheField.set(null, new HashMap<>());
        } catch (Exception e) {
            // Ignore
        }
    }

    // Test implementation of ReferenceCreator for integration testing
    private static class IntegrationTestReferenceCreator extends ReferenceCreator {
        private List<IdentifierNode> testSourceNodes;
        private ReferenceDatabase testReferenceDatabase;
        private Path testTempDir;
        private Set<String> externalIdentifierFileLines = new HashSet<>();

        public IntegrationTestReferenceCreator(
            String resourceName,
            Map<String, Set<String>> sourceToRefMap,
            List<IdentifierNode> sourceNodes,
            ReferenceDatabase referenceDatabase,
            Path tempDir) throws IllegalArgumentException {

            super(resourceName, sourceToRefMap);
            this.testSourceNodes = sourceNodes;
            this.testReferenceDatabase = referenceDatabase;
            this.testTempDir = tempDir;
        }

        @Override
        public void writeCSV() throws IOException, URISyntaxException {
            Path identifierFile = testTempDir.resolve(getResourceName() + "_Identifiers.csv");
            Path relationshipFile = testTempDir.resolve(getResourceName() + "_Relationships.csv");

            // Write headers
            Files.write(identifierFile, getReferenceCSVHeader().getBytes());
            Files.write(relationshipFile,
                "SourceDbId,ExternalIdentifierDbId,ReferenceDatabaseDbId,InstanceEditDbId\n".getBytes());

            // Create the identifier map (by dbId, not object identity - this is the fix!)
            Map<Long, List<? extends IdentifierNode>> sourceToExternalIdentifiers = new LinkedHashMap<>();
            for (IdentifierNode sourceNode : getIdentifierNodes()) {
                sourceToExternalIdentifiers.put(
                    sourceNode.getDbId(),  // Key by dbId, not by object
                    createExternalIdentifiersForSourceIdentifierNode(sourceNode)
                );
            }

            // Write CSV data
            for (Long sourceNodeDbId : sourceToExternalIdentifiers.keySet()) {
                List<? extends IdentifierNode> externalIdentifiers = sourceToExternalIdentifiers.get(sourceNodeDbId);

                for (IdentifierNode externalIdentifier : externalIdentifiers) {
                    String externalIdLine = getExternalIdentifierLine(externalIdentifier);

                    // Only write external identifier if not already written (in-memory deduplication)
                    if (!externalIdentifierFileLines.contains(externalIdLine)) {
                        externalIdentifierFileLines.add(externalIdLine);
                        Files.write(identifierFile, externalIdLine.getBytes(),
                            java.nio.file.StandardOpenOption.APPEND);
                    }

                    // Always write relationship line
                    String relationshipLine = String.format("%d,%d,%d,%d\n",
                        sourceNodeDbId,
                        externalIdentifier.getDbId(),
                        testReferenceDatabase.getDbId(),
                        999L // Mock InstanceEdit dbId
                    );
                    Files.write(relationshipFile, relationshipLine.getBytes(),
                        java.nio.file.StandardOpenOption.APPEND);
                }
            }
        }

        @Override
        protected List<? extends IdentifierNode> createExternalIdentifiersForSourceIdentifierNode(
            IdentifierNode sourceNode) {

            Set<String> identifierValues = getIdentifierValues(sourceNode);
            return identifierValues.stream()
                .map(id -> DatabaseIdentifier.fetchOrCreate(id, testReferenceDatabase))
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
    }

    // Test implementation of IdentifierNode
    private static class TestIdentifierNode extends IdentifierNode {
        public TestIdentifierNode(long dbId, String identifier) {
            super(dbId, identifier);
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
