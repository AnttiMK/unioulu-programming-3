package com.server;

import com.server.realm.Warning;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class Server {

    private Server() {
    }

    public static void main(String[] args) throws Exception {
        //create the http server to port 8001 with default logger
        HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
        SSLContext ssl = serverSSLContext();

        server.setHttpsConfigurator(new HttpsConfigurator(ssl) {
            @Override
            public void configure(HttpsParameters params) {
                SSLContext c = getSSLContext();
                SSLParameters sslParams = c.getDefaultSSLParameters();
                params.setSSLParameters(sslParams);
            }
        });

        //create context that defines path for the resource, in this case a "help"
        server.createContext("/warning", new Warning()).setAuthenticator(new UserAuthenticator());
        // creates a default executor
        server.setExecutor(null);
        server.start();
        System.out.println("Started web server on " + server.getAddress());
    }

    private static SSLContext serverSSLContext() throws Exception {
        char[] passphrase = "password".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        final String keyStoreFile = "keystore.jks";
        try {
            ks.load(new FileInputStream(keyStoreFile), passphrase);
        } catch (FileNotFoundException e) {
            System.err.println("Keystore file " + keyStoreFile + " not found in " + System.getProperty("user.dir") + "!");
            System.exit(1);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ssl;
    }

}
