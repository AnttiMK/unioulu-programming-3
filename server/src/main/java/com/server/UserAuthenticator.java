package com.server;

import com.server.storage.MessageDatabase;
import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {

    private final MessageDatabase database;

    public UserAuthenticator(MessageDatabase database) {
        super("warning");
        this.database = database;
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        return database.checkCredentials(username, password);
    }

    public boolean register(String username, String password, String email) {
        return database.register(username, password, email);
    }

}
