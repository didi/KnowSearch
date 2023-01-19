/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.authc.esnative.tool;

import org.elasticsearch.common.CheckedFunction;
import org.elasticsearch.common.CheckedSupplier;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.lease.Releasables;
import org.elasticsearch.common.network.InetAddresses;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.SecureString;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.env.Environment;
import org.elasticsearch.xpack.core.XPackSettings;
import org.elasticsearch.xpack.core.common.socket.SocketAccess;
import org.elasticsearch.xpack.core.security.authc.support.UsernamePasswordToken;
import org.elasticsearch.xpack.core.ssl.SSLConfiguration;
import org.elasticsearch.xpack.core.ssl.SSLService;
import org.elasticsearch.xpack.security.authc.esnative.tool.HttpResponse.HttpResponseBuilder;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;

import static org.elasticsearch.http.HttpTransportSettings.SETTING_HTTP_PORT;
import static org.elasticsearch.http.HttpTransportSettings.SETTING_HTTP_PUBLISH_HOST;
import static org.elasticsearch.http.HttpTransportSettings.SETTING_HTTP_PUBLISH_PORT;

/**
 * A simple http client for usage in command line tools. This client only uses internal jdk classes and does
 * not rely on an external http libraries.
 */
public class CommandLineHttpClient {

    /**
     * Timeout HTTP(s) reads after 35 seconds.
     * The default timeout for discovering a master is 30s, and we want to be longer than this, otherwise a querying a disconnected node
     * will trigger as client side timeout rather than giving clear error details.
     */
    private static final int READ_TIMEOUT = 35 * 1000;

    private final Settings settings;
    private final Environment env;

    public CommandLineHttpClient(Settings settings, Environment env) {
        this.settings = settings;
        this.env = env;
    }

    /**
     * General purpose HTTP(S) call with JSON Content-Type and Authorization Header.
     * SSL settings are read from the settings file, if any.
     *
     * @param user
     *            user in the authorization header.
     * @param password
     *            password in the authorization header.
     * @param requestBodySupplier
     *            supplier for the JSON string body of the request.
     * @param responseHandler
     *            handler of the response Input Stream.
     * @return HTTP protocol response code.
     */
    @SuppressForbidden(reason = "We call connect in doPrivileged and provide SocketPermission")
    public HttpResponse execute(String method, URL url, String user, SecureString password,
            CheckedSupplier<String, Exception> requestBodySupplier,
            CheckedFunction<InputStream, HttpResponseBuilder, Exception> responseHandler) throws Exception {
        final HttpURLConnection conn;
        // If using SSL, need a custom service because it's likely a self-signed certificate
        if ("https".equalsIgnoreCase(url.getProtocol())) {
            final SSLService sslService = new SSLService(settings, env);
            final HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                final SSLConfiguration sslConfiguration = sslService.getHttpTransportSSLConfiguration();
                // Requires permission java.lang.RuntimePermission "setFactory";
                httpsConn.setSSLSocketFactory(sslService.sslSocketFactory(sslConfiguration));
                final boolean isHostnameVerificationEnabled = sslConfiguration.verificationMode().isHostnameVerificationEnabled();
                if (isHostnameVerificationEnabled == false) {
                    httpsConn.setHostnameVerifier((hostname, session) -> true);
                }
                return null;
            });
            conn = httpsConn;
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }
        conn.setRequestMethod(method);
        conn.setReadTimeout(READ_TIMEOUT);
        // Add basic-auth header
        String token = UsernamePasswordToken.basicAuthHeaderValue(user, password);
        conn.setRequestProperty("Authorization", token);
        conn.setRequestProperty("Content-Type", XContentType.JSON.mediaType());
        String bodyString = requestBodySupplier.get();
        conn.setDoOutput(bodyString != null); // set true if we are sending a body
        SocketAccess.doPrivileged(conn::connect);
        if (bodyString != null) {
            try (OutputStream out = conn.getOutputStream()) {
                out.write(bodyString.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                Releasables.closeWhileHandlingException(conn::disconnect);
                throw e;
            }
        }
        // this throws IOException if there is a network problem
        final int responseCode = conn.getResponseCode();
        HttpResponseBuilder responseBuilder = null;
        try (InputStream inputStream = conn.getInputStream()) {
            responseBuilder = responseHandler.apply(inputStream);
        } catch (IOException e) {
            // this IOException is if the HTTP response code is 'BAD' (>= 400)
            try (InputStream errorStream = conn.getErrorStream()) {
                responseBuilder = responseHandler.apply(errorStream);
            }
        } finally {
            Releasables.closeWhileHandlingException(conn::disconnect);
        }
        responseBuilder.withHttpStatus(responseCode);
        return responseBuilder.build();
    }

    String getDefaultURL() {
        final String scheme = XPackSettings.HTTP_SSL_ENABLED.get(settings) ? "https" : "http";
        List<String> httpPublishHost = SETTING_HTTP_PUBLISH_HOST.get(settings);
        if (httpPublishHost.isEmpty()) {
            httpPublishHost = NetworkService.GLOBAL_NETWORK_PUBLISH_HOST_SETTING.get(settings);
        }

        // we cannot do custom name resolution here...
        NetworkService networkService = new NetworkService(Collections.emptyList());
        try {
            InetAddress publishAddress = networkService.resolvePublishHostAddresses(httpPublishHost.toArray(Strings.EMPTY_ARRAY));
            int port = SETTING_HTTP_PUBLISH_PORT.get(settings);
            if (port <= 0) {
                int[] ports = SETTING_HTTP_PORT.get(settings).ports();
                if (ports.length > 0) {
                    port = ports[0];
                }

                // this sucks but a port can be specified with a value of 0, we'll never be able to connect to it so just default to
                // what we know
                if (port <= 0) {
                    throw new IllegalStateException("unable to determine http port from settings");
                }
            }
            return scheme + "://" + InetAddresses.toUriString(publishAddress) + ":" + port;
        } catch (Exception e) {
            throw new IllegalStateException("unable to determine default URL from settings, please use the -u option to explicitly " +
                "provide the url", e);
        }
    }

}
