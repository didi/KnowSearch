/*
 *
 *  Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 *  or more contributor license agreements. Licensed under the Elastic License;
 *  you may not use this file except in compliance with the Elastic License.
 *
 */

package org.elasticsearch.xpack.core.security.authz.privilege;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesAction;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotAction;
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsAction;
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotsStatusAction;
import org.elasticsearch.action.admin.cluster.state.ClusterStateAction;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.util.set.Sets;
import org.elasticsearch.xpack.core.ilm.action.GetLifecycleAction;
import org.elasticsearch.xpack.core.ilm.action.GetStatusAction;
import org.elasticsearch.xpack.core.ilm.action.StartILMAction;
import org.elasticsearch.xpack.core.ilm.action.StopILMAction;
import org.elasticsearch.xpack.core.security.action.DelegatePkiAuthenticationAction;
import org.elasticsearch.xpack.core.security.action.token.InvalidateTokenAction;
import org.elasticsearch.xpack.core.security.action.token.RefreshTokenAction;
import org.elasticsearch.xpack.core.security.action.user.HasPrivilegesAction;
import org.elasticsearch.xpack.core.slm.action.GetSnapshotLifecycleAction;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Translates cluster privilege names into concrete implementations
 */
public class ClusterPrivilegeResolver {
    private static final Logger logger = LogManager.getLogger(ClusterPrivilegeResolver.class);

    // shared automatons
    private static final Set<String> ALL_SECURITY_PATTERN = Collections.singleton("cluster:admin/xpack/security/*");
    private static final Set<String> MANAGE_SAML_PATTERN = Collections.unmodifiableSet(
        Sets.newHashSet("cluster:admin/xpack/security/saml/*",
            InvalidateTokenAction.NAME, RefreshTokenAction.NAME));
    private static final Set<String> MANAGE_OIDC_PATTERN = Collections.singleton("cluster:admin/xpack/security/oidc/*");
    private static final Set<String> MANAGE_TOKEN_PATTERN = Collections.singleton("cluster:admin/xpack/security/token/*");
    private static final Set<String> MANAGE_API_KEY_PATTERN = Collections.singleton("cluster:admin/xpack/security/api_key/*");
    private static final Set<String> MONITOR_PATTERN = Collections.singleton("cluster:monitor/*");
    private static final Set<String> MONITOR_TRANSFORM_PATTERN = Collections.unmodifiableSet(
        Sets.newHashSet("cluster:monitor/data_frame/*", "cluster:monitor/transform/*"));
    private static final Set<String> MONITOR_ML_PATTERN = Collections.singleton("cluster:monitor/xpack/ml/*");
    private static final Set<String> MONITOR_WATCHER_PATTERN = Collections.singleton("cluster:monitor/xpack/watcher/*");
    private static final Set<String> MONITOR_ROLLUP_PATTERN = Collections.singleton("cluster:monitor/xpack/rollup/*");
    private static final Set<String> ALL_CLUSTER_PATTERN = Collections.unmodifiableSet(
        Sets.newHashSet("cluster:*", "indices:admin/template/*"));
    private static final Set<String> MANAGE_ML_PATTERN = Collections.unmodifiableSet(
        Sets.newHashSet("cluster:admin/xpack/ml/*", "cluster:monitor/xpack/ml/*"));
    private static final Set<String> MANAGE_TRANSFORM_PATTERN = Collections.unmodifiableSet(
        Sets.newHashSet("cluster:admin/data_frame/*", "cluster:monitor/data_frame/*",
            "cluster:monitor/transform/*", "cluster:admin/transform/*"));
    private static final Set<String> MANAGE_WATCHER_PATTERN = Collections.unmodifiableSet(
        Sets.newHashSet("cluster:admin/xpack/watcher/*", "cluster:monitor/xpack/watcher/*"));
    private static final Set<String> TRANSPORT_CLIENT_PATTERN = Collections.unmodifiableSet(
        Sets.newHashSet("cluster:monitor/nodes/liveness", "cluster:monitor/state"));
    private static final Set<String> MANAGE_IDX_TEMPLATE_PATTERN = Collections.singleton("indices:admin/template/*");
    private static final Set<String> MANAGE_INGEST_PIPELINE_PATTERN = Collections.singleton("cluster:admin/ingest/pipeline/*");
    private static final Set<String> MANAGE_ROLLUP_PATTERN = Collections.unmodifiableSet(
        Sets.newHashSet("cluster:admin/xpack/rollup/*", "cluster:monitor/xpack/rollup/*"));
    private static final Set<String> MANAGE_CCR_PATTERN =
        Collections.unmodifiableSet(Sets.newHashSet("cluster:admin/xpack/ccr/*", ClusterStateAction.NAME, HasPrivilegesAction.NAME));
    private static final Set<String> CREATE_SNAPSHOT_PATTERN = Collections.unmodifiableSet(
        Sets.newHashSet(CreateSnapshotAction.NAME, SnapshotsStatusAction.NAME + "*",
            GetSnapshotsAction.NAME, SnapshotsStatusAction.NAME, GetRepositoriesAction.NAME));
    private static final Set<String> MONITOR_SNAPSHOT_PATTERN = Collections.unmodifiableSet(Sets.newHashSet(
          SnapshotsStatusAction.NAME + "*", GetSnapshotsAction.NAME, SnapshotsStatusAction.NAME, GetRepositoriesAction.NAME));
    private static final Set<String> READ_CCR_PATTERN = Collections.unmodifiableSet(Sets.newHashSet(ClusterStateAction.NAME,
        HasPrivilegesAction.NAME));
    private static final Set<String> MANAGE_ILM_PATTERN = Collections.singleton("cluster:admin/ilm/*");
    private static final Set<String> READ_ILM_PATTERN = Collections.unmodifiableSet(Sets.newHashSet(GetLifecycleAction.NAME,
        GetStatusAction.NAME));
    private static final Set<String> MANAGE_SLM_PATTERN =
        Collections.unmodifiableSet(Sets.newHashSet("cluster:admin/slm/*", StartILMAction.NAME, StopILMAction.NAME, GetStatusAction.NAME));
    private static final Set<String> READ_SLM_PATTERN = Collections.unmodifiableSet(Sets.newHashSet(GetSnapshotLifecycleAction.NAME,
        GetStatusAction.NAME));
    private static final Set<String> MANAGE_ENRICH_AUTOMATON = Collections.unmodifiableSet(Sets.newHashSet("cluster:admin/xpack/enrich/*"));

    public static final NamedClusterPrivilege NONE = new ActionClusterPrivilege("none", Collections.emptySet(), Collections.emptySet());
    public static final NamedClusterPrivilege ALL = new ActionClusterPrivilege("all", ALL_CLUSTER_PATTERN);
    public static final NamedClusterPrivilege MONITOR = new ActionClusterPrivilege("monitor", MONITOR_PATTERN);
    public static final NamedClusterPrivilege MONITOR_ML = new ActionClusterPrivilege("monitor_ml", MONITOR_ML_PATTERN);
    public static final NamedClusterPrivilege MONITOR_TRANSFORM_DEPRECATED =
        new ActionClusterPrivilege("monitor_data_frame_transforms", MONITOR_TRANSFORM_PATTERN);
    public static final NamedClusterPrivilege MONITOR_TRANSFORM =
            new ActionClusterPrivilege("monitor_transform", MONITOR_TRANSFORM_PATTERN);
    public static final NamedClusterPrivilege MONITOR_WATCHER = new ActionClusterPrivilege("monitor_watcher", MONITOR_WATCHER_PATTERN);
    public static final NamedClusterPrivilege MONITOR_ROLLUP = new ActionClusterPrivilege("monitor_rollup", MONITOR_ROLLUP_PATTERN);
    public static final NamedClusterPrivilege MANAGE = new ActionClusterPrivilege("manage", ALL_CLUSTER_PATTERN, ALL_SECURITY_PATTERN);
    public static final NamedClusterPrivilege MANAGE_ML = new ActionClusterPrivilege("manage_ml", MANAGE_ML_PATTERN);
    public static final NamedClusterPrivilege MANAGE_TRANSFORM_DEPRECATED =
        new ActionClusterPrivilege("manage_data_frame_transforms", MANAGE_TRANSFORM_PATTERN);
    public static final NamedClusterPrivilege MANAGE_TRANSFORM =
            new ActionClusterPrivilege("manage_transform", MANAGE_TRANSFORM_PATTERN);
    public static final NamedClusterPrivilege MANAGE_TOKEN = new ActionClusterPrivilege("manage_token", MANAGE_TOKEN_PATTERN);
    public static final NamedClusterPrivilege MANAGE_WATCHER = new ActionClusterPrivilege("manage_watcher", MANAGE_WATCHER_PATTERN);
    public static final NamedClusterPrivilege MANAGE_ROLLUP = new ActionClusterPrivilege("manage_rollup", MANAGE_ROLLUP_PATTERN);
    public static final NamedClusterPrivilege MANAGE_IDX_TEMPLATES =
        new ActionClusterPrivilege("manage_index_templates", MANAGE_IDX_TEMPLATE_PATTERN);
    public static final NamedClusterPrivilege MANAGE_INGEST_PIPELINES =
        new ActionClusterPrivilege("manage_ingest_pipelines", MANAGE_INGEST_PIPELINE_PATTERN);
    public static final NamedClusterPrivilege TRANSPORT_CLIENT = new ActionClusterPrivilege("transport_client",
        TRANSPORT_CLIENT_PATTERN);
    public static final NamedClusterPrivilege MANAGE_SECURITY = new ActionClusterPrivilege("manage_security", ALL_SECURITY_PATTERN,
            Collections.singleton(DelegatePkiAuthenticationAction.NAME));
    public static final NamedClusterPrivilege MANAGE_SAML = new ActionClusterPrivilege("manage_saml", MANAGE_SAML_PATTERN);
    public static final NamedClusterPrivilege MANAGE_OIDC = new ActionClusterPrivilege("manage_oidc", MANAGE_OIDC_PATTERN);
    public static final NamedClusterPrivilege MANAGE_API_KEY = new ActionClusterPrivilege("manage_api_key", MANAGE_API_KEY_PATTERN);
    public static final NamedClusterPrivilege MANAGE_PIPELINE = new ActionClusterPrivilege("manage_pipeline",
        Collections.singleton("cluster:admin/ingest/pipeline/*"));
    public static final NamedClusterPrivilege MANAGE_CCR = new ActionClusterPrivilege("manage_ccr", MANAGE_CCR_PATTERN);
    public static final NamedClusterPrivilege READ_CCR = new ActionClusterPrivilege("read_ccr", READ_CCR_PATTERN);
    public static final NamedClusterPrivilege CREATE_SNAPSHOT = new ActionClusterPrivilege("create_snapshot", CREATE_SNAPSHOT_PATTERN);
    public static final NamedClusterPrivilege MONITOR_SNAPSHOT = new ActionClusterPrivilege("monitor_snapshot", MONITOR_SNAPSHOT_PATTERN);
    public static final NamedClusterPrivilege MANAGE_ILM = new ActionClusterPrivilege("manage_ilm", MANAGE_ILM_PATTERN);
    public static final NamedClusterPrivilege READ_ILM = new ActionClusterPrivilege("read_ilm", READ_ILM_PATTERN);
    public static final NamedClusterPrivilege MANAGE_SLM = new ActionClusterPrivilege("manage_slm", MANAGE_SLM_PATTERN);
    public static final NamedClusterPrivilege READ_SLM = new ActionClusterPrivilege("read_slm", READ_SLM_PATTERN);
    public static final NamedClusterPrivilege DELEGATE_PKI = new ActionClusterPrivilege("delegate_pki",
            Sets.newHashSet(DelegatePkiAuthenticationAction.NAME, InvalidateTokenAction.NAME));

    public static final NamedClusterPrivilege MANAGE_OWN_API_KEY = ManageOwnApiKeyClusterPrivilege.INSTANCE;
    public static final NamedClusterPrivilege MANAGE_ENRICH = new ActionClusterPrivilege("manage_enrich", MANAGE_ENRICH_AUTOMATON);

    private static final Map<String, NamedClusterPrivilege> VALUES = Collections.unmodifiableMap(
        Stream.of(
        NONE,
        ALL,
        MONITOR,
        MONITOR_ML,
        MONITOR_TRANSFORM_DEPRECATED,
        MONITOR_TRANSFORM,
        MONITOR_WATCHER,
        MONITOR_ROLLUP,
        MANAGE,
        MANAGE_ML,
        MANAGE_TRANSFORM_DEPRECATED,
        MANAGE_TRANSFORM,
        MANAGE_TOKEN,
        MANAGE_WATCHER,
        MANAGE_IDX_TEMPLATES,
        MANAGE_INGEST_PIPELINES,
        TRANSPORT_CLIENT,
        MANAGE_SECURITY,
        MANAGE_SAML,
        MANAGE_OIDC,
        MANAGE_API_KEY,
        MANAGE_PIPELINE,
        MANAGE_ROLLUP,
        MANAGE_CCR,
        READ_CCR,
        CREATE_SNAPSHOT,
        MONITOR_SNAPSHOT,
        MANAGE_ILM,
        READ_ILM,
        MANAGE_SLM,
        READ_SLM,
        DELEGATE_PKI,
        MANAGE_OWN_API_KEY,
        MANAGE_ENRICH).collect(Collectors.toMap(cp -> cp.name(), cp -> cp)));

    /**
     * Resolves a {@link NamedClusterPrivilege} from a given name if it exists.
     * If the name is a cluster action, then it converts the name to pattern and creates a {@link ActionClusterPrivilege}
     *
     * @param name either {@link ClusterPrivilegeResolver#names()} or cluster action {@link #isClusterAction(String)}
     * @return instance of {@link NamedClusterPrivilege}
     */
    public static NamedClusterPrivilege resolve(String name) {
        name = Objects.requireNonNull(name).toLowerCase(Locale.ROOT);
        if (isClusterAction(name)) {
            return new ActionClusterPrivilege(name, Collections.singleton(actionToPattern(name)));
        }
        final NamedClusterPrivilege fixedPrivilege = VALUES.get(name);
        if (fixedPrivilege != null) {
            return fixedPrivilege;
        }
        String errorMessage = "unknown cluster privilege [" + name + "]. a privilege must be either " +
            "one of the predefined cluster privilege names [" +
            Strings.collectionToCommaDelimitedString(VALUES.keySet()) + "] or a pattern over one of the available " +
            "cluster actions";
        logger.debug(errorMessage);
        throw new IllegalArgumentException(errorMessage);

    }

    public static Set<String> names() {
        return Collections.unmodifiableSet(VALUES.keySet());
    }

    public static boolean isClusterAction(String actionName) {
        return actionName.startsWith("cluster:") || actionName.startsWith("indices:admin/template/");
    }

    private static String actionToPattern(String text) {
        return text + "*";
    }

}
