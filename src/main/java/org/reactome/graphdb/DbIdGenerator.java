package org.reactome.graphdb;

import org.neo4j.driver.v1.StatementResult;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 5/20/2022
 */
public class DbIdGenerator {
    private static long currentMaxDbId = -1;

    private DbIdGenerator() {}

    public static long getNextDbId() {
        if (currentMaxDbId == -1) {
            currentMaxDbId = getMaxDbIdFromDatabase();
        }

        currentMaxDbId += 1;
        return currentMaxDbId;
    }

    private static long getMaxDbIdFromDatabase() {
        final String returnValueName = "maxDbId";

        StatementResult maxDbIdResult =
            ReactomeGraphDatabase.getSession().run("MATCH (n:DatabaseObject) RETURN max(n.dbId) as " + returnValueName);

        if (!maxDbIdResult.hasNext()) {
            throw new IllegalStateException("Can not find largest dbId from graph database");
        }

        return maxDbIdResult.next().get(returnValueName).asLong();
    }
}
