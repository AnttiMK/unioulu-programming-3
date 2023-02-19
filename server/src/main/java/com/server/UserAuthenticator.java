package com.server;

import com.sun.net.httpserver.BasicAuthenticator;

import java.util.HashMap;
import java.util.Map;

public class UserAuthenticator extends BasicAuthenticator {

    private final Map<String, User> users;

    public UserAuthenticator() {
        super("warning");
        this.users = new HashMap<>();
        users.put("dummy", new User("dummy", "password", "dummy@example.com"));
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        return users.containsKey(username) && users.get(username).getPassword().equals(password);
    }

    public boolean register(String username, String password, String email) {
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, new User(username, password, email));
        return true;
    }

}
