/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.license;

import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsIndices;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.SecureString;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.discovery.DiscoveryModule;
import org.elasticsearch.license.License.OperationMode;
import org.elasticsearch.node.MockNode;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.test.MockHttpTransport;
import org.elasticsearch.test.SecurityIntegTestCase;
import org.elasticsearch.test.SecuritySettingsSource;
import org.elasticsearch.test.SecuritySettingsSourceField;
import org.elasticsearch.transport.Netty4Plugin;
import org.elasticsearch.transport.Transport;
import org.elasticsearch.xpack.core.TestXPackTransportClient;
import org.elasticsearch.xpack.core.XPackField;
import org.elasticsearch.xpack.core.security.SecurityField;
import org.elasticsearch.xpack.core.security.action.user.PutUserResponse;
import org.elasticsearch.xpack.core.security.authc.support.Hasher;
import org.elasticsearch.xpack.core.security.authc.support.UsernamePasswordToken;
import org.elasticsearch.xpack.core.security.client.SecurityClient;
import org.elasticsearch.xpack.security.LocalStateSecurity;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.discovery.SettingsBasedSeedHostsProvider.DISCOVERY_SEED_HOSTS_SETTING;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertNoFailures;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class LicensingTests extends SecurityIntegTestCase {
    private static final String ROLES =
            SecuritySettingsSource.TEST_ROLE + ":\n" +
                    "  cluster: [ all ]\n" +
                    "  indices:\n" +
                    "    - names: '*'\n" +
                    "      privileges: [manage]\n" +
                    "    - names: '/.*/'\n" +
                    "      privileges: [write]\n" +
                    "    - names: 'test'\n" +
                    "      privileges: [read]\n" +
                    "    - names: 'test1'\n" +
                    "      privileges: [read]\n" +
                    "\n" +
                    "role_a:\n" +
                    "  indices:\n" +
                    "    - names: 'a'\n" +
                    "      privileges: [all]\n" +
                    "\n" +
                    "role_b:\n" +
                    "  indices:\n" +
                    "    - names: 'b'\n" +
                    "      privileges: [all]\n";

    private static final String USERS_ROLES =
            SecuritySettingsSource.CONFIG_STANDARD_USER_ROLES +
                    "role_a:user_a,user_b\n" +
                    "role_b:user_b\n";

    @Override
    protected String configRoles() {
        return ROLES;
    }

    @Override
    protected String configUsers() {
        return SecuritySettingsSource.CONFIG_STANDARD_USER +
            "user_a:{plain}passwd\n" +
            "user_b:{plain}passwd\n";
    }

    @Override
    protected String configUsersRoles() {
        return USERS_ROLES;
    }

    @Override
    protected boolean addMockHttpTransport() {
        return false; // enable http
    }

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        ArrayList<Class<? extends Plugin>> plugins = new ArrayList<>(super.nodePlugins());
        plugins.add(Netty4Plugin.class); // for http
        return plugins;
    }

    @Override
    protected int maxNumberOfNodes() {
        return super.maxNumberOfNodes() + 1;
    }

    @Before
    public void resetLicensing() throws Exception {
        enableLicensing(OperationMode.MISSING);
    }

    @After
    public void cleanupSecurityIndex() {
        deleteSecurityIndex();
    }

    public void testEnableDisableBehaviour() throws Exception {
        IndexResponse indexResponse = index("test", "type", jsonBuilder()
                .startObject()
                .field("name", "value")
                .endObject());
        assertEquals(DocWriteResponse.Result.CREATED, indexResponse.getResult());


        indexResponse = index("test1", "type", jsonBuilder()
                .startObject()
                .field("name", "value1")
                .endObject());
        assertEquals(DocWriteResponse.Result.CREATED, indexResponse.getResult());

        refresh();
        final Client client = internalCluster().transportClient();

        disableLicensing();

        assertElasticsearchSecurityException(() -> client.admin().indices().prepareStats().get());
        assertElasticsearchSecurityException(() -> client.admin().cluster().prepareClusterStats().get());
        assertElasticsearchSecurityException(() -> client.admin().cluster().prepareHealth().get());
        assertElasticsearchSecurityException(() -> client.admin().cluster().prepareNodesStats().get());

        enableLicensing(randomFrom(License.OperationMode.values()));

        IndicesStatsResponse indicesStatsResponse = client.admin().indices().prepareStats().get();
        assertNoFailures(indicesStatsResponse);

        ClusterStatsResponse clusterStatsNodeResponse = client.admin().cluster().prepareClusterStats().get();
        assertThat(clusterStatsNodeResponse, notNullValue());
        ClusterStatsIndices indices = clusterStatsNodeResponse.getIndicesStats();
        assertThat(indices, notNullValue());
        assertThat(indices.getIndexCount(), greaterThanOrEqualTo(2));

        ClusterHealthResponse clusterIndexHealth = client.admin().cluster().prepareHealth().get();
        assertThat(clusterIndexHealth, notNullValue());

        NodesStatsResponse nodeStats = client.admin().cluster().prepareNodesStats().get();
        assertThat(nodeStats, notNullValue());
    }

    public void testRestAuthenticationByLicenseType() throws Exception {
        Response unauthorizedRootResponse = getRestClient().performRequest(new Request("GET", "/"));
        // the default of the licensing tests is basic
        assertThat(unauthorizedRootResponse.getStatusLine().getStatusCode(), is(200));
        ResponseException e = expectThrows(ResponseException.class,
            () -> getRestClient().performRequest(new Request("GET", "/_security/_authenticate")));
        assertThat(e.getResponse().getStatusLine().getStatusCode(), is(403));

        // generate a new license with a mode that enables auth
        License.OperationMode mode = randomFrom(License.OperationMode.GOLD, License.OperationMode.TRIAL,
                License.OperationMode.PLATINUM, License.OperationMode.STANDARD);
        enableLicensing(mode);
        e = expectThrows(ResponseException.class, () -> getRestClient().performRequest(new Request("GET", "/")));
        assertThat(e.getResponse().getStatusLine().getStatusCode(), is(401));
        e = expectThrows(ResponseException.class,
            () -> getRestClient().performRequest(new Request("GET", "/_security/_authenticate")));
        assertThat(e.getResponse().getStatusLine().getStatusCode(), is(401));

        RequestOptions.Builder optionsBuilder = RequestOptions.DEFAULT.toBuilder();
        optionsBuilder.addHeader("Authorization", UsernamePasswordToken.basicAuthHeaderValue(SecuritySettingsSource.TEST_USER_NAME,
                new SecureString(SecuritySettingsSourceField.TEST_PASSWORD.toCharArray())));
        RequestOptions options = optionsBuilder.build();

        Request rootRequest = new Request("GET", "/");
        rootRequest.setOptions(options);
        Response authorizedRootResponse = getRestClient().performRequest(rootRequest);
        assertThat(authorizedRootResponse.getStatusLine().getStatusCode(), is(200));
        Request authenticateRequest = new Request("GET", "/_security/_authenticate");
        authenticateRequest.setOptions(options);
        Response authorizedAuthenticateResponse = getRestClient().performRequest(authenticateRequest);
        assertThat(authorizedAuthenticateResponse.getStatusLine().getStatusCode(), is(200));
    }

    public void testSecurityActionsByLicenseType() throws Exception {
        // security actions should not work!
        Settings settings = internalCluster().transportClient().settings();
        try (TransportClient client = new TestXPackTransportClient(settings, LocalStateSecurity.class)) {
            client.addTransportAddress(internalCluster().getDataNodeInstance(Transport.class).boundAddress().publishAddress());
            new SecurityClient(client).preparePutUser("john", "password".toCharArray(), Hasher.BCRYPT).get();
            fail("security actions should not be enabled!");
        } catch (ElasticsearchSecurityException e) {
            assertThat(e.status(), is(RestStatus.FORBIDDEN));
            assertThat(e.getMessage(), containsString("non-compliant"));
        }

        // enable a license that enables security
        License.OperationMode mode = randomFrom(License.OperationMode.GOLD, License.OperationMode.TRIAL,
                License.OperationMode.PLATINUM, License.OperationMode.STANDARD, OperationMode.BASIC);
        enableLicensing(mode);
        // security actions should work!
        try (TransportClient client = new TestXPackTransportClient(settings, LocalStateSecurity.class)) {
            client.addTransportAddress(internalCluster().getDataNodeInstance(Transport.class).boundAddress().publishAddress());
            PutUserResponse response = new SecurityClient(client).preparePutUser("john", "password".toCharArray(), Hasher.BCRYPT).get();
            assertNotNull(response);
        }
    }

    public void testTransportClientAuthenticationByLicenseType() throws Exception {
        Settings.Builder builder = Settings.builder()
                .put(internalCluster().transportClient().settings());
        // remove user info
        builder.remove(SecurityField.USER_SETTING.getKey());
        builder.remove(ThreadContext.PREFIX + "." + UsernamePasswordToken.BASIC_AUTH_HEADER);

        // basic has no auth
        try (TransportClient client = new TestXPackTransportClient(builder.build(), LocalStateSecurity.class)) {
            client.addTransportAddress(internalCluster().getDataNodeInstance(Transport.class).boundAddress().publishAddress());
            assertGreenClusterState(client);
        }

        // enable a license that enables security
        License.OperationMode mode = randomFrom(License.OperationMode.GOLD, License.OperationMode.TRIAL,
                License.OperationMode.PLATINUM, License.OperationMode.STANDARD);
        enableLicensing(mode);

        try (TransportClient client = new TestXPackTransportClient(builder.build(), LocalStateSecurity.class)) {
            client.addTransportAddress(internalCluster().getDataNodeInstance(Transport.class).boundAddress().publishAddress());
            client.admin().cluster().prepareHealth().get();
            fail("should not have been able to connect to a node!");
        } catch (NoNodeAvailableException e) {
            // expected
        }
    }

    public void testNodeJoinWithoutSecurityExplicitlyEnabled() throws Exception {
        License.OperationMode mode = randomFrom(License.OperationMode.GOLD, License.OperationMode.PLATINUM, License.OperationMode.STANDARD);
        enableLicensing(mode);

        final List<String> seedHosts = internalCluster().masterClient().admin().cluster().nodesInfo(new NodesInfoRequest()).get()
            .getNodes().stream().map(n -> n.getTransport().getAddress().publishAddress().toString()).distinct()
            .collect(Collectors.toList());

        Path home = createTempDir();
        Path conf = home.resolve("config");
        Files.createDirectories(conf);
        Settings.Builder nodeSettings = Settings.builder()
            .put(nodeSettings(maxNumberOfNodes() - 1).filter(s -> "xpack.security.enabled".equals(s) == false))
            .put("node.name", "my-test-node")
            .put("network.host", "localhost")
            .put("cluster.name", internalCluster().getClusterName())
            .put("path.home", home)
            .putList(DiscoveryModule.DISCOVERY_SEED_PROVIDERS_SETTING.getKey())
            .putList(DISCOVERY_SEED_HOSTS_SETTING.getKey(), seedHosts);

        Collection<Class<? extends Plugin>> mockPlugins = Arrays.asList(LocalStateSecurity.class, MockHttpTransport.TestPlugin.class);
        try (Node node = new MockNode(nodeSettings.build(), mockPlugins)) {
            node.start();
            ensureStableCluster(cluster().size() + 1);
        }
    }

    private static void assertElasticsearchSecurityException(ThrowingRunnable runnable) {
        ElasticsearchSecurityException ee = expectThrows(ElasticsearchSecurityException.class, runnable);
        assertThat(ee.getMetadata(LicenseUtils.EXPIRED_FEATURE_METADATA), hasItem(XPackField.SECURITY));
        assertThat(ee.status(), is(RestStatus.FORBIDDEN));
    }

    private void disableLicensing() throws Exception {
        // This method first makes sure licensing is enabled everywhere so that we can execute
        // monitoring actions to ensure we have a stable cluster and only then do we disable.
        // This is done in an assertBusy since there is a chance that the enabling of the license
        // is overwritten by some other cluster activity and the node throws an exception while we
        // wait for things to stabilize!
        assertBusy(() -> {
            for (XPackLicenseState licenseState : internalCluster().getInstances(XPackLicenseState.class)) {
                if (licenseState.isAuthAllowed() == false) {
                    enableLicensing(OperationMode.BASIC);
                    break;
                }
            }

            ensureGreen();
            ensureClusterSizeConsistency();
            ensureClusterStateConsistency();

            // apply the disabling of the license once the cluster is stable
            for (XPackLicenseState licenseState : internalCluster().getInstances(XPackLicenseState.class)) {
                licenseState.update(OperationMode.BASIC, false, null);
            }
        }, 30L, TimeUnit.SECONDS);
    }

    private void enableLicensing(License.OperationMode operationMode) throws Exception {
        // do this in an await busy since there is a chance that the enabling of the license is
        // overwritten by some other cluster activity and the node throws an exception while we
        // wait for things to stabilize!
        assertBusy(() -> {
            // first update the license so we can execute monitoring actions
            for (XPackLicenseState licenseState : internalCluster().getInstances(XPackLicenseState.class)) {
                licenseState.update(operationMode, true, null);
            }

            ensureGreen();
            ensureClusterSizeConsistency();
            ensureClusterStateConsistency();

            // re-apply the update in case any node received an updated cluster state that triggered the license state
            // to change
            for (XPackLicenseState licenseState : internalCluster().getInstances(XPackLicenseState.class)) {
                licenseState.update(operationMode, true, null);
            }
        }, 30L, TimeUnit.SECONDS);
    }
}
