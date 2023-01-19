/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.authz;

import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.CompositeIndicesRequest;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.LatchedActionListener;
import org.elasticsearch.action.MockIndicesRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthAction;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesAction;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsAction;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexAction;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.recovery.RecoveryAction;
import org.elasticsearch.action.admin.indices.recovery.RecoveryRequest;
import org.elasticsearch.action.admin.indices.segments.IndicesSegmentsAction;
import org.elasticsearch.action.admin.indices.segments.IndicesSegmentsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsAction;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsAction;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresAction;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsAction;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.upgrade.get.UpgradeStatusAction;
import org.elasticsearch.action.admin.indices.upgrade.get.UpgradeStatusRequest;
import org.elasticsearch.action.bulk.BulkAction;
import org.elasticsearch.action.bulk.BulkItemRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkShardRequest;
import org.elasticsearch.action.delete.DeleteAction;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetAction;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.MultiGetAction;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollAction;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.MultiSearchAction;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchScrollAction;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchTransportService;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.termvectors.MultiTermVectorsAction;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequest;
import org.elasticsearch.action.termvectors.TermVectorsAction;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.action.update.UpdateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.AliasOrIndex;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.TriFunction;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.util.concurrent.ThreadContext.StoredContext;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.license.XPackLicenseState;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportActionProxy;
import org.elasticsearch.transport.TransportRequest;
import org.elasticsearch.xpack.core.security.action.privilege.DeletePrivilegesAction;
import org.elasticsearch.xpack.core.security.action.privilege.DeletePrivilegesRequest;
import org.elasticsearch.xpack.core.security.action.user.AuthenticateAction;
import org.elasticsearch.xpack.core.security.action.user.AuthenticateRequest;
import org.elasticsearch.xpack.core.security.action.user.GetUserPrivilegesRequest;
import org.elasticsearch.xpack.core.security.action.user.GetUserPrivilegesResponse;
import org.elasticsearch.xpack.core.security.action.user.HasPrivilegesRequest;
import org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse;
import org.elasticsearch.xpack.core.security.authc.Authentication;
import org.elasticsearch.xpack.core.security.authc.Authentication.RealmRef;
import org.elasticsearch.xpack.core.security.authc.DefaultAuthenticationFailureHandler;
import org.elasticsearch.xpack.core.security.authz.AuthorizationEngine;
import org.elasticsearch.xpack.core.security.authz.AuthorizationEngine.AuthorizationInfo;
import org.elasticsearch.xpack.core.security.authz.AuthorizationServiceField;
import org.elasticsearch.xpack.core.security.authz.IndicesAndAliasesResolverField;
import org.elasticsearch.xpack.core.security.authz.ResolvedIndices;
import org.elasticsearch.xpack.core.security.authz.RoleDescriptor;
import org.elasticsearch.xpack.core.security.authz.RoleDescriptor.IndicesPrivileges;
import org.elasticsearch.xpack.core.security.authz.accesscontrol.IndicesAccessControl;
import org.elasticsearch.xpack.core.security.authz.permission.ClusterPermission;
import org.elasticsearch.xpack.core.security.authz.permission.FieldPermissionsCache;
import org.elasticsearch.xpack.core.security.authz.permission.Role;
import org.elasticsearch.xpack.core.security.authz.privilege.ActionClusterPrivilege;
import org.elasticsearch.xpack.core.security.authz.privilege.ApplicationPrivilege;
import org.elasticsearch.xpack.core.security.authz.privilege.ApplicationPrivilegeDescriptor;
import org.elasticsearch.xpack.core.security.authz.privilege.ClusterPrivilegeResolver;
import org.elasticsearch.xpack.core.security.authz.privilege.ConfigurableClusterPrivilege;
import org.elasticsearch.xpack.core.security.authz.store.ReservedRolesStore;
import org.elasticsearch.xpack.core.security.user.AnonymousUser;
import org.elasticsearch.xpack.core.security.user.ElasticUser;
import org.elasticsearch.xpack.core.security.user.KibanaUser;
import org.elasticsearch.xpack.core.security.user.SystemUser;
import org.elasticsearch.xpack.core.security.user.User;
import org.elasticsearch.xpack.core.security.user.XPackSecurityUser;
import org.elasticsearch.xpack.core.security.user.XPackUser;
import org.elasticsearch.xpack.security.audit.AuditLevel;
import org.elasticsearch.xpack.security.audit.AuditTrailService;
import org.elasticsearch.xpack.security.audit.AuditUtil;
import org.elasticsearch.xpack.security.authz.store.CompositeRolesStore;
import org.elasticsearch.xpack.security.authz.store.NativePrivilegeStore;
import org.elasticsearch.xpack.sql.action.SqlQueryAction;
import org.elasticsearch.xpack.sql.action.SqlQueryRequest;
import org.junit.Before;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.elasticsearch.test.SecurityTestsUtils.assertAuthenticationException;
import static org.elasticsearch.test.SecurityTestsUtils.assertThrowsAuthorizationException;
import static org.elasticsearch.test.SecurityTestsUtils.assertThrowsAuthorizationExceptionRunAs;
import static org.elasticsearch.xpack.security.audit.logfile.LoggingAuditTrail.PRINCIPAL_ROLES_FIELD_NAME;
import static org.elasticsearch.xpack.core.security.index.RestrictedIndicesNames.INTERNAL_SECURITY_MAIN_INDEX_7;
import static org.elasticsearch.xpack.core.security.index.RestrictedIndicesNames.SECURITY_MAIN_ALIAS;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AuthorizationServiceTests extends ESTestCase {
    private AuditTrailService auditTrail;
    private ClusterService clusterService;
    private AuthorizationService authorizationService;
    private ThreadContext threadContext;
    private ThreadPool threadPool;
    private Map<String, RoleDescriptor> roleMap = new HashMap<>();
    private CompositeRolesStore rolesStore;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        rolesStore = mock(CompositeRolesStore.class);
        clusterService = mock(ClusterService.class);
        final Settings settings = Settings.builder()
            .put("cluster.remote.other_cluster.seeds", "localhost:9999")
            .build();
        final ClusterSettings clusterSettings = new ClusterSettings(settings, ClusterSettings.BUILT_IN_CLUSTER_SETTINGS);
        when(clusterService.getClusterSettings()).thenReturn(clusterSettings);
        when(clusterService.state()).thenReturn(ClusterState.EMPTY_STATE);
        auditTrail = mock(AuditTrailService.class);
        threadContext = new ThreadContext(settings);
        threadPool = mock(ThreadPool.class);
        when(threadPool.getThreadContext()).thenReturn(threadContext);
        final FieldPermissionsCache fieldPermissionsCache = new FieldPermissionsCache(settings);

        final NativePrivilegeStore privilegesStore = mock(NativePrivilegeStore.class);
        doAnswer(i -> {
                assertThat(i.getArguments().length, equalTo(3));
                final Object arg2 = i.getArguments()[2];
                assertThat(arg2, instanceOf(ActionListener.class));
                ActionListener<Collection<ApplicationPrivilege>> listener = (ActionListener<Collection<ApplicationPrivilege>>) arg2;
                listener.onResponse(Collections.emptyList());
                return null;
            }
        ).when(privilegesStore).getPrivileges(any(Collection.class), any(Collection.class), any(ActionListener.class));

        doAnswer((i) -> {
            ActionListener<Role> callback = (ActionListener<Role>) i.getArguments()[2];
            User user = (User) i.getArguments()[0];
            Set<String> names = new HashSet<>(Arrays.asList(user.roles()));
            assertNotNull(names);
            Set<RoleDescriptor> roleDescriptors = new HashSet<>();
            for (String name : names) {
                RoleDescriptor descriptor = roleMap.get(name);
                if (descriptor != null) {
                    roleDescriptors.add(descriptor);
                }
            }

            if (roleDescriptors.isEmpty()) {
                callback.onResponse(Role.EMPTY);
            } else {
                CompositeRolesStore.buildRoleFromDescriptors(roleDescriptors, fieldPermissionsCache, privilegesStore,
                    ActionListener.wrap(r -> callback.onResponse(r), callback::onFailure)
                );
            }
            return Void.TYPE;
        }).when(rolesStore).getRoles(any(User.class), any(Authentication.class), any(ActionListener.class));
        roleMap.put(ReservedRolesStore.SUPERUSER_ROLE_DESCRIPTOR.getName(), ReservedRolesStore.SUPERUSER_ROLE_DESCRIPTOR);
        authorizationService = new AuthorizationService(settings, rolesStore, clusterService,
            auditTrail, new DefaultAuthenticationFailureHandler(Collections.emptyMap()), threadPool, new AnonymousUser(settings), null,
            Collections.emptySet(), new XPackLicenseState(settings));
    }

    private void authorize(Authentication authentication, String action, TransportRequest request) {
        PlainActionFuture<Void> future = new PlainActionFuture<>();
        authorizationService.authorize(authentication, action, request, future);
        future.actionGet();
    }

    public void testActionsForSystemUserIsAuthorized() throws IOException {
        final TransportRequest request = mock(TransportRequest.class);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        // A failure would throw an exception
        final Authentication authentication = createAuthentication(SystemUser.INSTANCE);
        final String[] actions = {
                "indices:monitor/whatever",
                "internal:whatever",
                "cluster:monitor/whatever",
                "cluster:admin/reroute",
                "indices:admin/mapping/put",
                "indices:admin/template/put",
                "indices:admin/seq_no/global_checkpoint_sync",
                "indices:admin/seq_no/retention_lease_sync",
                "indices:admin/seq_no/retention_lease_background_sync",
                "indices:admin/seq_no/add_retention_lease",
                "indices:admin/seq_no/remove_retention_lease",
                "indices:admin/seq_no/renew_retention_lease",
                "indices:admin/settings/update" };
        for (String action : actions) {
            authorize(authentication, action, request);
            verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(action), eq(request),
                authzInfoRoles(new String[] { SystemUser.ROLE_NAME }));
        }

        verifyNoMoreInteractions(auditTrail);
    }

    public void testIndicesActionsForSystemUserWhichAreNotAuthorized() throws IOException {
        final TransportRequest request = mock(TransportRequest.class);
        final Authentication authentication = createAuthentication(SystemUser.INSTANCE);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        assertThrowsAuthorizationException(
            () -> authorize(authentication, "indices:", request),
            "indices:", SystemUser.INSTANCE.principal());
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq("indices:"), eq(request),
            authzInfoRoles(new String[]{SystemUser.ROLE_NAME}));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testClusterAdminActionsForSystemUserWhichAreNotAuthorized() throws IOException {
        final TransportRequest request = mock(TransportRequest.class);
        final Authentication authentication = createAuthentication(SystemUser.INSTANCE);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        assertThrowsAuthorizationException(
            () -> authorize(authentication, "cluster:admin/whatever", request),
            "cluster:admin/whatever", SystemUser.INSTANCE.principal());
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq("cluster:admin/whatever"), eq(request),
            authzInfoRoles(new String[] { SystemUser.ROLE_NAME }));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testClusterAdminSnapshotStatusActionForSystemUserWhichIsNotAuthorized() throws IOException {
        final TransportRequest request = mock(TransportRequest.class);
        final Authentication authentication = createAuthentication(SystemUser.INSTANCE);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        assertThrowsAuthorizationException(
            () -> authorize(authentication, "cluster:admin/snapshot/status", request),
            "cluster:admin/snapshot/status", SystemUser.INSTANCE.principal());
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq("cluster:admin/snapshot/status"), eq(request),
            authzInfoRoles(new String[] { SystemUser.ROLE_NAME }));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testAuthorizeUsingConditionalPrivileges() throws IOException {
        final DeletePrivilegesRequest request = new DeletePrivilegesRequest();
        final Authentication authentication = createAuthentication(new User("user1", "role1"));

        final ConfigurableClusterPrivilege configurableClusterPrivilege = new MockConfigurableClusterPrivilege() {
            @Override
            public ClusterPermission.Builder buildPermission(ClusterPermission.Builder builder) {
                final Predicate<TransportRequest> requestPredicate = r -> r == request;
                builder.add(this, ((ActionClusterPrivilege) ClusterPrivilegeResolver.MANAGE_SECURITY).getAllowedActionPatterns(),
                    requestPredicate);
                return builder;
            }
        };
        final ConfigurableClusterPrivilege[] configurableClusterPrivileges = new ConfigurableClusterPrivilege[] {
            configurableClusterPrivilege
        };
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        RoleDescriptor role = new RoleDescriptor("role1", null, null, null, configurableClusterPrivileges, null, null ,null);
        roleMap.put("role1", role);

        authorize(authentication, DeletePrivilegesAction.NAME, request);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(DeletePrivilegesAction.NAME), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testAuthorizationDeniedWhenConditionalPrivilegesDoNotMatch() throws IOException {
        final DeletePrivilegesRequest request = new DeletePrivilegesRequest();
        final Authentication authentication = createAuthentication(new User("user1", "role1"));

        final ConfigurableClusterPrivilege configurableClusterPrivilege = new MockConfigurableClusterPrivilege() {
            @Override
            public ClusterPermission.Builder buildPermission(ClusterPermission.Builder builder) {
                final Predicate<TransportRequest> requestPredicate = r -> false;
                builder.add(this, ((ActionClusterPrivilege) ClusterPrivilegeResolver.MANAGE_SECURITY).getAllowedActionPatterns(),
                    requestPredicate);
                return builder;
            }
        };
        final ConfigurableClusterPrivilege[] configurableClusterPrivileges = new ConfigurableClusterPrivilege[] {
            configurableClusterPrivilege
        };
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        RoleDescriptor role = new RoleDescriptor("role1", null, null, null, configurableClusterPrivileges, null, null ,null);
        roleMap.put("role1", role);

        assertThrowsAuthorizationException(
            () -> authorize(authentication, DeletePrivilegesAction.NAME, request),
            DeletePrivilegesAction.NAME, "user1");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(DeletePrivilegesAction.NAME), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testNoRolesCausesDenial() throws IOException {
        final TransportRequest request = new SearchRequest();
        final Authentication authentication = createAuthentication(new User("test user"));
        mockEmptyMetaData();
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        assertThrowsAuthorizationException(
            () -> authorize(authentication, "indices:a", request),
            "indices:a", "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq("indices:a"), eq(request),
            authzInfoRoles(Role.EMPTY.names()));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testUserWithNoRolesCanPerformRemoteSearch() throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("other_cluster:index1", "*_cluster:index2", "other_cluster:other_*");
        final Authentication authentication = createAuthentication(new User("test user"));
        mockEmptyMetaData();
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        authorize(authentication, SearchAction.NAME, request);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(SearchAction.NAME), eq(request),
            authzInfoRoles(Role.EMPTY.names()));
        verifyNoMoreInteractions(auditTrail);
    }

    /**
     * This test mimics {@link #testUserWithNoRolesCanPerformRemoteSearch()} except that
     * while the referenced index _looks_ like a remote index, the remote cluster name has not
     * been defined, so it is actually a local index and access should be denied
     */
    public void testUserWithNoRolesCannotPerformLocalSearch() throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("no_such_cluster:index");
        final Authentication authentication = createAuthentication(new User("test user"));
        mockEmptyMetaData();
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        assertThrowsAuthorizationException(
            () -> authorize(authentication, SearchAction.NAME, request),
            SearchAction.NAME, "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(SearchAction.NAME), eq(request),
            authzInfoRoles(Role.EMPTY.names()));
        verifyNoMoreInteractions(auditTrail);
    }

    /**
     * This test mimics {@link #testUserWithNoRolesCannotPerformLocalSearch()} but includes
     * both local and remote indices, including wildcards
     */
    public void testUserWithNoRolesCanPerformMultiClusterSearch() throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("local_index", "wildcard_*", "other_cluster:remote_index", "*:foo?");
        final Authentication authentication = createAuthentication(new User("test user"));
        mockEmptyMetaData();
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        assertThrowsAuthorizationException(
            () -> authorize(authentication, SearchAction.NAME, request),
            SearchAction.NAME, "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(SearchAction.NAME), eq(request),
            authzInfoRoles(Role.EMPTY.names()));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testUserWithNoRolesCannotSql() throws IOException {
        TransportRequest request = new SqlQueryRequest();
        Authentication authentication = createAuthentication(new User("test user"));
        mockEmptyMetaData();
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        assertThrowsAuthorizationException(
            () -> authorize(authentication, SqlQueryAction.NAME, request),
            SqlQueryAction.NAME, "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(SqlQueryAction.NAME), eq(request),
            authzInfoRoles(Role.EMPTY.names()));
        verifyNoMoreInteractions(auditTrail);
    }
    /**
     * Verifies that the behaviour tested in {@link #testUserWithNoRolesCanPerformRemoteSearch}
     * does not work for requests that are not remote-index-capable.
     */
    public void testRemoteIndicesOnlyWorkWithApplicableRequestTypes() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest();
        request.indices("other_cluster:index1", "other_cluster:index2");
        final Authentication authentication = createAuthentication(new User("test user"));
        mockEmptyMetaData();
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        assertThrowsAuthorizationException(
            () -> authorize(authentication, DeleteIndexAction.NAME, request),
            DeleteIndexAction.NAME, "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(DeleteIndexAction.NAME), eq(request),
            authzInfoRoles(Role.EMPTY.names()));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testUnknownRoleCausesDenial() throws IOException {
        Tuple<String, TransportRequest> tuple = randomFrom(asList(
            new Tuple<>(SearchAction.NAME, new SearchRequest()),
            new Tuple<>(IndicesExistsAction.NAME, new IndicesExistsRequest()),
            new Tuple<>(SqlQueryAction.NAME, new SqlQueryRequest())));
        String action = tuple.v1();
        TransportRequest request = tuple.v2();
        final Authentication authentication = createAuthentication(new User("test user", "non-existent-role"));
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        mockEmptyMetaData();
        assertThrowsAuthorizationException(
            () -> authorize(authentication, action, request),
            action, "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(action), eq(request), authzInfoRoles(Role.EMPTY.names()));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testThatNonIndicesAndNonClusterActionIsDenied() throws IOException {
        final TransportRequest request = mock(TransportRequest.class);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        final Authentication authentication = createAuthentication(new User("test user", "a_all"));
        final RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()}, null);
        roleMap.put("a_all", role);

        assertThrowsAuthorizationException(
            () -> authorize(authentication, "whatever", request),
            "whatever", "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq("whatever"), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testThatRoleWithNoIndicesIsDenied() throws IOException {
        @SuppressWarnings("unchecked")
        Tuple<String, TransportRequest> tuple = randomFrom(
            new Tuple<>(SearchAction.NAME, new SearchRequest()),
            new Tuple<>(IndicesExistsAction.NAME, new IndicesExistsRequest()),
            new Tuple<>(SqlQueryAction.NAME, new SqlQueryRequest()));
        String action = tuple.v1();
        TransportRequest request = tuple.v2();
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        final Authentication authentication = createAuthentication(new User("test user", "no_indices"));
        RoleDescriptor role = new RoleDescriptor("no_indices", null, null, null);
        roleMap.put("no_indices", role);
        mockEmptyMetaData();

        assertThrowsAuthorizationException(
            () -> authorize(authentication, action, request),
            action, "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(action), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testElasticUserAuthorizedForNonChangePasswordRequestsWhenNotInSetupMode() throws IOException {
        final Authentication authentication = createAuthentication(new ElasticUser(true));
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        final Tuple<String, TransportRequest> request = randomCompositeRequest();
        authorize(authentication, request.v1(), request.v2());

        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(request.v1()), eq(request.v2()),
            authzInfoRoles(new String[]{ElasticUser.ROLE_NAME}));
    }

    public void testSearchAgainstEmptyCluster() throws Exception {
        RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()}, null);
        final Authentication authentication = createAuthentication(new User("test user", "a_all"));
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        roleMap.put("a_all", role);
        mockEmptyMetaData();

        {
            //ignore_unavailable set to false, user is not authorized for this index nor does it exist
            SearchRequest searchRequest = new SearchRequest("does_not_exist")
                .indicesOptions(IndicesOptions.fromOptions(false, true,
                    true, false));

            assertThrowsAuthorizationException(
                () -> authorize(authentication, SearchAction.NAME, searchRequest),
                SearchAction.NAME, "test user");
            verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(SearchAction.NAME), eq(searchRequest),
                authzInfoRoles(new String[]{role.getName()}));
            verifyNoMoreInteractions(auditTrail);
        }

        {
            //ignore_unavailable and allow_no_indices both set to true, user is not authorized for this index nor does it exist
            SearchRequest searchRequest = new SearchRequest("does_not_exist")
                .indicesOptions(IndicesOptions.fromOptions(true, true, true, false));
            final ActionListener<Void> listener = ActionListener.wrap(ignore -> {
                final IndicesAccessControl indicesAccessControl =
                    threadContext.getTransient(AuthorizationServiceField.INDICES_PERMISSIONS_KEY);
                assertNotNull(indicesAccessControl);
                final IndicesAccessControl.IndexAccessControl indexAccessControl =
                    indicesAccessControl.getIndexPermissions(IndicesAndAliasesResolverField.NO_INDEX_PLACEHOLDER);
                assertFalse(indexAccessControl.getFieldPermissions().hasFieldLevelSecurity());
                assertFalse(indexAccessControl.getDocumentPermissions().hasDocumentLevelPermissions());
            }, e -> {
                fail(e.getMessage());
            });
            final CountDownLatch latch = new CountDownLatch(1);
            authorizationService.authorize(authentication, SearchAction.NAME, searchRequest, new LatchedActionListener<>(listener, latch));
            latch.await();
            verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(SearchAction.NAME), eq(searchRequest),
                authzInfoRoles(new String[]{role.getName()}));
        }
    }

    public void testScrollRelatedRequestsAllowed() {
        RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()}, null);
        final Authentication authentication = createAuthentication(new User("test user", "a_all"));
        roleMap.put("a_all", role);
        mockEmptyMetaData();
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        final ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        authorize(authentication, ClearScrollAction.NAME, clearScrollRequest);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(ClearScrollAction.NAME), eq(clearScrollRequest),
            authzInfoRoles(new String[]{role.getName()}));

        final SearchScrollRequest searchScrollRequest = new SearchScrollRequest();
        authorize(authentication, SearchScrollAction.NAME, searchScrollRequest);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(SearchScrollAction.NAME), eq(searchScrollRequest),
            authzInfoRoles(new String[]{role.getName()}));

        // We have to use a mock request for other Scroll actions as the actual requests are package private to SearchTransportService
        final TransportRequest request = mock(TransportRequest.class);
        authorize(authentication, SearchTransportService.CLEAR_SCROLL_CONTEXTS_ACTION_NAME, request);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(SearchTransportService.CLEAR_SCROLL_CONTEXTS_ACTION_NAME),
            eq(request), authzInfoRoles(new String[]{role.getName()}));

        authorize(authentication, SearchTransportService.FETCH_ID_SCROLL_ACTION_NAME, request);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(SearchTransportService.FETCH_ID_SCROLL_ACTION_NAME),
            eq(request), authzInfoRoles(new String[]{role.getName()}));

        authorize(authentication, SearchTransportService.QUERY_FETCH_SCROLL_ACTION_NAME, request);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(SearchTransportService.QUERY_FETCH_SCROLL_ACTION_NAME),
            eq(request), authzInfoRoles(new String[]{role.getName()}));

        authorize(authentication, SearchTransportService.QUERY_SCROLL_ACTION_NAME, request);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(SearchTransportService.QUERY_SCROLL_ACTION_NAME),
            eq(request), authzInfoRoles(new String[]{role.getName()}));

        authorize(authentication, SearchTransportService.FREE_CONTEXT_SCROLL_ACTION_NAME, request);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(SearchTransportService.FREE_CONTEXT_SCROLL_ACTION_NAME),
            eq(request), authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testAuthorizeIndicesFailures() throws IOException {
        TransportRequest request = new GetIndexRequest().indices("b");
        ClusterState state = mockEmptyMetaData();
        RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()}, null);
        final Authentication authentication = createAuthentication(new User("test user", "a_all"));
        roleMap.put("a_all", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        assertThrowsAuthorizationException(
            () -> authorize(authentication, "indices:a", request),
            "indices:a", "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq("indices:a"), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
        verify(clusterService, times(1)).state();
        verify(state, times(1)).metaData();
    }

    public void testCreateIndexWithAliasWithoutPermissions() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("a");
        request.alias(new Alias("a2"));
        ClusterState state = mockEmptyMetaData();
        RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()}, null);
        final Authentication authentication = createAuthentication(new User("test user", "a_all"));
        roleMap.put("a_all", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        assertThrowsAuthorizationException(
            () -> authorize(authentication, CreateIndexAction.NAME, request),
            IndicesAliasesAction.NAME, "test user");
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(CreateIndexAction.NAME), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(IndicesAliasesAction.NAME), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
        verify(clusterService).state();
        verify(state, times(1)).metaData();
    }

    public void testCreateIndexWithAlias() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("a");
        request.alias(new Alias("a2"));
        ClusterState state = mockEmptyMetaData();
        RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a", "a2").privileges("all").build()}, null);
        final Authentication authentication = createAuthentication(new User("test user", "a_all"));
        roleMap.put("a_all", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        authorize(authentication, CreateIndexAction.NAME, request);

        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(CreateIndexAction.NAME), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq("indices:admin/aliases"), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
        verify(clusterService).state();
        verify(state, times(1)).metaData();
    }

    public void testDenialForAnonymousUser() throws IOException {
        TransportRequest request = new GetIndexRequest().indices("b");
        ClusterState state = mockEmptyMetaData();
        Settings settings = Settings.builder().put(AnonymousUser.ROLES_SETTING.getKey(), "a_all").build();
        final AnonymousUser anonymousUser = new AnonymousUser(settings);
        authorizationService = new AuthorizationService(settings, rolesStore, clusterService, auditTrail,
            new DefaultAuthenticationFailureHandler(Collections.emptyMap()), threadPool, anonymousUser, null, Collections.emptySet(),
            new XPackLicenseState(settings));

        RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[] { IndicesPrivileges.builder().indices("a").privileges("all").build() }, null);
        roleMap.put("a_all", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        final Authentication authentication = createAuthentication(anonymousUser);
        assertThrowsAuthorizationException(
            () -> authorize(authentication, "indices:a", request),
            "indices:a", anonymousUser.principal());
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq("indices:a"), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
        verify(clusterService, times(1)).state();
        verify(state, times(1)).metaData();
    }

    public void testDenialForAnonymousUserAuthorizationExceptionDisabled() throws IOException {
        TransportRequest request = new GetIndexRequest().indices("b");
        ClusterState state = mockEmptyMetaData();
        Settings settings = Settings.builder()
            .put(AnonymousUser.ROLES_SETTING.getKey(), "a_all")
            .put(AuthorizationService.ANONYMOUS_AUTHORIZATION_EXCEPTION_SETTING.getKey(), false)
            .build();
        final Authentication authentication = createAuthentication(new AnonymousUser(settings));
        authorizationService = new AuthorizationService(settings, rolesStore, clusterService, auditTrail,
            new DefaultAuthenticationFailureHandler(Collections.emptyMap()), threadPool, new AnonymousUser(settings), null,
            Collections.emptySet(), new XPackLicenseState(settings));

        RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()}, null);
        roleMap.put("a_all", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        final ElasticsearchSecurityException securityException = expectThrows(ElasticsearchSecurityException.class,
            () -> authorize(authentication, "indices:a", request));
        assertAuthenticationException(securityException, containsString("action [indices:a] requires authentication"));
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq("indices:a"), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
        verify(clusterService, times(1)).state();
        verify(state, times(1)).metaData();
    }

    public void testAuditTrailIsRecordedWhenIndexWildcardThrowsError() throws IOException {
        IndicesOptions options = IndicesOptions.fromOptions(false, false, true, true);
        TransportRequest request = new GetIndexRequest().indices("not-an-index-*").indicesOptions(options);
        ClusterState state = mockEmptyMetaData();
        RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()}, null);
        final Authentication authentication = createAuthentication(new User("test user", "a_all"));
        roleMap.put("a_all", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        final IndexNotFoundException nfe = expectThrows(
            IndexNotFoundException.class,
            () -> authorize(authentication, GetIndexAction.NAME, request));
        assertThat(nfe.getIndex(), is(notNullValue()));
        assertThat(nfe.getIndex().getName(), is("not-an-index-*"));
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(GetIndexAction.NAME), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
        verify(clusterService).state();
        verify(state, times(1)).metaData();
    }

    public void testRunAsRequestWithNoRolesUser() throws IOException {
        final TransportRequest request = mock(TransportRequest.class);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        final Authentication authentication = createAuthentication(new User("run as me", null, new User("test user", "admin")));
        assertNotEquals(authentication.getUser().authenticatedUser(), authentication);
        assertThrowsAuthorizationExceptionRunAs(
            () -> authorize(authentication, "indices:a", request),
            "indices:a", "test user", "run as me"); // run as [run as me]
        verify(auditTrail).runAsDenied(eq(requestId), eq(authentication), eq("indices:a"), eq(request),
            authzInfoRoles(Role.EMPTY.names()));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testRunAsRequestWithoutLookedUpBy() throws IOException {
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        AuthenticateRequest request = new AuthenticateRequest("run as me");
        roleMap.put("superuser", ReservedRolesStore.SUPERUSER_ROLE_DESCRIPTOR);
        User user = new User("run as me", Strings.EMPTY_ARRAY, new User("test user", new String[]{"superuser"}));
        Authentication authentication = new Authentication(user, new RealmRef("foo", "bar", "baz"), null);
        authentication.writeToContext(threadContext);
        assertNotEquals(user.authenticatedUser(), user);
        assertThrowsAuthorizationExceptionRunAs(
            () -> authorize(authentication, AuthenticateAction.NAME, request),
            AuthenticateAction.NAME, "test user", "run as me"); // run as [run as me]
        verify(auditTrail).runAsDenied(eq(requestId), eq(authentication), eq(AuthenticateAction.NAME), eq(request),
            authzInfoRoles(new String[] { ReservedRolesStore.SUPERUSER_ROLE_DESCRIPTOR.getName() }));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testRunAsRequestRunningAsUnAllowedUser() throws IOException {
        TransportRequest request = mock(TransportRequest.class);
        User user = new User("run as me", new String[]{"doesn't exist"}, new User("test user", "can run as"));
        assertNotEquals(user.authenticatedUser(), user);
        final Authentication authentication = createAuthentication(user);
        final RoleDescriptor role = new RoleDescriptor("can run as", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()},
            new String[]{"not the right user"});
        roleMap.put("can run as", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        assertThrowsAuthorizationExceptionRunAs(
            () -> authorize(authentication, "indices:a", request),
            "indices:a", "test user", "run as me");
        verify(auditTrail).runAsDenied(eq(requestId), eq(authentication), eq("indices:a"), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testRunAsRequestWithRunAsUserWithoutPermission() throws IOException {
        TransportRequest request = new GetIndexRequest().indices("a");
        User authenticatedUser = new User("test user", "can run as");
        User user = new User("run as me", new String[]{"b"}, authenticatedUser);
        assertNotEquals(user.authenticatedUser(), user);
        final Authentication authentication = createAuthentication(user);
        final RoleDescriptor runAsRole = new RoleDescriptor("can run as", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()},
            new String[]{"run as me"});
        roleMap.put("can run as", runAsRole);

        RoleDescriptor bRole = new RoleDescriptor("b", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("b").privileges("all").build()}, null);
        boolean indexExists = randomBoolean();
        if (indexExists) {
            ClusterState state = mock(ClusterState.class);
            when(clusterService.state()).thenReturn(state);
            when(state.metaData()).thenReturn(MetaData.builder()
                .put(new IndexMetaData.Builder("a")
                    .settings(Settings.builder().put("index.version.created", Version.CURRENT).build())
                    .numberOfShards(1).numberOfReplicas(0).build(), true)
                .build());
            roleMap.put("b", bRole);
        } else {
            mockEmptyMetaData();
        }
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        assertThrowsAuthorizationExceptionRunAs(
            () -> authorize(authentication, "indices:a", request),
            "indices:a", "test user", "run as me");
        verify(auditTrail).runAsGranted(eq(requestId), eq(authentication), eq("indices:a"), eq(request),
            authzInfoRoles(new String[]{runAsRole.getName()}));
        if (indexExists) {
            verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq("indices:a"), eq(request),
                authzInfoRoles(new String[]{bRole.getName()}));
        } else {
            verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq("indices:a"), eq(request),
                authzInfoRoles(Role.EMPTY.names()));
        }
        verifyNoMoreInteractions(auditTrail);
    }

    public void testRunAsRequestWithValidPermissions() throws IOException {
        TransportRequest request = new GetIndexRequest().indices("b");
        User authenticatedUser = new User("test user", new String[]{"can run as"});
        User user = new User("run as me", new String[]{"b"}, authenticatedUser);
        assertNotEquals(user.authenticatedUser(), user);
        final Authentication authentication = createAuthentication(user);
        final RoleDescriptor runAsRole = new RoleDescriptor("can run as", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()},
            new String[]{"run as me"});
        roleMap.put("can run as", runAsRole);
        ClusterState state = mock(ClusterState.class);
        when(clusterService.state()).thenReturn(state);
        when(state.metaData()).thenReturn(MetaData.builder()
            .put(new IndexMetaData.Builder("b")
                .settings(Settings.builder().put("index.version.created", Version.CURRENT).build())
                .numberOfShards(1).numberOfReplicas(0).build(), true)
            .build());
        RoleDescriptor bRole = new RoleDescriptor("b", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("b").privileges("all").build()}, null);
        roleMap.put("b", bRole);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        authorize(authentication, "indices:a", request);
        verify(auditTrail).runAsGranted(eq(requestId), eq(authentication), eq("indices:a"), eq(request),
            authzInfoRoles(new String[]{runAsRole.getName()}));
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq("indices:a"), eq(request),
            authzInfoRoles(new String[]{bRole.getName()}));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testGrantAllRestrictedUserCannotExecuteOperationAgainstSecurityIndices() throws IOException {
        RoleDescriptor role = new RoleDescriptor("all access", new String[]{"all"},
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("*").privileges("all").build()}, null);
        final Authentication authentication = createAuthentication(new User("all_access_user", "all_access"));
        roleMap.put("all_access", role);
        ClusterState state = mock(ClusterState.class);
        when(clusterService.state()).thenReturn(state);
        when(state.metaData()).thenReturn(MetaData.builder()
            .put(new IndexMetaData.Builder(INTERNAL_SECURITY_MAIN_INDEX_7)
                    .putAlias(new AliasMetaData.Builder(SECURITY_MAIN_ALIAS).build())
                    .settings(Settings.builder().put("index.version.created", Version.CURRENT).build())
                    .numberOfShards(1)
                    .numberOfReplicas(0)
                    .build(),true)
            .build());
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        List<Tuple<String, TransportRequest>> requests = new ArrayList<>();
        requests.add(new Tuple<>(BulkAction.NAME + "[s]",
                new DeleteRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), "id")));
        requests.add(
                new Tuple<>(UpdateAction.NAME, new UpdateRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), "id")));
        requests.add(
                new Tuple<>(BulkAction.NAME + "[s]", new IndexRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(SearchAction.NAME, new SearchRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(TermVectorsAction.NAME,
                new TermVectorsRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), "type", "id")));
        requests.add(new Tuple<>(GetAction.NAME, new GetRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), "id")));
        requests.add(new Tuple<>(IndicesAliasesAction.NAME, new IndicesAliasesRequest()
                .addAliasAction(AliasActions.add().alias("security_alias").index(INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(UpdateSettingsAction.NAME,
                new UpdateSettingsRequest().indices(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));
        // cannot execute monitor operations
        requests.add(new Tuple<>(IndicesStatsAction.NAME,
                new IndicesStatsRequest().indices(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(RecoveryAction.NAME,
                new RecoveryRequest().indices(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(IndicesSegmentsAction.NAME,
                new IndicesSegmentsRequest().indices(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(GetSettingsAction.NAME,
                new GetSettingsRequest().indices(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(IndicesShardStoresAction.NAME,
                new IndicesShardStoresRequest().indices(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(UpgradeStatusAction.NAME,
                new UpgradeStatusRequest().indices(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));

        for (Tuple<String, TransportRequest> requestTuple : requests) {
            String action = requestTuple.v1();
            TransportRequest request = requestTuple.v2();
            assertThrowsAuthorizationException(
                () -> authorize(authentication, action, request),
                action, "all_access_user");
            verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(action), eq(request),
                authzInfoRoles(new String[]{role.getName()}));
            verifyNoMoreInteractions(auditTrail);
        }

        // we should allow waiting for the health of the index or any index if the user has this permission
        ClusterHealthRequest request = new ClusterHealthRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7));
        authorize(authentication, ClusterHealthAction.NAME, request);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(ClusterHealthAction.NAME), eq(request),
            authzInfoRoles(new String[]{role.getName()}));

        // multiple indices
        request = new ClusterHealthRequest(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7, "foo", "bar");
        authorize(authentication, ClusterHealthAction.NAME, request);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(ClusterHealthAction.NAME), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);

        final SearchRequest searchRequest = new SearchRequest("_all");
        authorize(authentication, SearchAction.NAME, searchRequest);
        assertEquals(2, searchRequest.indices().length);
        assertEquals(IndicesAndAliasesResolver.NO_INDICES_OR_ALIASES_LIST, Arrays.asList(searchRequest.indices()));
    }

    public void testMonitoringOperationsAgainstSecurityIndexRequireAllowRestricted() throws IOException {
        final RoleDescriptor restrictedMonitorRole = new RoleDescriptor("restricted_monitor", null,
                new IndicesPrivileges[] { IndicesPrivileges.builder().indices("*").privileges("monitor").build() }, null);
        final RoleDescriptor unrestrictedMonitorRole = new RoleDescriptor("unrestricted_monitor", null, new IndicesPrivileges[] {
                IndicesPrivileges.builder().indices("*").privileges("monitor").allowRestrictedIndices(true).build() }, null);
        roleMap.put("restricted_monitor", restrictedMonitorRole);
        roleMap.put("unrestricted_monitor", unrestrictedMonitorRole);
        ClusterState state = mock(ClusterState.class);
        when(clusterService.state()).thenReturn(state);
        when(state.metaData()).thenReturn(MetaData.builder()
            .put(new IndexMetaData.Builder(INTERNAL_SECURITY_MAIN_INDEX_7)
                    .putAlias(new AliasMetaData.Builder(SECURITY_MAIN_ALIAS).build())
                    .settings(Settings.builder().put("index.version.created", Version.CURRENT).build())
                    .numberOfShards(1)
                    .numberOfReplicas(0)
                    .build(), true)
            .build());

        List<Tuple<String, ? extends TransportRequest>> requests = new ArrayList<>();
        requests.add(new Tuple<>(IndicesStatsAction.NAME, new IndicesStatsRequest().indices(SECURITY_MAIN_ALIAS)));
        requests.add(new Tuple<>(RecoveryAction.NAME, new RecoveryRequest().indices(SECURITY_MAIN_ALIAS)));
        requests.add(new Tuple<>(IndicesSegmentsAction.NAME, new IndicesSegmentsRequest().indices(SECURITY_MAIN_ALIAS)));
        requests.add(new Tuple<>(GetSettingsAction.NAME, new GetSettingsRequest().indices(SECURITY_MAIN_ALIAS)));
        requests.add(new Tuple<>(IndicesShardStoresAction.NAME, new IndicesShardStoresRequest().indices(SECURITY_MAIN_ALIAS)));
        requests.add(new Tuple<>(UpgradeStatusAction.NAME, new UpgradeStatusRequest().indices(SECURITY_MAIN_ALIAS)));

        for (final Tuple<String, ? extends TransportRequest> requestTuple : requests) {
            final String action = requestTuple.v1();
            final TransportRequest request = requestTuple.v2();
            try (StoredContext ignore = threadContext.stashContext()) {
                final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
                final Authentication restrictedUserAuthn = createAuthentication(new User("restricted_user", "restricted_monitor"));
                assertThrowsAuthorizationException(() -> authorize(restrictedUserAuthn, action, request), action, "restricted_user");
                verify(auditTrail).accessDenied(eq(requestId), eq(restrictedUserAuthn), eq(action), eq(request),
                    authzInfoRoles(new String[] { "restricted_monitor" }));
                verifyNoMoreInteractions(auditTrail);
            }
            try (StoredContext ignore = threadContext.stashContext()) {
                final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
                final Authentication unrestrictedUserAuthn = createAuthentication(new User("unrestricted_user", "unrestricted_monitor"));
                authorize(unrestrictedUserAuthn, action, request);
                verify(auditTrail).accessGranted(eq(requestId), eq(unrestrictedUserAuthn), eq(action), eq(request),
                    authzInfoRoles(new String[] { "unrestricted_monitor" }));
                verifyNoMoreInteractions(auditTrail);
            }
        }
    }

    public void testSuperusersCanExecuteOperationAgainstSecurityIndex() throws IOException {
        final User superuser = new User("custom_admin", ReservedRolesStore.SUPERUSER_ROLE_DESCRIPTOR.getName());
        roleMap.put(ReservedRolesStore.SUPERUSER_ROLE_DESCRIPTOR.getName(), ReservedRolesStore.SUPERUSER_ROLE_DESCRIPTOR);
        ClusterState state = mock(ClusterState.class);
        when(clusterService.state()).thenReturn(state);
        when(state.metaData()).thenReturn(MetaData.builder()
            .put(new IndexMetaData.Builder(INTERNAL_SECURITY_MAIN_INDEX_7)
                    .putAlias(new AliasMetaData.Builder(SECURITY_MAIN_ALIAS).build())
                    .settings(Settings.builder().put("index.version.created", Version.CURRENT).build())
                    .numberOfShards(1)
                    .numberOfReplicas(0)
                    .build(), true)
            .build());
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        List<Tuple<String, TransportRequest>> requests = new ArrayList<>();
        requests.add(
                new Tuple<>(DeleteAction.NAME, new DeleteRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), "id")));
        requests.add(new Tuple<>(BulkAction.NAME + "[s]",
                createBulkShardRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), DeleteRequest::new)));
        requests.add(
                new Tuple<>(UpdateAction.NAME, new UpdateRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), "id")));
        requests.add(new Tuple<>(IndexAction.NAME, new IndexRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(BulkAction.NAME + "[s]",
                createBulkShardRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), IndexRequest::new)));
        requests.add(new Tuple<>(SearchAction.NAME, new SearchRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(TermVectorsAction.NAME,
                new TermVectorsRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), "type", "id")));
        requests.add(
                new Tuple<>(GetAction.NAME, new GetRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), "type", "id")));
        requests.add(new Tuple<>(TermVectorsAction.NAME,
                new TermVectorsRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), "type", "id")));
        requests.add(new Tuple<>(IndicesAliasesAction.NAME, new IndicesAliasesRequest()
                .addAliasAction(AliasActions.add().alias("security_alias").index(INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(ClusterHealthAction.NAME,
                new ClusterHealthRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7))));
        requests.add(new Tuple<>(ClusterHealthAction.NAME,
                new ClusterHealthRequest(randomFrom(SECURITY_MAIN_ALIAS, INTERNAL_SECURITY_MAIN_INDEX_7), "foo", "bar")));

        for (final Tuple<String, TransportRequest> requestTuple : requests) {
            final String action = requestTuple.v1();
            final TransportRequest request = requestTuple.v2();
            try (ThreadContext.StoredContext ignore = threadContext.newStoredContext(false)) {
                final Authentication authentication = createAuthentication(superuser);
                authorize(authentication, action, request);
                verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(action), eq(request),
                    authzInfoRoles(superuser.roles()));
            }
        }
    }

    public void testSuperusersCanExecuteOperationAgainstSecurityIndexWithWildcard() throws IOException {
        final User superuser = new User("custom_admin", ReservedRolesStore.SUPERUSER_ROLE_DESCRIPTOR.getName());
        final Authentication authentication = createAuthentication(superuser);
        roleMap.put(ReservedRolesStore.SUPERUSER_ROLE_DESCRIPTOR.getName(), ReservedRolesStore.SUPERUSER_ROLE_DESCRIPTOR);
        ClusterState state = mock(ClusterState.class);
        when(clusterService.state()).thenReturn(state);
        when(state.metaData()).thenReturn(MetaData.builder()
            .put(new IndexMetaData.Builder(INTERNAL_SECURITY_MAIN_INDEX_7)
                    .putAlias(new AliasMetaData.Builder(SECURITY_MAIN_ALIAS).build())
                    .settings(Settings.builder().put("index.version.created", Version.CURRENT).build())
                    .numberOfShards(1)
                    .numberOfReplicas(0)
                    .build(), true)
            .build());
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        String action = SearchAction.NAME;
        SearchRequest request = new SearchRequest("_all");
        authorize(authentication, action, request);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(action), eq(request), authzInfoRoles(superuser.roles()));
        assertThat(request.indices(), arrayContainingInAnyOrder(INTERNAL_SECURITY_MAIN_INDEX_7, SECURITY_MAIN_ALIAS));
    }

    public void testCompositeActionsAreImmediatelyRejected() {
        //if the user has no permission for composite actions against any index, the request fails straight-away in the main action
        final Tuple<String, TransportRequest> compositeRequest = randomCompositeRequest();
        final String action = compositeRequest.v1();
        final TransportRequest request = compositeRequest.v2();
        final Authentication authentication = createAuthentication(new User("test user", "no_indices"));
        final RoleDescriptor role = new RoleDescriptor("no_indices", null, null, null);
        roleMap.put("no_indices", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        assertThrowsAuthorizationException(
            () -> authorize(authentication, action, request), action, "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(action), eq(request),
            authzInfoRoles(new String[] { role.getName() }));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testCompositeActionsIndicesAreNotChecked() throws IOException {
        //if the user has permission for some index, the request goes through without looking at the indices, they will be checked later
        final Tuple<String, TransportRequest> compositeRequest = randomCompositeRequest();
        final String action = compositeRequest.v1();
        final TransportRequest request = compositeRequest.v2();
        final Authentication authentication = createAuthentication(new User("test user", "role"));
        final RoleDescriptor role = new RoleDescriptor("role", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices(randomBoolean() ? "a" : "index").privileges("all").build()},
            null);
        roleMap.put("role", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        authorize(authentication, action, request);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(action), eq(request),
            authzInfoRoles(new String[] { role.getName() }));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testCompositeActionsMustImplementCompositeIndicesRequest() throws IOException {
        String action = randomCompositeRequest().v1();
        TransportRequest request = mock(TransportRequest.class);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        User user = new User("test user", "role");
        roleMap.put("role", new RoleDescriptor("role", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices(randomBoolean() ? "a" : "index").privileges("all").build()},
            null));
        IllegalStateException illegalStateException = expectThrows(IllegalStateException.class,
            () -> authorize(createAuthentication(user), action, request));
        assertThat(illegalStateException.getMessage(), containsString("Composite and bulk actions must implement CompositeIndicesRequest"));
    }

    public void testCompositeActionsIndicesAreCheckedAtTheShardLevel() throws IOException {
        final MockIndicesRequest mockRequest = new MockIndicesRequest(IndicesOptions.strictExpandOpen(), "index");
        final TransportRequest request;
        final String action;
        switch (randomIntBetween(0, 4)) {
            case 0:
                action = MultiGetAction.NAME + "[shard]";
                request = mockRequest;
                break;
            case 1:
                //reindex, msearch, search template, and multi search template delegate to search
                action = SearchAction.NAME;
                request = mockRequest;
                break;
            case 2:
                action = MultiTermVectorsAction.NAME + "[shard]";
                request = mockRequest;
                break;
            case 3:
                action = BulkAction.NAME + "[s]";
                request = createBulkShardRequest("index", IndexRequest::new);
                break;
            case 4:
                action = "indices:data/read/mpercolate[s]";
                request = mockRequest;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        logger.info("--> action: {}", action);

        User userAllowed = new User("userAllowed", "roleAllowed");
        roleMap.put("roleAllowed", new RoleDescriptor("roleAllowed", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("index").privileges("all").build()}, null));
        User userDenied = new User("userDenied", "roleDenied");
        roleMap.put("roleDenied", new RoleDescriptor("roleDenied", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()}, null));
        AuditUtil.getOrGenerateRequestId(threadContext);
        mockEmptyMetaData();
        try (ThreadContext.StoredContext ignore = threadContext.newStoredContext(false)) {
            authorize(createAuthentication(userAllowed), action, request);
        }
        assertThrowsAuthorizationException(
            () -> authorize(createAuthentication(userDenied), action, request), action, "userDenied");
    }

    public void testAuthorizationOfIndividualBulkItems() throws IOException {
        final String action = BulkAction.NAME + "[s]";
        final BulkItemRequest[] items = {
            new BulkItemRequest(1, new DeleteRequest("concrete-index", "doc", "c1")),
            new BulkItemRequest(2, new IndexRequest("concrete-index", "doc", "c2")),
            new BulkItemRequest(3, new DeleteRequest("alias-1", "doc", "a1a")),
            new BulkItemRequest(4, new IndexRequest("alias-1", "doc", "a1b")),
            new BulkItemRequest(5, new DeleteRequest("alias-2", "doc", "a2a")),
            new BulkItemRequest(6, new IndexRequest("alias-2", "doc", "a2b"))
        };
        final ShardId shardId = new ShardId("concrete-index", UUID.randomUUID().toString(), 1);
        final TransportRequest request = new BulkShardRequest(shardId, WriteRequest.RefreshPolicy.IMMEDIATE, items);

        final Authentication authentication = createAuthentication(new User("user", "my-role"));
        RoleDescriptor role = new RoleDescriptor("my-role", null, new IndicesPrivileges[]{
            IndicesPrivileges.builder().indices("concrete-index").privileges("all").build(),
            IndicesPrivileges.builder().indices("alias-1").privileges("index").build(),
            IndicesPrivileges.builder().indices("alias-2").privileges("delete").build()
        }, null);
        roleMap.put("my-role", role);

        mockEmptyMetaData();
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        authorize(authentication, action, request);

        verify(auditTrail).explicitIndexAccessEvent(eq(requestId), eq(AuditLevel.ACCESS_GRANTED), eq(authentication),
                eq(DeleteAction.NAME), eq("concrete-index"), eq(BulkItemRequest.class.getSimpleName()),
                eq(request.remoteAddress()), authzInfoRoles(new String[] { role.getName() }));
        verify(auditTrail).explicitIndexAccessEvent(eq(requestId), eq(AuditLevel.ACCESS_GRANTED), eq(authentication),
                eq(DeleteAction.NAME), eq("alias-2"), eq(BulkItemRequest.class.getSimpleName()),
                eq(request.remoteAddress()), authzInfoRoles(new String[] { role.getName() }));
        verify(auditTrail).explicitIndexAccessEvent(eq(requestId), eq(AuditLevel.ACCESS_GRANTED), eq(authentication),
                eq(IndexAction.NAME + ":op_type/index"), eq("concrete-index"), eq(BulkItemRequest.class.getSimpleName()),
                eq(request.remoteAddress()), authzInfoRoles(new String[] { role.getName() }));
        verify(auditTrail).explicitIndexAccessEvent(eq(requestId), eq(AuditLevel.ACCESS_GRANTED), eq(authentication),
                eq(IndexAction.NAME + ":op_type/index"), eq("alias-1"), eq(BulkItemRequest.class.getSimpleName()),
                eq(request.remoteAddress()), authzInfoRoles(new String[] { role.getName() }));
        verify(auditTrail).explicitIndexAccessEvent(eq(requestId), eq(AuditLevel.ACCESS_DENIED), eq(authentication),
                eq(DeleteAction.NAME), eq("alias-1"), eq(BulkItemRequest.class.getSimpleName()),
                eq(request.remoteAddress()), authzInfoRoles(new String[] { role.getName() }));
        verify(auditTrail).explicitIndexAccessEvent(eq(requestId), eq(AuditLevel.ACCESS_DENIED), eq(authentication),
                eq(IndexAction.NAME + ":op_type/index"), eq("alias-2"), eq(BulkItemRequest.class.getSimpleName()),
                eq(request.remoteAddress()), authzInfoRoles(new String[] { role.getName() }));
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(action), eq(request),
            authzInfoRoles(new String[] { role.getName() })); // bulk request is allowed
        verifyNoMoreInteractions(auditTrail);
    }

    public void testAuthorizationOfIndividualBulkItemsWithDateMath() throws IOException {
        final String action = BulkAction.NAME + "[s]";
        final BulkItemRequest[] items = {
            new BulkItemRequest(1, new IndexRequest("<datemath-{now/M{YYYY}}>", "doc", "dy1")),
            new BulkItemRequest(2,
                new DeleteRequest("<datemath-{now/d{YYYY}}>", "doc", "dy2")), // resolves to same as above
            new BulkItemRequest(3, new IndexRequest("<datemath-{now/M{YYYY.MM}}>", "doc", "dm1")),
            new BulkItemRequest(4,
                new DeleteRequest("<datemath-{now/d{YYYY.MM}}>", "doc", "dm2")), // resolves to same as above
        };
        final ShardId shardId = new ShardId("concrete-index", UUID.randomUUID().toString(), 1);
        final TransportRequest request = new BulkShardRequest(shardId, WriteRequest.RefreshPolicy.IMMEDIATE, items);

        final Authentication authentication = createAuthentication(new User("user", "my-role"));
        final RoleDescriptor role = new RoleDescriptor("my-role", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("datemath-*").privileges("index").build()}, null);
        roleMap.put("my-role", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        mockEmptyMetaData();
        authorize(authentication, action, request);

        // both deletes should fail
        verify(auditTrail, times(2)).explicitIndexAccessEvent(eq(requestId), eq(AuditLevel.ACCESS_DENIED), eq(authentication),
                eq(DeleteAction.NAME), Matchers.startsWith("datemath-"), eq(BulkItemRequest.class.getSimpleName()),
                eq(request.remoteAddress()), authzInfoRoles(new String[] { role.getName() }));
        verify(auditTrail, times(2)).explicitIndexAccessEvent(eq(requestId), eq(AuditLevel.ACCESS_GRANTED), eq(authentication),
                eq(IndexAction.NAME + ":op_type/index"), Matchers.startsWith("datemath-"), eq(BulkItemRequest.class.getSimpleName()),
                eq(request.remoteAddress()), authzInfoRoles(new String[] { role.getName() }));
        // bulk request is allowed
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(action), eq(request),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
    }

    private BulkShardRequest createBulkShardRequest(String indexName, TriFunction<String, String, String, DocWriteRequest<?>> req) {
        final BulkItemRequest[] items = {new BulkItemRequest(1, req.apply(indexName, "type", "id"))};
        return new BulkShardRequest(new ShardId(indexName, UUID.randomUUID().toString(), 1),
            WriteRequest.RefreshPolicy.IMMEDIATE, items);
    }

    private static Tuple<String, TransportRequest> randomCompositeRequest() {
        switch (randomIntBetween(0, 7)) {
            case 0:
                return Tuple.tuple(MultiGetAction.NAME, new MultiGetRequest().add("index", "type", "id"));
            case 1:
                return Tuple.tuple(MultiSearchAction.NAME, new MultiSearchRequest().add(new SearchRequest()));
            case 2:
                return Tuple.tuple(MultiTermVectorsAction.NAME, new MultiTermVectorsRequest().add("index", "type", "id"));
            case 3:
                return Tuple.tuple(BulkAction.NAME, new BulkRequest().add(new DeleteRequest("index", "type", "id")));
            case 4:
                return Tuple.tuple("indices:data/read/mpercolate", new MockCompositeIndicesRequest());
            case 5:
                return Tuple.tuple("indices:data/read/msearch/template", new MockCompositeIndicesRequest());
            case 6:
                return Tuple.tuple("indices:data/read/search/template", new MockCompositeIndicesRequest());
            case 7:
                return Tuple.tuple("indices:data/write/reindex", new MockCompositeIndicesRequest());
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static class MockCompositeIndicesRequest extends TransportRequest implements CompositeIndicesRequest {
    }

    private Authentication createAuthentication(User user) {
        RealmRef lookedUpBy = user.authenticatedUser() == user ? null : new RealmRef("looked", "up", "by");
        Authentication authentication = new Authentication(user, new RealmRef("test", "test", "foo"), lookedUpBy);
        try {
            authentication.writeToContext(threadContext);
        } catch (IOException e) {
            throw new UncheckedIOException("caught unexpected IOException", e);
        }
        return authentication;
    }

    private ClusterState mockEmptyMetaData() {
        ClusterState state = mock(ClusterState.class);
        when(clusterService.state()).thenReturn(state);
        when(state.metaData()).thenReturn(MetaData.EMPTY_META_DATA);
        return state;
    }

    public void testProxyRequestFailsOnNonProxyAction() {
        TransportRequest request = TransportRequest.Empty.INSTANCE;
        DiscoveryNode node = new DiscoveryNode("foo", buildNewFakeTransportAddress(), Version.CURRENT);
        TransportRequest transportRequest = TransportActionProxy.wrapRequest(node, request);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        User user = new User("test user", "role");
        ElasticsearchSecurityException ese = expectThrows(ElasticsearchSecurityException.class,
            () -> authorize(createAuthentication(user), "indices:some/action", transportRequest));
        assertThat(ese.getCause(), instanceOf(IllegalStateException.class));
        IllegalStateException illegalStateException = (IllegalStateException) ese.getCause();
        assertThat(illegalStateException.getMessage(),
            startsWith("originalRequest is a proxy request for: [org.elasticsearch.transport.TransportRequest$"));
        assertThat(illegalStateException.getMessage(), endsWith("] but action: [indices:some/action] isn't"));
    }

    public void testProxyRequestFailsOnNonProxyRequest() {
        TransportRequest request = TransportRequest.Empty.INSTANCE;
        User user = new User("test user", "role");
        AuditUtil.getOrGenerateRequestId(threadContext);
        ElasticsearchSecurityException ese = expectThrows(ElasticsearchSecurityException.class,
            () -> authorize(createAuthentication(user), TransportActionProxy.getProxyAction("indices:some/action"), request));
        assertThat(ese.getCause(), instanceOf(IllegalStateException.class));
        IllegalStateException illegalStateException = (IllegalStateException) ese.getCause();
        assertThat(illegalStateException.getMessage(),
            startsWith("originalRequest is not a proxy request: [org.elasticsearch.transport.TransportRequest$"));
        assertThat(illegalStateException.getMessage(),
            endsWith("] but action: [internal:transport/proxy/indices:some/action] is a proxy action"));
    }

    public void testProxyRequestAuthenticationDenied() throws IOException {
        final TransportRequest proxiedRequest = new SearchRequest();
        final DiscoveryNode node = new DiscoveryNode("foo", buildNewFakeTransportAddress(), Version.CURRENT);
        final TransportRequest transportRequest = TransportActionProxy.wrapRequest(node, proxiedRequest);
        final String action = TransportActionProxy.getProxyAction(SearchTransportService.QUERY_ACTION_NAME);
        final Authentication authentication = createAuthentication(new User("test user", "no_indices"));
        final RoleDescriptor role = new RoleDescriptor("no_indices", null, null, null);
        roleMap.put("no_indices", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        assertThrowsAuthorizationException(
            () -> authorize(authentication, action, transportRequest), action, "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(action), eq(proxiedRequest),
            authzInfoRoles(new String[]{role.getName()}));
        verifyNoMoreInteractions(auditTrail);
    }

    public void testProxyRequestAuthenticationGrantedWithAllPrivileges() {
        RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("all").build()}, null);
        final Authentication authentication = createAuthentication(new User("test user", "a_all"));
        roleMap.put("a_all", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        mockEmptyMetaData();
        DiscoveryNode node = new DiscoveryNode("foo", buildNewFakeTransportAddress(), Version.CURRENT);

        final ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        final TransportRequest transportRequest = TransportActionProxy.wrapRequest(node, clearScrollRequest);
        final String action = TransportActionProxy.getProxyAction(SearchTransportService.CLEAR_SCROLL_CONTEXTS_ACTION_NAME);
        authorize(authentication, action, transportRequest);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(action), eq(clearScrollRequest),
            authzInfoRoles(new String[]{role.getName()}));
    }

    public void testProxyRequestAuthenticationGranted() {
        RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("read_cross_cluster").build()}, null);
        final Authentication authentication = createAuthentication(new User("test user", "a_all"));
        roleMap.put("a_all", role);
        mockEmptyMetaData();
        DiscoveryNode node = new DiscoveryNode("foo", buildNewFakeTransportAddress(), Version.CURRENT);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);

        final ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        final TransportRequest transportRequest = TransportActionProxy.wrapRequest(node, clearScrollRequest);
        final String action = TransportActionProxy.getProxyAction(SearchTransportService.CLEAR_SCROLL_CONTEXTS_ACTION_NAME);
        authorize(authentication, action, transportRequest);
        verify(auditTrail).accessGranted(eq(requestId), eq(authentication), eq(action), eq(clearScrollRequest),
            authzInfoRoles(new String[]{role.getName()}));
    }

    public void testProxyRequestAuthenticationDeniedWithReadPrivileges() throws IOException {
        final Authentication authentication = createAuthentication(new User("test user", "a_all"));
        final RoleDescriptor role = new RoleDescriptor("a_all", null,
            new IndicesPrivileges[]{IndicesPrivileges.builder().indices("a").privileges("read").build()}, null);
        roleMap.put("a_all", role);
        final String requestId = AuditUtil.getOrGenerateRequestId(threadContext);
        mockEmptyMetaData();
        DiscoveryNode node = new DiscoveryNode("foo", buildNewFakeTransportAddress(), Version.CURRENT);
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        TransportRequest transportRequest = TransportActionProxy.wrapRequest(node, clearScrollRequest);
        String action = TransportActionProxy.getProxyAction(SearchTransportService.CLEAR_SCROLL_CONTEXTS_ACTION_NAME);
        assertThrowsAuthorizationException(
            () -> authorize(authentication, action, transportRequest), action, "test user");
        verify(auditTrail).accessDenied(eq(requestId), eq(authentication), eq(action), eq(clearScrollRequest),
            authzInfoRoles(new String[]{role.getName()}));
    }

    public void testAuthorizationEngineSelection() {
        final AuthorizationEngine engine = new AuthorizationEngine() {
            @Override
            public void resolveAuthorizationInfo(RequestInfo requestInfo, ActionListener<AuthorizationInfo> listener) {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public void authorizeRunAs(RequestInfo requestInfo, AuthorizationInfo authorizationInfo,
                                       ActionListener<AuthorizationResult> listener) {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public void authorizeClusterAction(RequestInfo requestInfo, AuthorizationInfo authorizationInfo,
                                               ActionListener<AuthorizationResult> listener) {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public void authorizeIndexAction(RequestInfo requestInfo, AuthorizationInfo authorizationInfo,
                                             AsyncSupplier<ResolvedIndices> indicesAsyncSupplier,
                                             Map<String, AliasOrIndex> aliasOrIndexLookup,
                                             ActionListener<IndexAuthorizationResult> listener) {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public void loadAuthorizedIndices(RequestInfo requestInfo, AuthorizationInfo authorizationInfo,
                                              Map<String, AliasOrIndex> aliasOrIndexLookup, ActionListener<List<String>> listener) {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public void validateIndexPermissionsAreSubset(RequestInfo requestInfo, AuthorizationInfo authorizationInfo,
                                                          Map<String, List<String>> indexNameToNewNames,
                                                          ActionListener<AuthorizationResult> listener) {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public void checkPrivileges(Authentication authentication, AuthorizationInfo authorizationInfo,
                                        HasPrivilegesRequest hasPrivilegesRequest,
                                        Collection<ApplicationPrivilegeDescriptor> applicationPrivilegeDescriptors,
                                        ActionListener<HasPrivilegesResponse> listener) {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public void getUserPrivileges(Authentication authentication, AuthorizationInfo authorizationInfo,
                                          GetUserPrivilegesRequest request, ActionListener<GetUserPrivilegesResponse> listener) {
                throw new UnsupportedOperationException("not implemented");
            }
        };

        XPackLicenseState licenseState = mock(XPackLicenseState.class);
        when(licenseState.isAuthorizationEngineAllowed()).thenReturn(true);
        authorizationService = new AuthorizationService(Settings.EMPTY, rolesStore, clusterService,
            auditTrail, new DefaultAuthenticationFailureHandler(Collections.emptyMap()), threadPool, new AnonymousUser(Settings.EMPTY),
            engine, Collections.emptySet(), licenseState);
        Authentication authentication;
        try (ThreadContext.StoredContext ignore = threadContext.stashContext()) {
            authentication = createAuthentication(new User("test user", "a_all"));
            assertEquals(engine, authorizationService.getAuthorizationEngine(authentication));
            when(licenseState.isAuthorizationEngineAllowed()).thenReturn(false);
            assertThat(authorizationService.getAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
        }

        when(licenseState.isAuthorizationEngineAllowed()).thenReturn(true);
        try (ThreadContext.StoredContext ignore = threadContext.stashContext()) {
            authentication = createAuthentication(new User("runas", new String[]{"runas_role"}, new User("runner", "runner_role")));
            assertEquals(engine, authorizationService.getAuthorizationEngine(authentication));
            assertEquals(engine, authorizationService.getRunAsAuthorizationEngine(authentication));
            when(licenseState.isAuthorizationEngineAllowed()).thenReturn(false);
            assertThat(authorizationService.getAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
            assertThat(authorizationService.getRunAsAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
        }

        when(licenseState.isAuthorizationEngineAllowed()).thenReturn(true);
        try (ThreadContext.StoredContext ignore = threadContext.stashContext()) {
            authentication = createAuthentication(new User("runas", new String[]{"runas_role"}, new ElasticUser(true)));
            assertEquals(engine, authorizationService.getAuthorizationEngine(authentication));
            assertNotEquals(engine, authorizationService.getRunAsAuthorizationEngine(authentication));
            assertThat(authorizationService.getRunAsAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
            when(licenseState.isAuthorizationEngineAllowed()).thenReturn(false);
            assertThat(authorizationService.getAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
            assertThat(authorizationService.getRunAsAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
        }

        when(licenseState.isAuthorizationEngineAllowed()).thenReturn(true);
        try (ThreadContext.StoredContext ignore = threadContext.stashContext()) {
            authentication = createAuthentication(new User("elastic", new String[]{"superuser"}, new User("runner", "runner_role")));
            assertNotEquals(engine, authorizationService.getAuthorizationEngine(authentication));
            assertThat(authorizationService.getAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
            assertEquals(engine, authorizationService.getRunAsAuthorizationEngine(authentication));
            when(licenseState.isAuthorizationEngineAllowed()).thenReturn(false);
            assertThat(authorizationService.getAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
            assertThat(authorizationService.getRunAsAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
        }

        when(licenseState.isAuthorizationEngineAllowed()).thenReturn(true);
        try (ThreadContext.StoredContext ignore = threadContext.stashContext()) {
            authentication = createAuthentication(new User("kibana", new String[]{"kibana_system"}, new ElasticUser(true)));
            assertNotEquals(engine, authorizationService.getAuthorizationEngine(authentication));
            assertThat(authorizationService.getAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
            assertNotEquals(engine, authorizationService.getRunAsAuthorizationEngine(authentication));
            assertThat(authorizationService.getRunAsAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
            when(licenseState.isAuthorizationEngineAllowed()).thenReturn(false);
            assertThat(authorizationService.getAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
            assertThat(authorizationService.getRunAsAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
        }

        when(licenseState.isAuthorizationEngineAllowed()).thenReturn(true);
        try (ThreadContext.StoredContext ignore = threadContext.stashContext()) {
            authentication = createAuthentication(randomFrom(XPackUser.INSTANCE, XPackSecurityUser.INSTANCE,
                new ElasticUser(true), new KibanaUser(true)));
            assertNotEquals(engine, authorizationService.getRunAsAuthorizationEngine(authentication));
            assertThat(authorizationService.getRunAsAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
            when(licenseState.isAuthorizationEngineAllowed()).thenReturn(false);
            assertThat(authorizationService.getAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
            assertThat(authorizationService.getRunAsAuthorizationEngine(authentication), instanceOf(RBACEngine.class));
        }
    }

    static AuthorizationInfo authzInfoRoles(String[] expectedRoles) {
        return Matchers.argThat(new RBACAuthorizationInfoRoleMatcher(expectedRoles));
    }

    private static class RBACAuthorizationInfoRoleMatcher extends ArgumentMatcher<AuthorizationInfo> {

        private final String[] wanted;

        RBACAuthorizationInfoRoleMatcher(String[] expectedRoles) {
            this.wanted = expectedRoles;
        }

        @Override
        public boolean matches(Object item) {
            if (item instanceof AuthorizationInfo) {
                final String[] found = (String[]) ((AuthorizationInfo) item).asMap().get(PRINCIPAL_ROLES_FIELD_NAME);
                return Arrays.equals(wanted, found);
            }
            return false;
        }
    }

    private abstract static class MockConfigurableClusterPrivilege implements ConfigurableClusterPrivilege {
        @Override
        public Category getCategory() {
            return Category.APPLICATION;
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            return builder;
        }

        @Override
        public String getWriteableName() {
            return "mock";
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
        }
    }
}
