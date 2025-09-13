package com.umang.Week1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

public class ConnectionPool {
    public static Connection createConnection() throws SQLException {
        final String url = "jdbc:postgresql://localhost:5432/postgres?targetServerType=primary";
        final Properties props = new Properties();
        props.setProperty("user", "root");
        props.setProperty("password", "root");

        return DriverManager.getConnection(url, props);
    }

    public static void connectionPool(int n) throws SQLException, InterruptedException {
        long startTime = System.currentTimeMillis();

        int connectionPoolSize = 10;
        BlockingQueue<Connection> blockingQueue = new ArrayBlockingQueue<>(connectionPoolSize);
        for (int i = 0; i < connectionPoolSize; i++) {
            blockingQueue.put(createConnection());
        }

        ExecutorService executorService = Executors.newFixedThreadPool(n);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Future<?> future = executorService.submit(() -> {
                try {
                    Connection conn = blockingQueue.take();
                    conn.createStatement().execute("Select pg_sleep(0.01)");
                    blockingQueue.put(conn);
                } catch (SQLException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                executorService.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken to create " + n + " connections: " + (endTime - startTime) + " ms");
    }

    public static void nonConnectionPool(int n) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(n);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Future<?> future = executorService.submit(() -> {
                try {
                    Connection conn = createConnection();
                    conn.createStatement().execute("Select pg_sleep(0.01)");
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                executorService.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken to create " + n + " connections: " + (endTime - startTime) + " ms");
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        connectionPool(1000);
        nonConnectionPool(200);
    }
}
