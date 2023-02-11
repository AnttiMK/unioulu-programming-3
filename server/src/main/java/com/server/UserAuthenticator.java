package com.server;

import com.sun.net.httpserver.BasicAuthenticator;

import java.util.HashMap;
import java.util.Map;

public class UserAuthenticator extends BasicAuthenticator {

    private final Map<String, String> users;

    public UserAuthenticator() {
        super("warning");
        this.users = new HashMap<>();
        users.put("dummy", "passwd");
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }

}
