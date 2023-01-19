/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.security.authc;

import com.google.common.collect.Sets;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.test.SecurityIntegTestCase;
import org.elasticsearch.test.SecuritySettingsSource;
import org.elasticsearch.test.SecuritySettingsSourceField;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.xpack.core.XPackSettings;
import org.elasticsearch.xpack.core.security.action.ApiKey;
import org.elasticsearch.xpack.core.security.action.CreateApiKeyResponse;
import org.elasticsearch.xpack.core.security.action.GetApiKeyRequest;
import org.elasticsearch.xpack.core.security.action.GetApiKeyResponse;
import org.elasticsearch.xpack.core.security.action.InvalidateApiKeyRequest;
import org.elasticsearch.xpack.core.security.action.InvalidateApiKeyResponse;
import org.elasticsearch.xpack.core.security.authc.support.UsernamePasswordToken;
import org.elasticsearch.xpack.core.security.authz.RoleDescriptor;
import org.elasticsearch.xpack.core.security.client.SecurityClient;
import org.elasticsearch.xpack.security.transport.filter.IPFilter;
import org.junit.After;
import org.junit.Before;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.index.mapper.MapperService.SINGLE_MAPPING_NAME;
import static org.elasticsearch.xpack.core.security.index.RestrictedIndicesNames.SECURITY_MAIN_ALIAS;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class ApiKeyIntegTests extends SecurityIntegTestCase {
    private static final long DELETE_INTERVAL_MILLIS = 100L;

    @Override
    public Settings nodeSettings(int nodeOrdinal) {
        return Settings.builder()
            .put(super.nodeSettings(nodeOrdinal))
            .put(XPackSettings.API_KEY_SERVICE_ENABLED_SETTING.getKey(), true)
            .put(ApiKeyService.DELETE_INTERVAL.getKey(), TimeValue.timeValueMillis(DELETE_INTERVAL_MILLIS))
            .put(ApiKeyService.DELETE_TIMEOUT.getKey(), TimeValue.timeValueSeconds(5L))
            .build();
    }

    @Before
    public void waitForSecurityIndexWritable() throws Exception {
        assertSecurityIndexActive();
    }

    @After
    public void wipeSecurityIndex() throws Exception {
        // get the api key service and wait until api key expiration is not in progress!
        awaitApiKeysRemoverCompletion();
        deleteSecurityIndex();
    }

    @Override
    public String configRoles() {
        return super.configRoles() + "\n" +
            "no_api_key_role:\n" +
            "  cluster: [\"manage_token\"]\n" +
            "manage_api_key_role:\n" +
            "  cluster: [\"manage_api_key\"]\n" +
            "manage_own_api_key_role:\n" +
            "  cluster: [\"manage_own_api_key\"]\n";
    }

    @Override
    public String configUsers() {
        final String usersPasswdHashed = new String(
            getFastStoredHashAlgoForTests().hash(SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING));
        return super.configUsers() +
            "user_with_no_api_key_role:" + usersPasswdHashed + "\n" +
            "user_with_manage_api_key_role:" + usersPasswdHashed + "\n" +
            "user_with_manage_own_api_key_role:" + usersPasswdHashed + "\n";
    }

    @Override
    public String configUsersRoles() {
        return super.configUsersRoles() +
            "no_api_key_role:user_with_no_api_key_role\n" +
            "manage_api_key_role:user_with_manage_api_key_role\n" +
            "manage_own_api_key_role:user_with_manage_own_api_key_role\n";
    }

    private void awaitApiKeysRemoverCompletion() throws Exception {
        for (ApiKeyService apiKeyService : internalCluster().getInstances(ApiKeyService.class)) {
            assertBusy(() -> assertFalse(apiKeyService.isExpirationInProgress()));
        }
    }
    
    public void testCreateApiKey() {
        // Get an instant without nanoseconds as the expiration has millisecond precision
        final Instant start = Instant.ofEpochMilli(Instant.now().toEpochMilli());
        final RoleDescriptor descriptor = new RoleDescriptor("role", new String[] { "monitor" }, null, null);
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization",
            UsernamePasswordToken.basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER,
                SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        final CreateApiKeyResponse response = securityClient.prepareCreateApiKey()
            .setName("test key")
            .setExpiration(TimeValue.timeValueHours(TimeUnit.DAYS.toHours(7L)))
            .setRoleDescriptors(Collections.singletonList(descriptor))
            .get();

        assertEquals("test key", response.getName());
        assertNotNull(response.getId());
        assertNotNull(response.getKey());
        Instant expiration = response.getExpiration();
        // Expiration has millisecond precision
        final long daysBetween = ChronoUnit.DAYS.between(start, expiration);
        assertThat(daysBetween, is(7L));

        // create simple api key
        final CreateApiKeyResponse simple = securityClient.prepareCreateApiKey().setName("simple").get();
        assertEquals("simple", simple.getName());
        assertNotNull(simple.getId());
        assertNotNull(simple.getKey());
        assertThat(simple.getId(), not(containsString(new String(simple.getKey().getChars()))));
        assertNull(simple.getExpiration());

        // use the first ApiKey for authorized action
        final String base64ApiKeyKeyValue = Base64.getEncoder().encodeToString(
            (response.getId() + ":" + response.getKey().toString()).getBytes(StandardCharsets.UTF_8));
        ClusterHealthResponse healthResponse = client()
            .filterWithHeader(Collections.singletonMap("Authorization", "ApiKey " + base64ApiKeyKeyValue))
            .admin()
            .cluster()
            .prepareHealth()
            .get();
        assertFalse(healthResponse.isTimedOut());

        // use the first ApiKey for an unauthorized action
        ElasticsearchSecurityException e = expectThrows(ElasticsearchSecurityException.class, () ->
            client().filterWithHeader(Collections.singletonMap("Authorization", "ApiKey " + base64ApiKeyKeyValue))
                .admin()
                .cluster()
                .prepareUpdateSettings().setTransientSettings(Settings.builder().put(IPFilter.IP_FILTER_ENABLED_SETTING.getKey(), true))
                .get());
        assertThat(e.getMessage(), containsString("unauthorized"));
        assertThat(e.status(), is(RestStatus.FORBIDDEN));
    }

    public void testMultipleApiKeysCanHaveSameName() {
        String keyName = randomAlphaOfLength(5);
        int noOfApiKeys = randomIntBetween(2, 5);
        List<CreateApiKeyResponse> responses = new ArrayList<>();
        for (int i = 0; i < noOfApiKeys; i++) {
            final RoleDescriptor descriptor = new RoleDescriptor("role", new String[]{"monitor"}, null, null);
            Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
            SecurityClient securityClient = new SecurityClient(client);
            final CreateApiKeyResponse response = securityClient.prepareCreateApiKey().setName(keyName).setExpiration(null)
                .setRoleDescriptors(Collections.singletonList(descriptor)).get();
            assertNotNull(response.getId());
            assertNotNull(response.getKey());
            responses.add(response);
        }
        assertThat(responses.size(), is(noOfApiKeys));
        for (int i = 0; i < noOfApiKeys; i++) {
            assertThat(responses.get(i).getName(), is(keyName));
        }
    }

    public void testInvalidateApiKeysForRealm() throws InterruptedException, ExecutionException {
        int noOfApiKeys = randomIntBetween(3, 5);
        List<CreateApiKeyResponse> responses = createApiKeys(noOfApiKeys, null);
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<InvalidateApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingRealmName("file"), listener);
        InvalidateApiKeyResponse invalidateResponse = listener.get();
        verifyInvalidateResponse(noOfApiKeys, responses, invalidateResponse);
    }

    public void testInvalidateApiKeysForUser() throws Exception {
        int noOfApiKeys = randomIntBetween(3, 5);
        List<CreateApiKeyResponse> responses = createApiKeys(noOfApiKeys, null);
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<InvalidateApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingUserName(SecuritySettingsSource.TEST_SUPERUSER), listener);
        InvalidateApiKeyResponse invalidateResponse = listener.get();
        verifyInvalidateResponse(noOfApiKeys, responses, invalidateResponse);
    }

    public void testInvalidateApiKeysForRealmAndUser() throws InterruptedException, ExecutionException {
        List<CreateApiKeyResponse> responses = createApiKeys(1, null);
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<InvalidateApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingRealmAndUserName("file", SecuritySettingsSource.TEST_SUPERUSER),
                listener);
        InvalidateApiKeyResponse invalidateResponse = listener.get();
        verifyInvalidateResponse(1, responses, invalidateResponse);
    }

    public void testInvalidateApiKeysForApiKeyId() throws InterruptedException, ExecutionException {
        List<CreateApiKeyResponse> responses = createApiKeys(1, null);
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<InvalidateApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingApiKeyId(responses.get(0).getId(), false), listener);
        InvalidateApiKeyResponse invalidateResponse = listener.get();
        verifyInvalidateResponse(1, responses, invalidateResponse);
    }

    public void testInvalidateApiKeysForApiKeyName() throws InterruptedException, ExecutionException {
        List<CreateApiKeyResponse> responses = createApiKeys(1, null);
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<InvalidateApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingApiKeyName(responses.get(0).getName(), false), listener);
        InvalidateApiKeyResponse invalidateResponse = listener.get();
        verifyInvalidateResponse(1, responses, invalidateResponse);
    }

    private void verifyInvalidateResponse(int noOfApiKeys, List<CreateApiKeyResponse> responses,
                                          InvalidateApiKeyResponse invalidateResponse) {
        assertThat(invalidateResponse.getInvalidatedApiKeys().size(), equalTo(noOfApiKeys));
        assertThat(invalidateResponse.getInvalidatedApiKeys(),
                containsInAnyOrder(responses.stream().map(r -> r.getId()).collect(Collectors.toList()).toArray(Strings.EMPTY_ARRAY)));
        assertThat(invalidateResponse.getPreviouslyInvalidatedApiKeys().size(), equalTo(0));
        assertThat(invalidateResponse.getErrors().size(), equalTo(0));
    }

    public void testInvalidatedApiKeysDeletedByRemover() throws Exception {
        Client client = waitForExpiredApiKeysRemoverTriggerReadyAndGetClient().filterWithHeader(
                Collections.singletonMap("Authorization", UsernamePasswordToken.basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER,
                        SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));

        List<CreateApiKeyResponse> createdApiKeys = createApiKeys(2, null);

        SecurityClient securityClient = new SecurityClient(client);

        PlainActionFuture<InvalidateApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingApiKeyId(createdApiKeys.get(0).getId(), false), listener);
        InvalidateApiKeyResponse invalidateResponse = listener.get();
        assertThat(invalidateResponse.getInvalidatedApiKeys().size(), equalTo(1));
        assertThat(invalidateResponse.getPreviouslyInvalidatedApiKeys().size(), equalTo(0));
        assertThat(invalidateResponse.getErrors().size(), equalTo(0));

        awaitApiKeysRemoverCompletion();
        refreshSecurityIndex();

        PlainActionFuture<GetApiKeyResponse> getApiKeyResponseListener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.usingRealmName("file"), getApiKeyResponseListener);
        Set<String> expectedKeyIds = Sets.newHashSet(createdApiKeys.get(0).getId(), createdApiKeys.get(1).getId());
        boolean apiKeyInvalidatedButNotYetDeletedByExpiredApiKeysRemover = false;
        for (ApiKey apiKey : getApiKeyResponseListener.get().getApiKeyInfos()) {
            assertThat(apiKey.getId(), isIn(expectedKeyIds));
            if (apiKey.getId().equals(createdApiKeys.get(0).getId())) {
                // has been invalidated but not yet deleted by ExpiredApiKeysRemover
                assertThat(apiKey.isInvalidated(), is(true));
                apiKeyInvalidatedButNotYetDeletedByExpiredApiKeysRemover = true;
            } else if (apiKey.getId().equals(createdApiKeys.get(1).getId())) {
                // active api key
                assertThat(apiKey.isInvalidated(), is(false));
            }
        }
        assertThat(getApiKeyResponseListener.get().getApiKeyInfos().length,
                is((apiKeyInvalidatedButNotYetDeletedByExpiredApiKeysRemover) ? 2 : 1));

        client = waitForExpiredApiKeysRemoverTriggerReadyAndGetClient().filterWithHeader(
                Collections.singletonMap("Authorization", UsernamePasswordToken.basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER,
                        SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        securityClient = new SecurityClient(client);

        // invalidate API key to trigger remover
        listener = new PlainActionFuture<>();
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingApiKeyId(createdApiKeys.get(1).getId(), false), listener);
        assertThat(listener.get().getInvalidatedApiKeys().size(), is(1));

        awaitApiKeysRemoverCompletion();
        refreshSecurityIndex();

        // Verify that 1st invalidated API key is deleted whereas the next one may be or may not be as it depends on whether update was
        // indexed before ExpiredApiKeysRemover ran
        getApiKeyResponseListener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.usingRealmName("file"), getApiKeyResponseListener);
        expectedKeyIds = Sets.newHashSet(createdApiKeys.get(1).getId());
        apiKeyInvalidatedButNotYetDeletedByExpiredApiKeysRemover = false;
        for (ApiKey apiKey : getApiKeyResponseListener.get().getApiKeyInfos()) {
            assertThat(apiKey.getId(), isIn(expectedKeyIds));
            if (apiKey.getId().equals(createdApiKeys.get(1).getId())) {
                // has been invalidated but not yet deleted by ExpiredApiKeysRemover
                assertThat(apiKey.isInvalidated(), is(true));
                apiKeyInvalidatedButNotYetDeletedByExpiredApiKeysRemover = true;
            }
        }
        assertThat(getApiKeyResponseListener.get().getApiKeyInfos().length,
                is((apiKeyInvalidatedButNotYetDeletedByExpiredApiKeysRemover) ? 1 : 0));
    }

    private Client waitForExpiredApiKeysRemoverTriggerReadyAndGetClient() throws Exception {
        String nodeWithMostRecentRun = null;
        long apiKeyLastTrigger = -1L;
        for (String nodeName : internalCluster().getNodeNames()) {
            ApiKeyService apiKeyService = internalCluster().getInstance(ApiKeyService.class, nodeName);
            if (apiKeyService != null) {
                if (apiKeyService.lastTimeWhenApiKeysRemoverWasTriggered() > apiKeyLastTrigger) {
                    nodeWithMostRecentRun = nodeName;
                    apiKeyLastTrigger = apiKeyService.lastTimeWhenApiKeysRemoverWasTriggered();
               }
            }
        }
        final ThreadPool threadPool = internalCluster().getInstance(ThreadPool.class, nodeWithMostRecentRun);
        final long lastRunTime = apiKeyLastTrigger;
        assertBusy(() -> {
            assertThat(threadPool.relativeTimeInMillis() - lastRunTime, greaterThan(DELETE_INTERVAL_MILLIS));
        });
        return internalCluster().client(nodeWithMostRecentRun);
    }

    public void testExpiredApiKeysBehaviorWhenKeysExpired1WeekBeforeAnd1DayBefore() throws Exception {
        Client client = waitForExpiredApiKeysRemoverTriggerReadyAndGetClient().filterWithHeader(
                Collections.singletonMap("Authorization", UsernamePasswordToken.basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER,
                        SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));

        int noOfKeys = 4;
        List<CreateApiKeyResponse> createdApiKeys = createApiKeys(noOfKeys, null);
        Instant created = Instant.now();

        SecurityClient securityClient = new SecurityClient(client);

        PlainActionFuture<GetApiKeyResponse> getApiKeyResponseListener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.usingRealmName("file"), getApiKeyResponseListener);
        assertThat(getApiKeyResponseListener.get().getApiKeyInfos().length, is(noOfKeys));

        // Expire the 1st key such that it cannot be deleted by the remover
        // hack doc to modify the expiration time to a day before
        Instant dayBefore = created.minus(1L, ChronoUnit.DAYS);
        assertTrue(Instant.now().isAfter(dayBefore));
        UpdateResponse expirationDateUpdatedResponse = client
                .prepareUpdate(SECURITY_MAIN_ALIAS, SINGLE_MAPPING_NAME, createdApiKeys.get(0).getId())
                .setDoc("expiration_time", dayBefore.toEpochMilli())
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();
        assertThat(expirationDateUpdatedResponse.getResult(), is(DocWriteResponse.Result.UPDATED));

        // Expire the 2nd key such that it can be deleted by the remover
        // hack doc to modify the expiration time to the week before
        Instant weekBefore = created.minus(8L, ChronoUnit.DAYS);
        assertTrue(Instant.now().isAfter(weekBefore));
        expirationDateUpdatedResponse = client.prepareUpdate(SECURITY_MAIN_ALIAS, SINGLE_MAPPING_NAME, createdApiKeys.get(1).getId())
                .setDoc("expiration_time", weekBefore.toEpochMilli())
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();
        assertThat(expirationDateUpdatedResponse.getResult(), is(DocWriteResponse.Result.UPDATED));

        // Invalidate to trigger the remover
        PlainActionFuture<InvalidateApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingApiKeyId(createdApiKeys.get(2).getId(), false), listener);
        assertThat(listener.get().getInvalidatedApiKeys().size(), is(1));

        awaitApiKeysRemoverCompletion();

        refreshSecurityIndex();

        // Verify get API keys does not return api keys deleted by ExpiredApiKeysRemover
        getApiKeyResponseListener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.usingRealmName("file"), getApiKeyResponseListener);

        Set<String> expectedKeyIds = Sets.newHashSet(createdApiKeys.get(0).getId(), createdApiKeys.get(2).getId(),
                createdApiKeys.get(3).getId());
        boolean apiKeyInvalidatedButNotYetDeletedByExpiredApiKeysRemover = false;
        for (ApiKey apiKey : getApiKeyResponseListener.get().getApiKeyInfos()) {
            assertThat(apiKey.getId(), isIn(expectedKeyIds));
            if (apiKey.getId().equals(createdApiKeys.get(0).getId())) {
                // has been expired, not invalidated
                assertTrue(apiKey.getExpiration().isBefore(Instant.now()));
                assertThat(apiKey.isInvalidated(), is(false));
            } else if (apiKey.getId().equals(createdApiKeys.get(2).getId())) {
                // has not been expired as no expiration, is invalidated but not yet deleted by ExpiredApiKeysRemover
                assertThat(apiKey.getExpiration(), is(nullValue()));
                assertThat(apiKey.isInvalidated(), is(true));
                apiKeyInvalidatedButNotYetDeletedByExpiredApiKeysRemover = true;
            } else if (apiKey.getId().equals(createdApiKeys.get(3).getId())) {
                // has not been expired as no expiration, not invalidated
                assertThat(apiKey.getExpiration(), is(nullValue()));
                assertThat(apiKey.isInvalidated(), is(false));
            } else {
                fail("unexpected API key " + apiKey);
            }
        }
        assertThat(getApiKeyResponseListener.get().getApiKeyInfos().length,
                is((apiKeyInvalidatedButNotYetDeletedByExpiredApiKeysRemover) ? 3 : 2));
    }

    private void refreshSecurityIndex() throws Exception {
        assertBusy(() -> {
            final RefreshResponse refreshResponse = client().admin().indices().prepareRefresh(SECURITY_MAIN_ALIAS).get();
            assertThat(refreshResponse.getFailedShards(), is(0));
        });
    }

    public void testActiveApiKeysWithNoExpirationNeverGetDeletedByRemover() throws Exception {
        List<CreateApiKeyResponse> responses = createApiKeys(2, null);

        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<InvalidateApiKeyResponse> listener = new PlainActionFuture<>();
        // trigger expired keys remover
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingApiKeyId(responses.get(1).getId(), false), listener);
        InvalidateApiKeyResponse invalidateResponse = listener.get();
        assertThat(invalidateResponse.getInvalidatedApiKeys().size(), equalTo(1));
        assertThat(invalidateResponse.getPreviouslyInvalidatedApiKeys().size(), equalTo(0));
        assertThat(invalidateResponse.getErrors().size(), equalTo(0));

        PlainActionFuture<GetApiKeyResponse> getApiKeyResponseListener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.usingRealmName("file"), getApiKeyResponseListener);
        GetApiKeyResponse response = getApiKeyResponseListener.get();
        verifyGetResponse(2, responses, response, Collections.singleton(responses.get(0).getId()),
                Collections.singletonList(responses.get(1).getId()));
    }

    public void testGetApiKeysForRealm() throws InterruptedException, ExecutionException {
        int noOfApiKeys = randomIntBetween(3, 5);
        List<CreateApiKeyResponse> responses = createApiKeys(noOfApiKeys, null);
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        boolean invalidate= randomBoolean();
        List<String> invalidatedApiKeyIds = null;
        Set<String> expectedValidKeyIds = null;
        if (invalidate) {
            PlainActionFuture<InvalidateApiKeyResponse> listener = new PlainActionFuture<>();
            securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingApiKeyId(responses.get(0).getId(), false), listener);
            InvalidateApiKeyResponse invalidateResponse = listener.get();
            invalidatedApiKeyIds = invalidateResponse.getInvalidatedApiKeys();
            expectedValidKeyIds = responses.stream().filter(o -> !o.getId().equals(responses.get(0).getId())).map(o -> o.getId())
                    .collect(Collectors.toSet());
        } else {
            invalidatedApiKeyIds = Collections.emptyList();
            expectedValidKeyIds = responses.stream().map(o -> o.getId()).collect(Collectors.toSet());
        }

        PlainActionFuture<GetApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.usingRealmName("file"), listener);
        GetApiKeyResponse response = listener.get();
        verifyGetResponse(noOfApiKeys, responses, response,
                expectedValidKeyIds,
                invalidatedApiKeyIds);
    }

    public void testGetApiKeysForUser() throws Exception {
        int noOfApiKeys = randomIntBetween(3, 5);
        List<CreateApiKeyResponse> responses = createApiKeys(noOfApiKeys, null);
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<GetApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.usingUserName(SecuritySettingsSource.TEST_SUPERUSER), listener);
        GetApiKeyResponse response = listener.get();
        verifyGetResponse(noOfApiKeys, responses, response, responses.stream().map(o -> o.getId()).collect(Collectors.toSet()), null);
    }

    public void testGetApiKeysForRealmAndUser() throws InterruptedException, ExecutionException {
        List<CreateApiKeyResponse> responses = createApiKeys(1, null);
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<GetApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.usingRealmAndUserName("file", SecuritySettingsSource.TEST_SUPERUSER),
                listener);
        GetApiKeyResponse response = listener.get();
        verifyGetResponse(1, responses, response, Collections.singleton(responses.get(0).getId()), null);
    }

    public void testGetApiKeysForApiKeyId() throws InterruptedException, ExecutionException {
        List<CreateApiKeyResponse> responses = createApiKeys(1, null);
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<GetApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.usingApiKeyId(responses.get(0).getId(), false), listener);
        GetApiKeyResponse response = listener.get();
        verifyGetResponse(1, responses, response, Collections.singleton(responses.get(0).getId()), null);
    }

    public void testGetApiKeysForApiKeyName() throws InterruptedException, ExecutionException {
        List<CreateApiKeyResponse> responses = createApiKeys(1, null);
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                .basicAuthHeaderValue(SecuritySettingsSource.TEST_SUPERUSER, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<GetApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.usingApiKeyName(responses.get(0).getName(), false), listener);
        GetApiKeyResponse response = listener.get();
        verifyGetResponse(1, responses, response, Collections.singleton(responses.get(0).getId()), null);
    }

    public void testGetApiKeysOwnedByCurrentAuthenticatedUser() throws InterruptedException, ExecutionException {
        int noOfSuperuserApiKeys = randomIntBetween(3, 5);
        int noOfApiKeysForUserWithManageApiKeyRole = randomIntBetween(3, 5);
        List<CreateApiKeyResponse> defaultUserCreatedKeys = createApiKeys(noOfSuperuserApiKeys, null);
        String userWithManageApiKeyRole = randomFrom("user_with_manage_api_key_role", "user_with_manage_own_api_key_role");
        List<CreateApiKeyResponse> userWithManageApiKeyRoleApiKeys = createApiKeys(userWithManageApiKeyRole,
            noOfApiKeysForUserWithManageApiKeyRole, null, "monitor");
        final Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
            .basicAuthHeaderValue(userWithManageApiKeyRole, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);

        PlainActionFuture<GetApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.forOwnedApiKeys(), listener);
        GetApiKeyResponse response = listener.get();
        verifyGetResponse(userWithManageApiKeyRole, noOfApiKeysForUserWithManageApiKeyRole, userWithManageApiKeyRoleApiKeys,
            response, userWithManageApiKeyRoleApiKeys.stream().map(o -> o.getId()).collect(Collectors.toSet()), null);
    }

    public void testGetAllApiKeys() throws InterruptedException, ExecutionException {
        int noOfSuperuserApiKeys = randomIntBetween(3, 5);
        int noOfApiKeysForUserWithManageApiKeyRole = randomIntBetween(3, 5);
        int noOfApiKeysForUserWithManageOwnApiKeyRole = randomIntBetween(3,7);
        List<CreateApiKeyResponse> defaultUserCreatedKeys = createApiKeys(noOfSuperuserApiKeys, null);
        List<CreateApiKeyResponse> userWithManageApiKeyRoleApiKeys = createApiKeys("user_with_manage_api_key_role",
            noOfApiKeysForUserWithManageApiKeyRole, null, "monitor");
        List<CreateApiKeyResponse> userWithManageOwnApiKeyRoleApiKeys = createApiKeys("user_with_manage_own_api_key_role",
            noOfApiKeysForUserWithManageOwnApiKeyRole, null, "monitor");

        final Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
            .basicAuthHeaderValue("user_with_manage_api_key_role", SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        final SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<GetApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.getApiKey(new GetApiKeyRequest(), listener);
        GetApiKeyResponse response = listener.get();
        int totalApiKeys = noOfSuperuserApiKeys + noOfApiKeysForUserWithManageApiKeyRole + noOfApiKeysForUserWithManageOwnApiKeyRole;
        List<CreateApiKeyResponse> allApiKeys = new ArrayList<>();
        Stream.of(defaultUserCreatedKeys, userWithManageApiKeyRoleApiKeys, userWithManageOwnApiKeyRoleApiKeys).forEach(
            allApiKeys::addAll);
        verifyGetResponse(new String[]{SecuritySettingsSource.TEST_SUPERUSER, "user_with_manage_api_key_role",
                "user_with_manage_own_api_key_role"}, totalApiKeys, allApiKeys, response,
            allApiKeys.stream().map(o -> o.getId()).collect(Collectors.toSet()), null);
    }

    public void testGetAllApiKeysFailsForUserWithNoRoleOrRetrieveOwnApiKeyRole() throws InterruptedException, ExecutionException {
        int noOfSuperuserApiKeys = randomIntBetween(3, 5);
        int noOfApiKeysForUserWithManageApiKeyRole = randomIntBetween(3, 5);
        int noOfApiKeysForUserWithManageOwnApiKeyRole = randomIntBetween(3,7);
        List<CreateApiKeyResponse> defaultUserCreatedKeys = createApiKeys(noOfSuperuserApiKeys, null);
        List<CreateApiKeyResponse> userWithManageApiKeyRoleApiKeys = createApiKeys("user_with_manage_api_key_role",
            noOfApiKeysForUserWithManageApiKeyRole, null, "monitor");
        List<CreateApiKeyResponse> userWithManageOwnApiKeyRoleApiKeys = createApiKeys("user_with_manage_own_api_key_role",
            noOfApiKeysForUserWithManageOwnApiKeyRole, null, "monitor");

        final String withUser = randomFrom("user_with_manage_own_api_key_role", "user_with_no_api_key_role");
        final Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
            .basicAuthHeaderValue(withUser, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        final SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<GetApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.getApiKey(new GetApiKeyRequest(), listener);
        ElasticsearchSecurityException ese = expectThrows(ElasticsearchSecurityException.class, () -> listener.actionGet());
        assertErrorMessage(ese, "cluster:admin/xpack/security/api_key/get", withUser);
    }

    public void testInvalidateApiKeysOwnedByCurrentAuthenticatedUser() throws InterruptedException, ExecutionException {
        int noOfSuperuserApiKeys = randomIntBetween(3, 5);
        int noOfApiKeysForUserWithManageApiKeyRole = randomIntBetween(3, 5);
        List<CreateApiKeyResponse> defaultUserCreatedKeys = createApiKeys(noOfSuperuserApiKeys, null);
        String userWithManageApiKeyRole = randomFrom("user_with_manage_api_key_role", "user_with_manage_own_api_key_role");
        List<CreateApiKeyResponse> userWithManageApiKeyRoleApiKeys = createApiKeys(userWithManageApiKeyRole,
            noOfApiKeysForUserWithManageApiKeyRole, null, "monitor");
        final Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
            .basicAuthHeaderValue(userWithManageApiKeyRole, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
        SecurityClient securityClient = new SecurityClient(client);

        PlainActionFuture<InvalidateApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.forOwnedApiKeys(), listener);
        InvalidateApiKeyResponse invalidateResponse = listener.get();

        verifyInvalidateResponse(noOfApiKeysForUserWithManageApiKeyRole, userWithManageApiKeyRoleApiKeys, invalidateResponse);
    }

    public void testApiKeyAuthorizationApiKeyMustBeAbleToRetrieveItsOwnInformationButNotAnyOtherKeysCreatedBySameOwner()
        throws InterruptedException, ExecutionException {
        List<CreateApiKeyResponse> responses = createApiKeys(SecuritySettingsSource.TEST_SUPERUSER,2, null, (String[]) null);
        final String base64ApiKeyKeyValue = Base64.getEncoder().encodeToString(
            (responses.get(0).getId() + ":" + responses.get(0).getKey().toString()).getBytes(StandardCharsets.UTF_8));
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", "ApiKey " + base64ApiKeyKeyValue));
        SecurityClient securityClient = new SecurityClient(client);
        PlainActionFuture<GetApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.usingApiKeyId(responses.get(0).getId(), randomBoolean()), listener);
        GetApiKeyResponse response = listener.get();
        verifyGetResponse(1, responses, response, Collections.singleton(responses.get(0).getId()), null);

        final PlainActionFuture<GetApiKeyResponse> failureListener = new PlainActionFuture<>();
        // for any other API key id, it must deny access
        securityClient.getApiKey(GetApiKeyRequest.usingApiKeyId(responses.get(1).getId(), randomBoolean()),
            failureListener);
        ElasticsearchSecurityException ese = expectThrows(ElasticsearchSecurityException.class, () -> failureListener.actionGet());
        assertErrorMessage(ese, "cluster:admin/xpack/security/api_key/get", SecuritySettingsSource.TEST_SUPERUSER,
            responses.get(0).getId());

        final PlainActionFuture<GetApiKeyResponse> failureListener1 = new PlainActionFuture<>();
        securityClient.getApiKey(GetApiKeyRequest.forOwnedApiKeys(), failureListener1);
        ese = expectThrows(ElasticsearchSecurityException.class, () -> failureListener1.actionGet());
        assertErrorMessage(ese, "cluster:admin/xpack/security/api_key/get", SecuritySettingsSource.TEST_SUPERUSER,
            responses.get(0).getId());
    }

    public void testApiKeyWithManageOwnPrivilegeIsAbleToInvalidateItselfButNotAnyOtherKeysCreatedBySameOwner()
        throws InterruptedException, ExecutionException {
        List<CreateApiKeyResponse> responses = createApiKeys(SecuritySettingsSource.TEST_SUPERUSER, 2, null, "manage_own_api_key");
        final String base64ApiKeyKeyValue = Base64.getEncoder().encodeToString(
            (responses.get(0).getId() + ":" + responses.get(0).getKey().toString()).getBytes(StandardCharsets.UTF_8));
        Client client = client().filterWithHeader(Collections.singletonMap("Authorization", "ApiKey " + base64ApiKeyKeyValue));
        SecurityClient securityClient = new SecurityClient(client);

        final PlainActionFuture<InvalidateApiKeyResponse> failureListener = new PlainActionFuture<>();
        // for any other API key id, it must deny access
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingApiKeyId(responses.get(1).getId(), randomBoolean()),
            failureListener);
        ElasticsearchSecurityException ese = expectThrows(ElasticsearchSecurityException.class, () -> failureListener.actionGet());
        assertErrorMessage(ese, "cluster:admin/xpack/security/api_key/invalidate", SecuritySettingsSource.TEST_SUPERUSER,
            responses.get(0).getId());

        final PlainActionFuture<InvalidateApiKeyResponse> failureListener1 = new PlainActionFuture<>();
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.forOwnedApiKeys(), failureListener1);
        ese = expectThrows(ElasticsearchSecurityException.class, () -> failureListener1.actionGet());
        assertErrorMessage(ese, "cluster:admin/xpack/security/api_key/invalidate", SecuritySettingsSource.TEST_SUPERUSER,
            responses.get(0).getId());

        PlainActionFuture<InvalidateApiKeyResponse> listener = new PlainActionFuture<>();
        securityClient.invalidateApiKey(InvalidateApiKeyRequest.usingApiKeyId(responses.get(0).getId(), randomBoolean()),
            listener);
        InvalidateApiKeyResponse invalidateResponse = listener.get();

        assertThat(invalidateResponse.getInvalidatedApiKeys().size(), equalTo(1));
        assertThat(invalidateResponse.getInvalidatedApiKeys(), containsInAnyOrder(responses.get(0).getId()));
        assertThat(invalidateResponse.getPreviouslyInvalidatedApiKeys().size(), equalTo(0));
        assertThat(invalidateResponse.getErrors().size(), equalTo(0));
    }

    private void verifyGetResponse(int expectedNumberOfApiKeys, List<CreateApiKeyResponse> responses,
                                   GetApiKeyResponse response, Set<String> validApiKeyIds, List<String> invalidatedApiKeyIds) {
        verifyGetResponse(SecuritySettingsSource.TEST_SUPERUSER, expectedNumberOfApiKeys, responses, response, validApiKeyIds,
            invalidatedApiKeyIds);
    }

    private void verifyGetResponse(String user, int expectedNumberOfApiKeys, List<CreateApiKeyResponse> responses,
                                   GetApiKeyResponse response, Set<String> validApiKeyIds, List<String> invalidatedApiKeyIds) {
        verifyGetResponse(new String[]{user}, expectedNumberOfApiKeys, responses, response, validApiKeyIds, invalidatedApiKeyIds);
    }

    private void verifyGetResponse(String[] user, int expectedNumberOfApiKeys, List<CreateApiKeyResponse> responses,
                                   GetApiKeyResponse response, Set<String> validApiKeyIds, List<String> invalidatedApiKeyIds) {
        assertThat(response.getApiKeyInfos().length, equalTo(expectedNumberOfApiKeys));
        List<String> expectedIds = responses.stream().filter(o -> validApiKeyIds.contains(o.getId())).map(o -> o.getId())
                .collect(Collectors.toList());
        List<String> actualIds = Arrays.stream(response.getApiKeyInfos()).filter(o -> o.isInvalidated() == false).map(o -> o.getId())
                .collect(Collectors.toList());
        assertThat(actualIds, containsInAnyOrder(expectedIds.toArray(Strings.EMPTY_ARRAY)));
        List<String> expectedNames = responses.stream().filter(o -> validApiKeyIds.contains(o.getId())).map(o -> o.getName())
                .collect(Collectors.toList());
        List<String> actualNames = Arrays.stream(response.getApiKeyInfos()).filter(o -> o.isInvalidated() == false).map(o -> o.getName())
                .collect(Collectors.toList());
        assertThat(actualNames, containsInAnyOrder(expectedNames.toArray(Strings.EMPTY_ARRAY)));
        Set<String> expectedUsernames = (validApiKeyIds.isEmpty()) ? Collections.emptySet()
                : Sets.newHashSet(user);
        Set<String> actualUsernames = Arrays.stream(response.getApiKeyInfos()).filter(o -> o.isInvalidated() == false)
                .map(o -> o.getUsername()).collect(Collectors.toSet());
        assertThat(actualUsernames, containsInAnyOrder(expectedUsernames.toArray(Strings.EMPTY_ARRAY)));
        if (invalidatedApiKeyIds != null) {
            List<String> actualInvalidatedApiKeyIds = Arrays.stream(response.getApiKeyInfos()).filter(o -> o.isInvalidated())
                    .map(o -> o.getId()).collect(Collectors.toList());
            assertThat(invalidatedApiKeyIds, containsInAnyOrder(actualInvalidatedApiKeyIds.toArray(Strings.EMPTY_ARRAY)));
        }
    }

    private List<CreateApiKeyResponse> createApiKeys(int noOfApiKeys, TimeValue expiration) {
        return createApiKeys(SecuritySettingsSource.TEST_SUPERUSER, noOfApiKeys, expiration, "monitor");
    }

    private List<CreateApiKeyResponse> createApiKeys(String user, int noOfApiKeys, TimeValue expiration, String... clusterPrivileges) {
        List<CreateApiKeyResponse> responses = new ArrayList<>();
        for (int i = 0; i < noOfApiKeys; i++) {
            final RoleDescriptor descriptor = new RoleDescriptor("role", clusterPrivileges, null, null);
            Client client = client().filterWithHeader(Collections.singletonMap("Authorization", UsernamePasswordToken
                    .basicAuthHeaderValue(user, SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING)));
            SecurityClient securityClient = new SecurityClient(client);
            final CreateApiKeyResponse response = securityClient.prepareCreateApiKey()
                    .setName("test-key-" + randomAlphaOfLengthBetween(5, 9) + i).setExpiration(expiration)
                    .setRoleDescriptors(Collections.singletonList(descriptor)).get();
            assertNotNull(response.getId());
            assertNotNull(response.getKey());
            responses.add(response);
        }
        assertThat(responses.size(), is(noOfApiKeys));
        return responses;
    }

    private void assertErrorMessage(final ElasticsearchSecurityException ese, String action, String userName, String apiKeyId) {
        assertThat(ese.getMessage(),
            is("action [" + action + "] is unauthorized for API key id [" + apiKeyId + "] of user [" + userName + "]"));
    }

    private void assertErrorMessage(final ElasticsearchSecurityException ese, String action, String userName) {
        assertThat(ese.getMessage(),
            is("action [" + action + "] is unauthorized for user [" + userName + "]"));
    }
}
