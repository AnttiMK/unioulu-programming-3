package com.server;

import com.server.realm.RegistrationHandler;
import com.server.realm.WarningHandler;
import com.server.storage.MessageDatabase;
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
import java.util.concurrent.Executors;

public class Server {

    private Server() {
    }

    public static void main(String[] args) {
        try {
            MessageDatabase database = new MessageDatabase();
            //create the http server to port 8001 with default logger
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext ssl = serverSSLContext(args);

            server.setHttpsConfigurator(new HttpsConfigurator(ssl) {
                @Override
                public void configure(HttpsParameters params) {
                    SSLContext c = getSSLContext();
                    SSLParameters sslParams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslParams);
                }
            });

            UserAuthenticator auth = new UserAuthenticator(database);
            server.createContext("/warning", new WarningHandler(database)).setAuthenticator(auth);
            server.createContext("/registration", new RegistrationHandler(auth));

            server.setExecutor(Executors.newCachedThreadPool());
            addShutdownHook(server, database);
            server.start();
            System.out.println("Started web server on " + server.getAddress());
        } catch (FileNotFoundException e) {
            System.err.println("Keystore file not found in " + System.getProperty("user.dir"));
        } catch (Exception e) {
            System.err.println("Error starting server!");
            e.printStackTrace();
        }
    }

    private static SSLContext serverSSLContext(String[] args) throws Exception {
        final char[] passphrase;
        final String keyStoreFile;
        if (args.length == 2) {
            keyStoreFile = args[0];
            passphrase = args[1].toCharArray();
        } else {
            // If both args were not provided, use default keystore credentials
            keyStoreFile = "keystore.jks";
            passphrase = "password".toCharArray();
        }

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyStoreFile), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ssl;
    }

    private static void addShutdownHook(HttpsServer server, MessageDatabase database) {
        /*
         * Adds a shutdown hook to the process so that
         * the webserver and database can be shut down gracefully.
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop(0);
            database.close();
        }));
    }

}
