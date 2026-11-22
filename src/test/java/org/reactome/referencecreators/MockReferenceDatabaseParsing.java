package org.reactome.referencecreators;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockedStatic;
import org.reactome.graphnodes.ReferenceDatabase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Mocks ReferenceDatabase.parseReferenceDatabase for each test, so a ReferenceCreator can be constructed with a
 * resource name that isn't in identifier-resources.json. Each resource name gets its own mock ReferenceDatabase,
 * whose getDbIdInGraphDatabase returns null, so no graph database is needed.
 */
public class MockReferenceDatabaseParsing implements BeforeEachCallback, AfterEachCallback {
    private static final ExtensionContext.Namespace NAMESPACE =
        ExtensionContext.Namespace.create(MockReferenceDatabaseParsing.class);
    private static final String MOCKED_STATIC = "mockedStatic";

    @Override
    public void beforeEach(ExtensionContext context) {
        Map<String, ReferenceDatabase> referenceDatabaseByName = new HashMap<>();

        MockedStatic<ReferenceDatabase> mockedStatic = mockStatic(ReferenceDatabase.class);
        mockedStatic.when(() -> ReferenceDatabase.parseReferenceDatabase(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            if (!referenceDatabaseByName.containsKey(name)) {
                referenceDatabaseByName.put(name, referenceDatabaseNamed(name, 1000L + referenceDatabaseByName.size()));
            }
            return referenceDatabaseByName.get(name);
        });

        context.getStore(NAMESPACE).put(MOCKED_STATIC, mockedStatic);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        MockedStatic<?> mockedStatic = context.getStore(NAMESPACE).remove(MOCKED_STATIC, MockedStatic.class);
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

    private static ReferenceDatabase referenceDatabaseNamed(String name, long dbId) {
        ReferenceDatabase referenceDatabase = mock(ReferenceDatabase.class);
        when(referenceDatabase.getDisplayName()).thenReturn(name);
        when(referenceDatabase.getNames()).thenReturn(Collections.singletonList(name));
        when(referenceDatabase.getDbId()).thenReturn(dbId);
        when(referenceDatabase.getAccessURL()).thenReturn("http://test.db/###ID###");
        return referenceDatabase;
    }
}
