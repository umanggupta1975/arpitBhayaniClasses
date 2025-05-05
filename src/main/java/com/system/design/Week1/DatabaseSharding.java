package com.system.design.Week1;

import java.sql.*;
import java.util.Properties;
import java.util.Random;

public class DatabaseSharding {
    private static Connection[] connections;

    private static Connection createPostgresConnection() throws SQLException {
        final String url = "jdbc:postgresql://localhost:5432/postgres?targetServerType=primary";
        final Properties props = new Properties();
        props.setProperty("user", "root");
        props.setProperty("password", "root");

        return DriverManager.getConnection(url, props);
    }

    private static Connection createMySqlConnection() throws SQLException {
        final String url = "jdbc:mysql://localhost:3306/mysql?targetServerType=primary";
        final Properties props = new Properties();
        props.setProperty("user", "root");
        props.setProperty("password", "root");

        return DriverManager.getConnection(url, props);
    }
    private static Connection getShard(int id){
        return connections[id % 2];
    }

    private static int insertData(int id, String name) throws SQLException {
        Connection connection = getShard(id);
        connection.createStatement().execute("INSERT INTO test (id, name) VALUES (" + id + ", '" + name + "')");
        System.out.println("Inserted data into shard " + connection.getMetaData().getDatabaseProductName());
        return id;
    }

    private static void findById(int id) throws SQLException {
        Connection connection = getShard(id);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM test WHERE id = " + id);

        while (resultSet.next()) {
            int retrievedId = resultSet.getInt("id");
            String name = resultSet.getString("name");
            System.out.println("Found data: id = " + retrievedId + ", name = " + name +
                    " from shard " + connection.getMetaData().getDatabaseProductName());
        }

        resultSet.close();
        statement.close();
    }

    private static void databaseSharding() throws SQLException {
        Random r = new Random();
        int id1 = insertData(r.nextInt(100), "John Doe");
        int id2 = insertData(r.nextInt(100), "Jane Doe");
        int id3 = insertData(r.nextInt(100), "Alice Smith");
        int id4 = insertData(r.nextInt(100), "Bob Johnson");
        int id5 = insertData(r.nextInt(100), "Charlie Brown");

        findById(id1);
        findById(id3);
        findById(id4);
    }
    public static void main(String[] args) throws Exception {
        connections = new Connection[2];
        connections[0] = createMySqlConnection();
        connections[1] = createPostgresConnection();

        connections[0].createStatement().execute("DROP TABLE IF EXISTS test");
        connections[0].createStatement().execute("Create table test (id int primary key, name varchar(255))");

        connections[1].createStatement().execute("DROP TABLE IF EXISTS test");
        connections[1].createStatement().execute("Create table test (id int primary key, name varchar(255))");

        databaseSharding();
    }
}
