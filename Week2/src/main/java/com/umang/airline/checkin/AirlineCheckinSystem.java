package com.umang.airline.checkin;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AirlineCheckinSystem {
    public static Connection createConnection() throws SQLException {
        final String url = "jdbc:postgresql://localhost:5432/postgres?targetServerType=primary";
        final Properties props = new Properties();
        props.setProperty("user", "root");
        props.setProperty("password", "root");

        return DriverManager.getConnection(url, props);
    }

    // Naive check-in method
    public static void checkInPassenger1(Connection connection, int passengerId) {
        try {
            connection.setAutoCommit(false);

            String selectQuery = "SELECT seat_id FROM airlineCheckin.seats WHERE assignedPassenger IS NULL order by seat_id LIMIT 1 FOR UPDATE SKIP LOCKED";
            String updateQuery = "UPDATE airlineCheckin.seats SET assignedPassenger = ? WHERE seat_id = ?";

            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                 PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    int seatId = rs.getInt("seat_id");

                    updateStmt.setInt(1, passengerId);
                    updateStmt.setInt(2, seatId);
                    updateStmt.executeUpdate();

                    connection.commit();
                    System.out.println("Passenger " + passengerId + " checked in to seat " + seatId);
                } else {
                    System.out.println("No available seats.");
                }
            } catch (SQLException e) {
                connection.rollback();
                System.err.println("Transaction failed, rolled back. Error: " + e.getMessage());
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        clearAllSeats();
        ExecutorService executorService = Executors.newFixedThreadPool(60);

        for (int id = 1; id <= 120; id++) {
            int finalId = id;
            executorService.submit(() -> {
                try (Connection connection = createConnection()) {
                    checkInPassenger1(connection, finalId);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES);

        printSeatAssignments();
        long endTime = System.currentTimeMillis();
        System.out.println("Total time taken: " + (endTime - startTime) + " ms");
    }

    private static void clearAllSeats() {
        try (Connection connection = createConnection()) {
            String updateQuery = "UPDATE airlineCheckin.seats SET assignedPassenger = NULL";

            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                int rowsUpdated = updateStmt.executeUpdate();
                System.out.println("Cleared seat assignments for " + rowsUpdated + " seats.");
            }
        } catch (SQLException e) {
            System.err.println("Error clearing seat assignments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printSeatAssignments() {
        try (Connection connection = createConnection()) {
            String query = "SELECT seat_number, assignedPassenger FROM airlineCheckin.seats ORDER BY seat_id";

            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                System.out.println("Seat Assignments:");
                System.out.println("=================");

                while (rs.next()) {
                    String seatNumber = rs.getString("seat_number");
                    Integer assignedPassenger = rs.getInt("assignedPassenger");

                    // Check if assignedPassenger is NULL
                    if (rs.wasNull()) {
                        System.out.println(seatNumber + " -> NULL");
                    } else {
                        System.out.println(seatNumber + " -> " + assignedPassenger);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error printing seat assignments: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
