/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.authc.esnative;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.elasticsearch.cli.MockTerminal;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.test.NativeRealmIntegTestCase;
import org.elasticsearch.common.CharArrays;
import org.elasticsearch.xpack.core.XPackSettings;
import org.elasticsearch.xpack.core.security.client.SecurityClient;
import org.elasticsearch.xpack.core.security.index.RestrictedIndicesNames;
import org.junit.BeforeClass;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.elasticsearch.test.SecuritySettingsSource.addSSLSettingsForNodePEMFiles;
import static org.elasticsearch.test.SecuritySettingsSource.addSSLSettingsForPEMFiles;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests for the {@code ESNativeMigrateTool}
 */
public class ESNativeMigrateToolTests extends NativeRealmIntegTestCase {

    // Randomly use SSL (or not)
    private static boolean useSSL;

    @BeforeClass
    public static void setSSL() {
        useSSL = randomBoolean();
    }

    @Override
    protected boolean addMockHttpTransport() {
        return false; // enable http
    }

    @Override
    public Settings nodeSettings(int nodeOrdinal) {
        logger.info("--> use SSL? {}", useSSL);
        Settings.Builder builder = Settings.builder()
                .put(super.nodeSettings(nodeOrdinal));
        addSSLSettingsForNodePEMFiles(builder, "xpack.security.http.", true);
        builder.put("xpack.security.http.ssl.enabled", useSSL);
        return builder.build();
    }

    @Override
    protected boolean transportSSLEnabled() {
        return useSSL;
    }

    @Override
    protected boolean shouldSetReservedUserPasswords() {
        return false;
    }

    private Environment nodeEnvironment() throws Exception {
        return internalCluster().getInstances(Environment.class).iterator().next();
    }

    public void testRetrieveUsers() throws Exception {
        final Environment nodeEnvironment = nodeEnvironment();
        String home = Environment.PATH_HOME_SETTING.get(nodeEnvironment.settings());
        Path conf = nodeEnvironment.configFile();
        SecurityClient c = new SecurityClient(client());
        logger.error("--> creating users");
        int numToAdd = randomIntBetween(1,10);
        Set<String> addedUsers = new HashSet<>(numToAdd);
        for (int i = 0; i < numToAdd; i++) {
            String uname = randomAlphaOfLength(5);
            c.preparePutUser(uname, "s3kirt".toCharArray(), getFastStoredHashAlgoForTests(), "role1", "user").get();
            addedUsers.add(uname);
        }
        logger.error("--> waiting for .security index");
        ensureGreen(RestrictedIndicesNames.SECURITY_MAIN_ALIAS);

        MockTerminal t = new MockTerminal();
        String username = nodeClientUsername();
        String password = new String(CharArrays.toUtf8Bytes(nodeClientPassword().getChars()), StandardCharsets.UTF_8);
        String url = getHttpURL();
        ESNativeRealmMigrateTool.MigrateUserOrRoles muor = new ESNativeRealmMigrateTool.MigrateUserOrRoles();

        Settings.Builder builder = getSettingsBuilder()
                .put("path.home", home)
                .put("path.conf", conf.toString())
                .put("xpack.security.http.ssl.client_authentication", "none");
        addSSLSettingsForPEMFiles(
            builder,
            "/org/elasticsearch/xpack/security/transport/ssl/certs/simple/testnode.pem",
            "testnode",
            "/org/elasticsearch/xpack/security/transport/ssl/certs/simple/testnode.crt",
            "xpack.security.http.",
            Collections.singletonList("/org/elasticsearch/xpack/security/transport/ssl/certs/simple/testnode.crt"));
        Settings settings = builder.build();
        logger.error("--> retrieving users using URL: {}, home: {}", url, home);

        OptionParser parser = muor.getParser();
        OptionSet options = parser.parse("-u", username, "-p", password, "-U", url);
        logger.info("--> options: {}", options.asMap());
        Set<String> users = muor.getUsersThatExist(t, settings, new Environment(settings, conf), options);
        logger.info("--> output: \n{}", t.getOutput());
        for (String u : addedUsers) {
            assertThat("expected list to contain: " + u + ", real list: " + users, users.contains(u), is(true));
        }
    }

    public void testRetrieveRoles() throws Exception {
        final Environment nodeEnvironment = nodeEnvironment();
        String home = Environment.PATH_HOME_SETTING.get(nodeEnvironment.settings());
        Path conf = nodeEnvironment.configFile();
        SecurityClient c = new SecurityClient(client());
        logger.error("--> creating roles");
        int numToAdd = randomIntBetween(1,10);
        Set<String> addedRoles = new HashSet<>(numToAdd);
        for (int i = 0; i < numToAdd; i++) {
            String rname = randomAlphaOfLength(5);
            c.preparePutRole(rname)
                    .cluster("all", "none")
                    .runAs("root", "nobody")
                    .addIndices(new String[] { "index" }, new String[] { "read" }, new String[] { "body", "title" }, null,
                            new BytesArray("{\"match_all\": {}}"), randomBoolean())
                    .get();
            addedRoles.add(rname);
        }
        logger.error("--> waiting for .security index");
        ensureGreen(RestrictedIndicesNames.SECURITY_MAIN_ALIAS);

        MockTerminal t = new MockTerminal();
        String username = nodeClientUsername();
        String password = new String(CharArrays.toUtf8Bytes(nodeClientPassword().getChars()), StandardCharsets.UTF_8);
        String url = getHttpURL();
        ESNativeRealmMigrateTool.MigrateUserOrRoles muor = new ESNativeRealmMigrateTool.MigrateUserOrRoles();
        Settings.Builder builder = getSettingsBuilder()
                .put("path.home", home)
                .put("xpack.security.http.ssl.client_authentication", "none");
        addSSLSettingsForPEMFiles(builder,
            "/org/elasticsearch/xpack/security/transport/ssl/certs/simple/testclient.pem",
            "testclient",
            "/org/elasticsearch/xpack/security/transport/ssl/certs/simple/testclient.crt",
            "xpack.security.http.",
            Collections.singletonList("/org/elasticsearch/xpack/security/transport/ssl/certs/simple/testnode.crt"));
        Settings settings = builder.build();
        logger.error("--> retrieving roles using URL: {}, home: {}", url, home);

        OptionParser parser = muor.getParser();
        OptionSet options = parser.parse("-u", username, "-p", password, "-U", url);
        Set<String> roles = muor.getRolesThatExist(t, settings, new Environment(settings, conf), options);
        logger.info("--> output: \n{}", t.getOutput());
        for (String r : addedRoles) {
            assertThat("expected list to contain: " + r, roles.contains(r), is(true));
        }
    }

    public void testMissingPasswordParameter() {
        ESNativeRealmMigrateTool.MigrateUserOrRoles muor = new ESNativeRealmMigrateTool.MigrateUserOrRoles();

        final OptionException ex = expectThrows(OptionException.class,
            () -> muor.getParser().parse("-u", "elastic", "-U", "http://localhost:9200"));

        assertThat(ex.getMessage(), containsString("password"));
    }

    private Settings.Builder getSettingsBuilder() {
        Settings.Builder builder = Settings.builder();
        if (inFipsJvm()) {
            builder.put(XPackSettings.DIAGNOSE_TRUST_EXCEPTIONS_SETTING.getKey(), false);
        }
        return builder;
    }
}
