package com.server.storage;

import java.io.File;
import java.sql.*;

public class MessageDatabase {

    private static final String DB_PATH = "database.db";
    private Connection connection;

    public MessageDatabase() {
        this.init();
    }

    public Connection getConnection() throws SQLException {
        return connection;
    }

    private void init() {
        boolean exists = new File(DB_PATH).exists();
        if (exists) return;

        try {
            String url = "jdbc:sqlite:" + DB_PATH;
            this.connection = DriverManager.getConnection(url);
            System.out.println("Database file not found, creating new database");
            executeUpdate(connection.prepareStatement(DBQueries.CREATE_TABLE_USERS));
            executeUpdate(connection.prepareStatement(DBQueries.CREATE_TABLE_MESSAGES));
            executeUpdate(connection.prepareStatement(DBQueries.INSERT_DUMMY_USER));
        } catch (SQLException e) {
            System.err.println("Error while initializing database: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public ResultSet query(PreparedStatement ps) {
        try (ps) {
            ps.execute();
            return ps.getResultSet();
        } catch (SQLException e) {
            System.err.println("Error executing query");
            e.printStackTrace();
            return null;
        }
    }

    public void executeUpdate(PreparedStatement ps) {
        try (ps) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error executing update");
            e.printStackTrace();
        }
    }

    public boolean checkCredentials(String username, String password) {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.CHECK_CREDENTIALS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = query(ps);
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking credentials");
            e.printStackTrace();
            return false;
        }
    }

    public boolean register(String username, String password, String email) {
        try {
            try (PreparedStatement ps = connection.prepareStatement(DBQueries.CHECK_USER_EXISTS)) {
                ps.setString(1, username);
                ResultSet rs = query(ps);
                if (rs.next()) {
                    return false;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(DBQueries.INSERT_USER)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, email);
                executeUpdate(ps);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error while registering user");
            e.printStackTrace();
            return false;
        }
    }

    public void handleMessage(String nickname, double latitude, double longitude, long sent, String dangerType) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.INSERT_MESSAGE)) {
            ps.setString(1, nickname);
            ps.setDouble(2, latitude);
            ps.setDouble(3, longitude);
            ps.setLong(4, sent);
            ps.setString(5, dangerType);
            ps.executeUpdate();
        }
    }

    public ResultSet getMessages() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.GET_ALL_MESSAGES)) {
            return ps.executeQuery();
        }
    }

}
