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

    private void runInitQuery(PreparedStatement ps) {
        try (ps) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error executing update");
            e.printStackTrace();
        }
    }

    /**
     * Checks if the given credentials exist in the database and are valid
     *
     * @param username the username to check
     * @param password the password to check
     * @return true if the credentials are valid, false otherwise
     */
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

    /**
     * Registers a new user in the database
     *
     * @param username the username
     * @param password the password
     * @param email    the email
     * @return true if the user was successfully registered, false if the username already exists
     */
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

    /**
     * Inserts a new warning message into the database
     *
     * @param nickname    the nickname of the user
     * @param latitude    the latitude of the position of the warning
     * @param longitude   the longitude of the position of the warning
     * @param sent        the time the warning was sent
     * @param dangerType  the type of danger
     * @param areaCode    the area code of the user
     * @param phoneNumber the phone number of the user
     * @param username    the username of the user
     * @throws SQLException if an error occurs while inserting the message
     */
    public void submitMessage(String nickname,
                              double latitude,
                              double longitude,
                              long sent,
                              String dangerType,
                              String areaCode,
                              String phoneNumber,
                              String username) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.INSERT_MESSAGE)) {
            ps.setString(1, nickname);
            ps.setDouble(2, latitude);
            ps.setDouble(3, longitude);
            ps.setLong(4, sent);
            ps.setString(5, dangerType);
            ps.setString(6, areaCode);
            ps.setString(7, phoneNumber);
            ps.setString(8, username);
            ps.executeUpdate();
        }
    }

    /**
     * Updates an existing warning message in the database
     *
     * @param id           the id of the message to update
     * @param nickname     the nickname of the user
     * @param latitude     the latitude of the position of the warning
     * @param longitude    the longitude of the position of the warning
     * @param sent         the time the warning was sent
     * @param dangerType   the type of danger
     * @param areaCode     the area code of the user
     * @param phoneNumber  the phone number of the user
     * @param updateReason the reason for the update
     * @param modified     the time the message was modified
     * @throws SQLException if an error occurs while updating the message
     */
    public void updateMessage(int id,
                              String nickname,
                              double latitude,
                              double longitude,
                              long sent,
                              String dangerType,
                              String areaCode,
                              String phoneNumber,
                              String updateReason,
                              long modified) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.UPDATE_MESSAGE)) {
            ps.setString(1, nickname);
            ps.setDouble(2, latitude);
            ps.setDouble(3, longitude);
            ps.setLong(4, sent);
            ps.setString(5, dangerType);
            ps.setString(6, areaCode);
            ps.setString(7, phoneNumber);
            ps.setString(8, updateReason);
            ps.setLong(9, modified);
            ps.setInt(10, id);
            ps.executeUpdate();
        }
    }

    /**
     * {@return a JSONArray containing all of the messages in the database}
     * @throws SQLException if an error occurs while querying the database
     */
    public JSONArray getMessages() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.GET_ALL_MESSAGES)) {
            ResultSet rs = ps.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                parseMsgToArray(rs, array);
            }
            return array;
        }
    }

    /**
     * Gets all the messages for a specific nickname
     *
     * @param nickname the nickname to get the messages for
     * @return a JSONArray containing the messages
     * @throws SQLException if an error occurs while querying the database
     */
    public JSONArray getMessages(String nickname) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.GET_MESSAGES_BY_NICKNAME)) {
            ps.setString(1, nickname);
            ResultSet rs = ps.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                parseMsgToArray(rs, array);
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
     * @throws SQLException if an error occurs while querying the database
     */
    public JSONArray getMessages(long timeStart, long timeEnd) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.GET_MESSAGES_BY_TIME)) {
            ps.setLong(1, timeStart);
            ps.setLong(2, timeEnd);
            ResultSet rs = ps.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                parseMsgToArray(rs, array);
            }
            return array;
        }
    }

    /**
     * Gets all warnings for a specific geographic area, using an artificial X/Y coordinate system.
     * The area is defined by the upper and lower limits of latitude and longitude, where
     * the latitude is the Y coordinate and the longitude is the X coordinate.
     * The values for longitude are "reversed", meaning that the upLongitude is the
     * smaller value on the X axis, and downLongitude is the larger value.
     *
     * @param upLatitude    the upper limit of the latitude
     * @param downLatitude  the lower limit of the latitude
     * @param upLongitude   the upper limit of the longitude
     * @param downLongitude the lower limit of the longitude
     * @return a JSONArray containing the messages
     * @throws SQLException if an error occurs while querying the database
     */
    public JSONArray getMessages(double upLatitude, double downLatitude, double upLongitude, double downLongitude) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.GET_MESSAGES_BY_AREA)) {
            ps.setDouble(1, upLatitude);
            ps.setDouble(2, downLatitude);
            ps.setDouble(3, upLongitude);
            ps.setDouble(4, downLongitude);
            ResultSet rs = ps.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                parseMsgToArray(rs, array);
            }
            return array;
        }
    }

    /**
     * Parses a message from a ResultSet and adds it to a JSONArray.
     * The ResultSet must not be empty (i.e. {@link ResultSet#next()} must have been called and returned true).
     *
     * @param rs    the ResultSet to parse the message from
     * @param array the JSONArray to add the message to
     * @throws SQLException if an error occurs while querying the database
     */
    private void parseMsgToArray(ResultSet rs, JSONArray array) throws SQLException {
        JSONObject json = new JSONObject();
        json.put("id", rs.getInt("id"));
        json.put("nickname", rs.getString("nickname"));
        json.put("latitude", rs.getDouble("latitude"));
        json.put("longitude", rs.getDouble("longitude"));
        String sentDate = epochMilliToDateString(rs.getLong("sent"));
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

        String updateReason = rs.getString("updatereason");
        if (updateReason != null) {
            json.put("updatereason", updateReason);
        }

        long modified = rs.getLong("modified");
        if (modified != 0) {
            json.put("modified", epochMilliToDateString(modified));
        }

        array.put(json);
    }

    private String epochMilliToDateString(long epochMs) {
        return Instant.ofEpochMilli(epochMs).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

    /**
     * Checks if the given username is the sender of the given message
     *
     * @param id       the id of the message
     * @param username the username to check
     * @return true if the username is the sender of the message, false otherwise
     */
    public boolean isSender(int id, String username) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DBQueries.IS_SENDER)) {
            ps.setInt(1, id);
            ps.setString(2, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
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
