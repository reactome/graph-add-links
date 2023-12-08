package org.reactome.graphnodes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 5/26/2022
 */
public class DatabaseIdentifierTest {
    private final String DUMMY_IDENTIFIER = "A12345";

    @Mock
    private ReferenceDatabase referenceDatabase;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void identifierExpectedRetrieved() {
        DatabaseIdentifier databaseIdentifier = getTestDatabaseIdentifier();
        assertThat(databaseIdentifier.getIdentifier(), is(equalTo(DUMMY_IDENTIFIER)));
    }

    @Test
    public void graphNodeLabelsExpectedRetrieved() {
        DatabaseIdentifier databaseIdentifier = getTestDatabaseIdentifier();
        assertThat(
            databaseIdentifier.getLabels(),
            contains("DatabaseObject", "DatabaseIdentifier")
        );
    }

    @Test
    public void toStringRetrievesCypherNodeRepresentation() {
        final String expectedToStringResult =
            "(n:DatabaseObject:DatabaseIdentifier {" +
            "dbId: -1," +
            "displayName: Dummy Reference Database:A12345," +
            "schemaClass: DatabaseIdentifier," +
            "databaseName: Dummy Reference Database,identifier: A12345" +
            "})";

        when(referenceDatabase.getDisplayName()).thenReturn("Dummy Reference Database");

        DatabaseIdentifier databaseIdentifier = getTestDatabaseIdentifier();
        assertThat(
            databaseIdentifier.toString(),
            is(equalTo(expectedToStringResult))
        );
    }

    @Test
    public void isNodeInDatabaseFalseBeforeInsertion() {
        final boolean expectedIsNodeInDatabaseForUncreatedNode = false;
        DatabaseIdentifier databaseIdentifier = getTestDatabaseIdentifier();

        assertThat(
            databaseIdentifier.isNodeInDatabase(),
            is(expectedIsNodeInDatabaseForUncreatedNode)
        );
    }

    private DatabaseIdentifier getTestDatabaseIdentifier() {
        return new DatabaseIdentifier(DUMMY_IDENTIFIER, referenceDatabase);
    }
}
