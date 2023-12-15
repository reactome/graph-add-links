package org.reactome.graphdb;

import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/28/2021
 */
public class ReactomeGraphDatabase {
    private static Driver driver;
    private static Session session;

    //private static final int MAX_TRANSACTION_QUERIES = 3000;
    //private static int transactionQueryCount = 0;
    //private static Transaction currentTransaction;

    //private static List<Transaction> activeTransactions = new ArrayList<>();

    // Make class non-instantiable
    private ReactomeGraphDatabase() {}

    public static Session getSession() {
        if (session == null) {
            session = getDriver().session();
        }

        return session;
    }

//    public static Transaction getCurrentTransaction() {
//        //if (currentTransaction == null) {
//        //    currentTransaction = getSession().beginTransaction();
//        //}
//        //return currentTransaction;
//
//        if (currentTransaction == null) {
//            Transaction transaction = getSession().beginTransaction();
//            currentTransaction = transaction;
//            activeTransactions.add(transaction);
//            return transaction;
//        }
//
//        return currentTransaction;
//    }

//    public static void queue(String query) {
//
//        if (transactionQueryCount > MAX_TRANSACTION_QUERIES) {
//            commit();
////            while(transactionCommitting) {
////                try {
////                    // Wait until currentTransaction committed
////                    Thread.sleep(1000);
////                } catch (InterruptedException e) {
////                    throw new RuntimeException("Sleep while currentTransaction committing interrupted", e);
////                }
////            }
//        }
//        //Transaction transaction = getCurrentTransaction();
//        transaction.run(query);
//        transactionQueryCount += 1;
//    }

//    public static boolean commit() {
//        if (currentTransaction == null) {
//            return false;
//        }
//
//        System.out.println("Committing transaction...");
//        int currentTransactionIndex = activeTransactions.indexOf(currentTransaction);
//        currentTransaction.commitAsync().thenRun(() -> {
//            activeTransactions.get(currentTransactionIndex).close();
//            activeTransactions.remove(currentTransactionIndex);
//            System.out.println("Transaction committed");
//        });//.toCompletableFuture().join();
////        while (activeTransactions.size() > 30) {
////            try {
////                Thread.sleep(1000);
////            } catch (InterruptedException e) {
////                throw new RuntimeException("Sleep while waiting for transactions to complete interrupted",e);
////            }
////        }
//        currentTransaction = getDriver().session().beginTransaction();
//        activeTransactions.add(currentTransaction);
//        transactionQueryCount = 0;
//
//        return true;
//    }

//    public static void closeConnection() {
//        driver.close();
//        session.close();
//        driver = null;
//        session = null;
//    }

    private static Driver getDriver() {
        if (driver == null) {
            AuthToken connectionCredentials;
            try {
                connectionCredentials = getGraphDatabaseCredentials();
            } catch (IOException e) {
                throw new RuntimeException(
                    "Unable to read 'auth.properties' resource file to get graph database credentials", e
                );
            }

            driver = GraphDatabase.driver("bolt://localhost:7687", connectionCredentials);
        }
        return driver;
    }

    private static AuthToken getGraphDatabaseCredentials() throws IOException {
        return AuthTokens.basic(getUserName(), getPassword());
    }

    private static String getUserName() throws IOException {
        return getProperties().getProperty("user", "neo4j");
    }

    private static String getPassword() throws IOException {
        return getProperties().getProperty("password");
    }

    private static Properties getProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(getAuthFileInputStream());
        return properties;
    }

    private static InputStream getAuthFileInputStream() {
        return ReactomeGraphDatabase.class.getClassLoader().getResourceAsStream("auth.properties");
    }
}

