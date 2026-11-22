package org.reactome.graphdb;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockedStatic;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Mocks ReactomeGraphDatabase.getSession for each test, so code that queries the graph database runs without
 * Neo4j. Every query returns one record whose values are MAX_DB_ID, which is what DbIdGenerator reads as the
 * largest dbId in the database when a new node is created. Writes (run and writeTransaction) do nothing.
 */
public class MockGraphDatabaseSession implements BeforeEachCallback, AfterEachCallback {
    public static final long MAX_DB_ID = 1_000_000L;

    private static final ExtensionContext.Namespace NAMESPACE =
        ExtensionContext.Namespace.create(MockGraphDatabaseSession.class);
    private static final String MOCKED_STATIC = "mockedStatic";

    @Override
    public void beforeEach(ExtensionContext context) {
        Value value = mock(Value.class);
        when(value.asLong()).thenReturn(MAX_DB_ID);

        Record record = mock(Record.class);
        when(record.get(anyString())).thenReturn(value);

        Result result = mock(Result.class);
        when(result.hasNext()).thenReturn(true);
        when(result.next()).thenReturn(record);

        Session session = mock(Session.class);
        when(session.run(anyString())).thenReturn(result);

        MockedStatic<ReactomeGraphDatabase> mockedStatic = mockStatic(ReactomeGraphDatabase.class);
        mockedStatic.when(ReactomeGraphDatabase::getSession).thenReturn(session);

        context.getStore(NAMESPACE).put(MOCKED_STATIC, mockedStatic);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        MockedStatic<?> mockedStatic = context.getStore(NAMESPACE).remove(MOCKED_STATIC, MockedStatic.class);
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }
}
