package com.server.storage;

import org.apache.commons.codec.digest.Crypt;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class MessageDatabase {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    private static final String DB_PATH = "database.db";
    private final SecureRandom random = new SecureRandom();
    private Connection connection;

    public MessageDatabase() {
        this.init();
    }

    public Connection getConnection() throws SQLException {
        return connection;
    }

    private void init() {
        boolean exists = new File(DB_PATH).exists();

        try {
            String url = "jdbc:sqlite:" + DB_PATH;
            this.connection = DriverManager.getConnection(url);
            if (exists) return;

            System.out.println("Database file not found, creating new database");
            runInitQuery(connection.prepareStatement(DBQueries.CREATE_TABLE_USERS));
            runInitQuery(connection.prepareStatement(DBQueries.CREATE_TABLE_MESSAGES));
            runInitQuery(connection.prepareStatement(DBQueries.INSERT_DUMMY_USER));
        } catch (SQLException e) {
            System.err.println("Error while initializing database: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void runInitQuery(PreparedStatement ps) {
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
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashedPw = rs.getString("password");
                return Crypt.crypt(password, hashedPw).equals(hashedPw);
            }
            return false;
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
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return false;
                }
            }

            byte[] bytes = new byte[13];
            random.nextBytes(bytes);
            String saltBytes = new String(Base64.getEncoder().encode(bytes));
            String salt = "$6$" + saltBytes;
            String hashedPw = Crypt.crypt(password, salt);

            try (PreparedStatement ps = connection.prepareStatement(DBQueries.INSERT_USER)) {
                ps.setString(1, username);
                ps.setString(2, hashedPw);
                ps.setString(3, email);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error while registering user");
            e.printStackTrace();
            return false;
        }
    }

    public void handleMessage(String nickname, double latitude, double longitude, long sent, String dangerType, String areaCode, String phoneNumber) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.INSERT_MESSAGE)) {
            ps.setString(1, nickname);
            ps.setDouble(2, latitude);
            ps.setDouble(3, longitude);
            ps.setLong(4, sent);
            ps.setString(5, dangerType);
            ps.setString(6, areaCode);
            ps.setString(7, phoneNumber);
            ps.executeUpdate();
        }
    }

    /**
     * {@return a JSONArray containing all of the messages in the database}
     *
     * @throws SQLException if an error occurs while querying the database
     */
    public JSONArray getMessages() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.GET_ALL_MESSAGES)) {
            // TODO refactor dupe code
            ResultSet rs = ps.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject json = new JSONObject();
                json.put("nickname", rs.getString("nickname"));
                json.put("latitude", rs.getDouble("latitude"));
                json.put("longitude", rs.getDouble("longitude"));
                String sentDate = Instant.ofEpochMilli(rs.getLong("sent")).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(DATE_PATTERN));
                json.put("sent", sentDate);
                json.put("dangertype", rs.getString("dangertype"));

                String areacode = rs.getString("areacode");
                if (areacode != null) {
                    json.put("areacode", areacode);
                }

                String phonenumber = rs.getString("phonenumber");
                if (phonenumber != null) {
                    json.put("phonenumber", phonenumber);
                }

                array.put(json);
            }
            return array;
        }
    }

    /**
     * Gets all the messages for a specific nickname
     *
     * @param nickname the nickname to get the messages for
     * @return a JSONArray containing the messages
     */
    public JSONArray getMessages(String nickname) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.GET_MESSAGES_BY_NICKNAME)) {
            ps.setString(1, nickname);
            // TODO refactor dupe code
            ResultSet rs = ps.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject json = new JSONObject();
                json.put("nickname", rs.getString("nickname"));
                json.put("latitude", rs.getDouble("latitude"));
                json.put("longitude", rs.getDouble("longitude"));
                String sentDate = Instant.ofEpochMilli(rs.getLong("sent")).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(DATE_PATTERN));
                json.put("sent", sentDate);
                json.put("dangertype", rs.getString("dangertype"));

                String areacode = rs.getString("areacode");
                if (areacode != null) {
                    json.put("areacode", areacode);
                }

                String phonenumber = rs.getString("phonenumber");
                if (phonenumber != null) {
                    json.put("phonenumber", phonenumber);
                }

                array.put(json);
            }
            return array;
        }
    }

    /**
     * Gets all messages sent inside a given time period
     *
     * @param timeStart the start of the time period
     * @param timeEnd   the end of the time period
     * @return a JSONArray containing the messages
     */
    public JSONArray getMessages(long timeStart, long timeEnd) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.GET_MESSAGES_BY_TIME)) {
            ps.setLong(1, timeStart);
            ps.setLong(2, timeEnd);
            // TODO refactor dupe code
            ResultSet rs = ps.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject json = new JSONObject();
                json.put("nickname", rs.getString("nickname"));
                json.put("latitude", rs.getDouble("latitude"));
                json.put("longitude", rs.getDouble("longitude"));
                String sentDate = Instant.ofEpochMilli(rs.getLong("sent")).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(DATE_PATTERN));
                json.put("sent", sentDate);
                json.put("dangertype", rs.getString("dangertype"));

                String areacode = rs.getString("areacode");
                if (areacode != null) {
                    json.put("areacode", areacode);
                }

                String phonenumber = rs.getString("phonenumber");
                if (phonenumber != null) {
                    json.put("phonenumber", phonenumber);
                }

                array.put(json);
            }
            return array;
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Error while closing database connection");
            e.printStackTrace();
        }
    }

}
