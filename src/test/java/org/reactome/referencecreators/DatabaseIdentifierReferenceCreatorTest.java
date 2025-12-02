package org.reactome.referencecreators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactome.graphnodes.DatabaseIdentifier;
import org.reactome.graphnodes.IdentifierNode;
import org.reactome.graphnodes.ReferenceDatabase;

import java.lang.reflect.Field;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for DatabaseIdentifierReferenceCreator to ensure proper use of
 * fetchOrCreate pattern and duplicate prevention.
 *
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 11/03/2025
 */
public class DatabaseIdentifierReferenceCreatorTest {

    @Mock
    private ReferenceDatabase mockReferenceDatabase;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockReferenceDatabase.getDisplayName()).thenReturn("TestDB");
        when(mockReferenceDatabase.getDbId()).thenReturn(100L);

        // Clear the static cache between tests
        clearDatabaseIdentifierCache();
    }

    /**
     * Tests that DatabaseIdentifier.fetchOrCreate returns the same instance
     * when called multiple times with the same identifier and reference database.
     */
    @Test
    public void testFetchOrCreateReturnsSameInstanceForSameIdentifier() {
        DatabaseIdentifier first = DatabaseIdentifier.fetchOrCreate("TEST_ID_1", mockReferenceDatabase);
        DatabaseIdentifier second = DatabaseIdentifier.fetchOrCreate("TEST_ID_1", mockReferenceDatabase);

        // Should return the exact same instance
        assertThat(first, is(sameInstance(second)));
        assertThat(first.getIdentifier(), is(equalTo("TEST_ID_1")));
    }

    /**
     * Tests that different identifiers create different DatabaseIdentifier objects.
     */
    @Test
    public void testDifferentIdentifiersCreateDifferentObjects() {
        DatabaseIdentifier id1 = DatabaseIdentifier.fetchOrCreate("TEST_ID_1", mockReferenceDatabase);
        DatabaseIdentifier id2 = DatabaseIdentifier.fetchOrCreate("TEST_ID_2", mockReferenceDatabase);

        assertThat(id1, is(not(sameInstance(id2))));
        assertThat(id1.getIdentifier(), is(equalTo("TEST_ID_1")));
        assertThat(id2.getIdentifier(), is(equalTo("TEST_ID_2")));
    }

    /**
     * Tests that DatabaseIdentifiers are cached per ReferenceDatabase.
     * Same identifier with different reference databases should create different objects.
     */
    @Test
    public void testDatabaseIdentifiersCachedPerReferenceDatabase() {
        ReferenceDatabase refDb1 = createMockReferenceDatabase("DB1", 100L);
        ReferenceDatabase refDb2 = createMockReferenceDatabase("DB2", 200L);

        DatabaseIdentifier fromDb1 = DatabaseIdentifier.fetchOrCreate("SHARED_ID", refDb1);
        DatabaseIdentifier fromDb2 = DatabaseIdentifier.fetchOrCreate("SHARED_ID", refDb2);

        // Should be different instances because they're from different reference databases
        assertThat(fromDb1, is(not(sameInstance(fromDb2))));

        // But fetching again from the same database should return the cached instance
        DatabaseIdentifier fromDb1Again = DatabaseIdentifier.fetchOrCreate("SHARED_ID", refDb1);
        assertThat(fromDb1, is(sameInstance(fromDb1Again)));
    }

    /**
     * Tests that fetchExternalIdentifiersForSourceIdentifierNode uses the
     * fetchOrCreate pattern to prevent duplicate DatabaseIdentifiers.
     */
    @Test
    public void testFetchExternalIdentifiersUsesCaching() {
        Map<String, Set<String>> sourceToRefMap = new HashMap<>();
        // Two different sources mapping to the same external identifier
        sourceToRefMap.put("SOURCE_1", new HashSet<>(Arrays.asList("EXT_ID_1", "EXT_ID_2")));
        sourceToRefMap.put("SOURCE_2", new HashSet<>(Arrays.asList("EXT_ID_1", "EXT_ID_3")));

        TestDatabaseIdentifierReferenceCreator creator = new TestDatabaseIdentifierReferenceCreator(
            "TestResource",
            sourceToRefMap,
            mockReferenceDatabase
        );

        TestIdentifierNode source1 = new TestIdentifierNode(1L, "SOURCE_1");
        TestIdentifierNode source2 = new TestIdentifierNode(2L, "SOURCE_2");

        List<? extends IdentifierNode> fromSource1 = creator.testFetchExternalIdentifiers(source1);
        List<? extends IdentifierNode> fromSource2 = creator.testFetchExternalIdentifiers(source2);

        // Find EXT_ID_1 from both lists
        DatabaseIdentifier extId1FromSource1 = (DatabaseIdentifier) fromSource1.stream()
            .filter(id -> id.getIdentifier().equals("EXT_ID_1"))
            .findFirst()
            .orElse(null);

        DatabaseIdentifier extId1FromSource2 = (DatabaseIdentifier) fromSource2.stream()
            .filter(id -> id.getIdentifier().equals("EXT_ID_1"))
            .findFirst()
            .orElse(null);

        // Should be the same instance due to fetchOrCreate caching
        assertThat(extId1FromSource1, is(notNullValue()));
        assertThat(extId1FromSource2, is(notNullValue()));
        assertThat(extId1FromSource1, is(sameInstance(extId1FromSource2)));
    }

    /**
     * Tests that multiple calls to fetch external identifiers don't create
     * duplicate DatabaseIdentifier objects in memory.
     */
    @Test
    public void testNoDuplicateDatabaseIdentifiersInMemory() {
        Map<String, Set<String>> sourceToRefMap = new HashMap<>();
        sourceToRefMap.put("SOURCE_1", new HashSet<>(Collections.singletonList("SHARED_EXT_ID")));

        TestDatabaseIdentifierReferenceCreator creator = new TestDatabaseIdentifierReferenceCreator(
            "TestResource",
            sourceToRefMap,
            mockReferenceDatabase
        );

        TestIdentifierNode source = new TestIdentifierNode(1L, "SOURCE_1");

        // Fetch multiple times
        List<? extends IdentifierNode> firstFetch = creator.testFetchExternalIdentifiers(source);
        List<? extends IdentifierNode> secondFetch = creator.testFetchExternalIdentifiers(source);
        List<? extends IdentifierNode> thirdFetch = creator.testFetchExternalIdentifiers(source);

        // All should return the same instance for the same identifier
        assertThat(firstFetch.get(0), is(sameInstance(secondFetch.get(0))));
        assertThat(secondFetch.get(0), is(sameInstance(thirdFetch.get(0))));
    }

    /**
     * Helper method to create a mock ReferenceDatabase for testing.
     */
    private ReferenceDatabase createMockReferenceDatabase(String displayName, long dbId) {
        ReferenceDatabase mockDb = org.mockito.Mockito.mock(ReferenceDatabase.class);
        when(mockDb.getDisplayName()).thenReturn(displayName);
        when(mockDb.getDbId()).thenReturn(dbId);
        return mockDb;
    }

    /**
     * Helper method to clear the static DatabaseIdentifier cache between tests.
     */
    private void clearDatabaseIdentifierCache() {
        try {
            Field cacheField = DatabaseIdentifier.class.getDeclaredField(
                "refDbToIdentifierToDatabaseIdentifier"
            );
            cacheField.setAccessible(true);
            cacheField.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // If the field doesn't exist or can't be accessed, that's fine
        }
    }

    // Test implementation of DatabaseIdentifierReferenceCreator
    private static class TestDatabaseIdentifierReferenceCreator extends DatabaseIdentifierReferenceCreator {
        private ReferenceDatabase testReferenceDatabase;

        public TestDatabaseIdentifierReferenceCreator(
            String referenceName,
            Map<String, Set<String>> sourceIdentifierToReferenceIdentifiers,
            ReferenceDatabase referenceDatabase) throws IllegalArgumentException {

            super(referenceName, sourceIdentifierToReferenceIdentifiers);
            this.testReferenceDatabase = referenceDatabase;
        }

        @Override
        public ReferenceDatabase getReferenceDatabase() {
            return testReferenceDatabase;
        }

        @Override
        protected List<? extends IdentifierNode> getIdentifierNodes() {
            return Collections.emptyList();
        }

        // Expose the protected method for testing
        public List<? extends IdentifierNode> testFetchExternalIdentifiers(IdentifierNode sourceNode) {
            return createExternalIdentifiersForSourceIdentifierNode(sourceNode);
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
