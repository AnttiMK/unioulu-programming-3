package com.server.storage;

public class DBQueries {

    public static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS users ("
            + "username TEXT PRIMARY KEY NOT NULL,"
            + "password TEXT NOT NULL,"
            + "email TEXT NOT NULL"
            + ")";

    public static final String CREATE_TABLE_MESSAGES = "CREATE TABLE IF NOT EXISTS messages ("
            + "id INTEGER PRIMARY KEY,"
            + "nickname TEXT NOT NULL,"
            + "latitude DOUBLE NOT NULL,"
            + "longitude DOUBLE NOT NULL,"
            + "sent INTEGER NOT NULL,"
            + "dangertype TEXT NOT NULL,"
            + "areacode TEXT,"
            + "phonenumber TEXT,"
            + "updatereason TEXT,"
            + "modified TEXT"
            + ")";

    public static final String INSERT_DUMMY_USER = "INSERT INTO users (username, password, email) "
            + "VALUES ('dummy', 'password', 'dummy@example.com')";

    public static final String CHECK_CREDENTIALS = "SELECT * FROM users WHERE username = ?";

    public static final String INSERT_USER = "INSERT INTO users (username, password, email) "
            + "VALUES (?, ?, ?)";

    public static final String CHECK_USER_EXISTS = "SELECT * FROM users WHERE username = ?";

    public static final String INSERT_MESSAGE = "INSERT INTO messages (nickname, latitude, longitude, sent, dangertype, areacode, phonenumber) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_MESSAGE = "UPDATE messages SET nickname = ?, latitude = ?, longitude = ?, sent = ?, dangertype = ?, areacode = ?, phonenumber = ?, updatereason = ?, modified = ? WHERE id = ?";

    public static final String GET_ALL_MESSAGES = "SELECT * FROM messages";

    public static final String GET_MESSAGES_BY_NICKNAME = "SELECT * FROM messages WHERE nickname = ?";

    public static final String GET_MESSAGES_BY_TIME = "SELECT * FROM messages WHERE sent >= ? AND sent <= ?";
}
