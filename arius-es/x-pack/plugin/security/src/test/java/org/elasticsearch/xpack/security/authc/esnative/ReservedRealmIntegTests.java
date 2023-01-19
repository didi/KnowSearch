/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.authc.esnative;

import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.common.settings.SecureString;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.NativeRealmIntegTestCase;
import org.elasticsearch.xpack.core.security.action.user.ChangePasswordResponse;
import org.elasticsearch.xpack.core.security.authc.support.Hasher;
import org.elasticsearch.xpack.core.security.client.SecurityClient;
import org.elasticsearch.xpack.core.security.user.APMSystemUser;
import org.elasticsearch.xpack.core.security.user.BeatsSystemUser;
import org.elasticsearch.xpack.core.security.user.ElasticUser;
import org.elasticsearch.xpack.core.security.user.KibanaUser;
import org.elasticsearch.xpack.core.security.user.LogstashSystemUser;
import org.elasticsearch.xpack.core.security.user.RemoteMonitoringUser;
import org.junit.BeforeClass;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonMap;
import static org.elasticsearch.xpack.core.security.authc.support.UsernamePasswordToken.basicAuthHeaderValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration tests for the built in realm
 */
public class ReservedRealmIntegTests extends NativeRealmIntegTestCase {

    private static Hasher hasher;

    @BeforeClass
    public static void setHasher() {
        hasher = getFastStoredHashAlgoForTests();
    }

    @Override
    public Settings nodeSettings(int nodeOrdinal) {
        Settings settings = Settings.builder()
            .put(super.nodeSettings(nodeOrdinal))
            .put("xpack.security.authc.password_hashing.algorithm", hasher.name())
            .build();
        return settings;
    }

    public void testAuthenticate() {
        final List<String> usernames = Arrays.asList(ElasticUser.NAME, KibanaUser.NAME, LogstashSystemUser.NAME,
            BeatsSystemUser.NAME, APMSystemUser.NAME, RemoteMonitoringUser.NAME);
        for (String username : usernames) {
            ClusterHealthResponse response = client()
                    .filterWithHeader(singletonMap("Authorization", basicAuthHeaderValue(username, getReservedPassword())))
                    .admin()
                    .cluster()
                    .prepareHealth()
                    .get();

            assertThat(response.getClusterName(), is(cluster().getClusterName()));
        }
    }

    /**
     * Enabling a user forces a doc to be written to the security index, and "user doc with empty password" has a special case code in
     * the reserved realm.
     */
    public void testAuthenticateAfterEnablingUser() {
        final SecurityClient c = securityClient();
        final List<String> usernames = Arrays.asList(ElasticUser.NAME, KibanaUser.NAME, LogstashSystemUser.NAME,
            BeatsSystemUser.NAME, APMSystemUser.NAME, RemoteMonitoringUser.NAME);
        for (String username : usernames) {
            c.prepareSetEnabled(username, true).get();
            ClusterHealthResponse response = client()
                    .filterWithHeader(singletonMap("Authorization", basicAuthHeaderValue(username, getReservedPassword())))
                    .admin()
                    .cluster()
                    .prepareHealth()
                    .get();

            assertThat(response.getClusterName(), is(cluster().getClusterName()));
        }
    }

    public void testChangingPassword() {
        String username = randomFrom(ElasticUser.NAME, KibanaUser.NAME, LogstashSystemUser.NAME,
            BeatsSystemUser.NAME, APMSystemUser.NAME, RemoteMonitoringUser.NAME);
        final char[] newPassword = "supersecretvalue".toCharArray();

        if (randomBoolean()) {
            ClusterHealthResponse response = client()
                    .filterWithHeader(singletonMap("Authorization", basicAuthHeaderValue(username, getReservedPassword())))
                    .admin()
                    .cluster()
                    .prepareHealth()
                    .get();
            assertThat(response.getClusterName(), is(cluster().getClusterName()));
        }

        ChangePasswordResponse response = securityClient()
            .prepareChangePassword(username, Arrays.copyOf(newPassword, newPassword.length), hasher)
                .get();
        assertThat(response, notNullValue());

        ElasticsearchSecurityException elasticsearchSecurityException = expectThrows(ElasticsearchSecurityException.class, () -> client()
                    .filterWithHeader(singletonMap("Authorization", basicAuthHeaderValue(username, getReservedPassword())))
                    .admin()
                    .cluster()
                    .prepareHealth()
                    .get());
        assertThat(elasticsearchSecurityException.getMessage(), containsString("authenticate"));

        ClusterHealthResponse healthResponse = client()
                .filterWithHeader(singletonMap("Authorization", basicAuthHeaderValue(username, new SecureString(newPassword))))
                .admin()
                .cluster()
                .prepareHealth()
                .get();
        assertThat(healthResponse.getClusterName(), is(cluster().getClusterName()));
    }

    public void testDisablingUser() throws Exception {
        // validate the user works
        ClusterHealthResponse response = client()
                .filterWithHeader(singletonMap("Authorization", basicAuthHeaderValue(ElasticUser.NAME, getReservedPassword())))
                .admin()
                .cluster()
                .prepareHealth()
                .get();
        assertThat(response.getClusterName(), is(cluster().getClusterName()));

        // disable user
        securityClient().prepareSetEnabled(ElasticUser.NAME, false).get();
        ElasticsearchSecurityException elasticsearchSecurityException = expectThrows(ElasticsearchSecurityException.class, () -> client()
                .filterWithHeader(singletonMap("Authorization", basicAuthHeaderValue(ElasticUser.NAME, getReservedPassword())))
                .admin()
                .cluster()
                .prepareHealth()
                .get());
        assertThat(elasticsearchSecurityException.getMessage(), containsString("authenticate"));

        //enable
        securityClient().prepareSetEnabled(ElasticUser.NAME, true).get();
        response = client()
                .filterWithHeader(singletonMap("Authorization", basicAuthHeaderValue(ElasticUser.NAME, getReservedPassword())))
                .admin()
                .cluster()
                .prepareHealth()
                .get();
        assertThat(response.getClusterName(), is(cluster().getClusterName()));
    }
}
