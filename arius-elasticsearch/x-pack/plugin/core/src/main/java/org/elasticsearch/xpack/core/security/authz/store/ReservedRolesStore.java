/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.authz.store;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.remote.RemoteInfoAction;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesAction;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.xpack.core.ilm.action.GetLifecycleAction;
import org.elasticsearch.xpack.core.ilm.action.PutLifecycleAction;
import org.elasticsearch.xpack.core.monitoring.action.MonitoringBulkAction;
import org.elasticsearch.xpack.core.security.action.privilege.GetBuiltinPrivilegesAction;
import org.elasticsearch.xpack.core.security.authz.RoleDescriptor;
import org.elasticsearch.xpack.core.security.authz.permission.Role;
import org.elasticsearch.xpack.core.security.authz.privilege.ConfigurableClusterPrivilege;
import org.elasticsearch.xpack.core.security.authz.privilege.ConfigurableClusterPrivileges.ManageApplicationPrivileges;
import org.elasticsearch.xpack.core.security.support.MetadataUtils;
import org.elasticsearch.xpack.core.security.user.KibanaUser;
import org.elasticsearch.xpack.core.security.user.UsernamesField;
import org.elasticsearch.xpack.core.transform.transforms.persistence.TransformInternalIndexConstants;
import org.elasticsearch.xpack.core.watcher.execution.TriggeredWatchStoreField;
import org.elasticsearch.xpack.core.watcher.history.HistoryStoreField;
import org.elasticsearch.xpack.core.watcher.watch.Watch;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ReservedRolesStore implements BiConsumer<Set<String>, ActionListener<RoleRetrievalResult>> {

    public static final RoleDescriptor SUPERUSER_ROLE_DESCRIPTOR = new RoleDescriptor("superuser",
            new String[] { "all" },
            new RoleDescriptor.IndicesPrivileges[] {
                    RoleDescriptor.IndicesPrivileges.builder().indices("*").privileges("all").allowRestrictedIndices(true).build()},
            new RoleDescriptor.ApplicationResourcePrivileges[] {
                RoleDescriptor.ApplicationResourcePrivileges.builder().application("*").privileges("*").resources("*").build()
            },
            null, new String[] { "*" },
            MetadataUtils.DEFAULT_RESERVED_METADATA, Collections.emptyMap());
    public static final Role SUPERUSER_ROLE = Role.builder(SUPERUSER_ROLE_DESCRIPTOR, null).build();
    private static final Map<String, RoleDescriptor> RESERVED_ROLES = initializeReservedRoles();

    private static Map<String, RoleDescriptor> initializeReservedRoles() {
        return MapBuilder.<String, RoleDescriptor>newMapBuilder()
                .put("superuser", SUPERUSER_ROLE_DESCRIPTOR)
                .put("transport_client", new RoleDescriptor("transport_client", new String[] { "transport_client" }, null, null,
                        MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("kibana_admin", kibanaAdminUser("kibana_admin", MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("kibana_user", kibanaAdminUser("kibana_user",
                    MetadataUtils.getDeprecatedReservedMetadata("Please use the [kibana_admin] role instead")))
                .put("monitoring_user", new RoleDescriptor("monitoring_user",
                        new String[] { "cluster:monitor/main", "cluster:monitor/xpack/info", RemoteInfoAction.NAME },
                        new RoleDescriptor.IndicesPrivileges[] {
                            RoleDescriptor.IndicesPrivileges.builder()
                                .indices(".monitoring-*").privileges("read", "read_cross_cluster").build()
                        },
                        new RoleDescriptor.ApplicationResourcePrivileges[] {
                            RoleDescriptor.ApplicationResourcePrivileges.builder()
                                .application("kibana-*").resources("*").privileges("reserved_monitoring").build()
                        },
                        null, null, MetadataUtils.DEFAULT_RESERVED_METADATA, null))
                .put("remote_monitoring_agent", new RoleDescriptor("remote_monitoring_agent",
                        new String[] {
                                "manage_index_templates", "manage_ingest_pipelines", "monitor",
                                "cluster:monitor/xpack/watcher/watch/get",
                                "cluster:admin/xpack/watcher/watch/put",
                                "cluster:admin/xpack/watcher/watch/delete",
                        },
                        new RoleDescriptor.IndicesPrivileges[] {
                                RoleDescriptor.IndicesPrivileges.builder().indices(".monitoring-*").privileges("all").build(),
                                RoleDescriptor.IndicesPrivileges.builder()
                                    .indices("metricbeat-*").privileges("index", "create_index").build() },
                        null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("remote_monitoring_collector", new RoleDescriptor(
                        "remote_monitoring_collector",
                        new String[] {
                            "monitor"
                        },
                        new RoleDescriptor.IndicesPrivileges[] {
                            RoleDescriptor.IndicesPrivileges.builder()
                                .indices("*").privileges("monitor").allowRestrictedIndices(true).build(),
                            RoleDescriptor.IndicesPrivileges.builder()
                                .indices(".kibana*").privileges("read").build()
                        },
                        null,
                        null,
                        null,
                        MetadataUtils.DEFAULT_RESERVED_METADATA,
                        null
                ))
                .put("ingest_admin", new RoleDescriptor("ingest_admin", new String[] { "manage_index_templates", "manage_pipeline" },
                        null, null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                // reporting_user doesn't have any privileges in Elasticsearch, and Kibana authorizes privileges based on this role
                .put("reporting_user", new RoleDescriptor("reporting_user", null, null,
                        null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("kibana_dashboard_only_user", new RoleDescriptor(
                        "kibana_dashboard_only_user",
                        null,
                        null,
                        new RoleDescriptor.ApplicationResourcePrivileges[] {
                            RoleDescriptor.ApplicationResourcePrivileges.builder()
                            .application("kibana-.kibana").resources("*").privileges("read").build() },
                        null, null,
                        MetadataUtils.getDeprecatedReservedMetadata("Please use Kibana feature privileges instead"),
                        null))
                .put(KibanaUser.ROLE_NAME, new RoleDescriptor(KibanaUser.ROLE_NAME,
                        new String[] {
                            "monitor", "manage_index_templates", MonitoringBulkAction.NAME, "manage_saml", "manage_token", "manage_oidc",
                            GetBuiltinPrivilegesAction.NAME, "delegate_pki", GetLifecycleAction.NAME,  PutLifecycleAction.NAME
                        },
                        new RoleDescriptor.IndicesPrivileges[] {
                                RoleDescriptor.IndicesPrivileges.builder()
                                        .indices(".kibana*", ".reporting-*").privileges("all").build(),
                                RoleDescriptor.IndicesPrivileges.builder()
                                        .indices(".monitoring-*").privileges("read", "read_cross_cluster").build(),
                                RoleDescriptor.IndicesPrivileges.builder()
                                        .indices(".management-beats").privileges("create_index", "read", "write").build(),
                                // .apm-* is for APM's agent configuration index creation
                                RoleDescriptor.IndicesPrivileges.builder()
                                        .indices(".apm-agent-configuration").privileges("all").build(),
                        },
                        null,
                        new ConfigurableClusterPrivilege[] { new ManageApplicationPrivileges(Collections.singleton("kibana-*")) },
                        null, MetadataUtils.DEFAULT_RESERVED_METADATA, null))
                .put("logstash_system", new RoleDescriptor("logstash_system", new String[] { "monitor", MonitoringBulkAction.NAME},
                        null, null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("beats_admin", new RoleDescriptor("beats_admin",
                    null,
                    new RoleDescriptor.IndicesPrivileges[] {
                        RoleDescriptor.IndicesPrivileges.builder().indices(".management-beats").privileges("all").build()
                    },
                    null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put(UsernamesField.BEATS_ROLE, new RoleDescriptor(UsernamesField.BEATS_ROLE,
                        new String[] { "monitor", MonitoringBulkAction.NAME},
                        new RoleDescriptor.IndicesPrivileges[]{
                            RoleDescriptor.IndicesPrivileges.builder()
                                .indices(".monitoring-beats-*").privileges("create_index", "create").build()
                        },
                    null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put(UsernamesField.APM_ROLE, new RoleDescriptor(UsernamesField.APM_ROLE,
                        new String[] { "monitor", MonitoringBulkAction.NAME},
                        new RoleDescriptor.IndicesPrivileges[]{
                            RoleDescriptor.IndicesPrivileges.builder()
                                .indices(".monitoring-beats-*").privileges("create_index", "create_doc").build()
                        },
                    null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("apm_user", new RoleDescriptor("apm_user",
                    null, new RoleDescriptor.IndicesPrivileges[] {
                        RoleDescriptor.IndicesPrivileges.builder().indices("apm-*")
                            .privileges("read", "view_index_metadata").build(),
                        RoleDescriptor.IndicesPrivileges.builder().indices(".ml-anomalies*")
                            .privileges("view_index_metadata", "read").build(),
                    }, null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("machine_learning_user", new RoleDescriptor("machine_learning_user", new String[] { "monitor_ml" },
                        new RoleDescriptor.IndicesPrivileges[] {
                                RoleDescriptor.IndicesPrivileges.builder().indices(".ml-anomalies*", ".ml-notifications*")
                                        .privileges("view_index_metadata", "read").build(),
                                RoleDescriptor.IndicesPrivileges.builder().indices(".ml-annotations*")
                                        .privileges("view_index_metadata", "read", "write").build()
                        },
                        new RoleDescriptor.ApplicationResourcePrivileges[] {
                            RoleDescriptor.ApplicationResourcePrivileges.builder()
                                .application("kibana-*").resources("*").privileges("reserved_ml").build()
                        },
                        null, null, MetadataUtils.DEFAULT_RESERVED_METADATA, null))
                .put("machine_learning_admin", new RoleDescriptor("machine_learning_admin", new String[] { "manage_ml" },
                        new RoleDescriptor.IndicesPrivileges[] {
                                RoleDescriptor.IndicesPrivileges.builder()
                                        .indices(".ml-anomalies*", ".ml-notifications*", ".ml-state*", ".ml-meta*")
                                        .privileges("view_index_metadata", "read").build(),
                                RoleDescriptor.IndicesPrivileges.builder().indices(".ml-annotations*")
                                        .privileges("view_index_metadata", "read", "write").build()
                        },
                        new RoleDescriptor.ApplicationResourcePrivileges[] {
                            RoleDescriptor.ApplicationResourcePrivileges.builder()
                                .application("kibana-*").resources("*").privileges("reserved_ml").build()
                        },
                        null, null, MetadataUtils.DEFAULT_RESERVED_METADATA, null))
                // DEPRECATED: to be removed in 9.0.0
                .put("data_frame_transforms_admin", new RoleDescriptor("data_frame_transforms_admin",
                        new String[] { "manage_data_frame_transforms" },
                        new RoleDescriptor.IndicesPrivileges[]{
                            RoleDescriptor.IndicesPrivileges.builder()
                                .indices(TransformInternalIndexConstants.AUDIT_INDEX_PATTERN,
                                         TransformInternalIndexConstants.AUDIT_INDEX_PATTERN_DEPRECATED,
                                         TransformInternalIndexConstants.AUDIT_INDEX_READ_ALIAS)
                                .privileges("view_index_metadata", "read").build()
                        },
                        new RoleDescriptor.ApplicationResourcePrivileges[] {
                            RoleDescriptor.ApplicationResourcePrivileges.builder()
                                .application("kibana-*").resources("*").privileges("reserved_ml").build()
                        }, null, null, MetadataUtils.DEFAULT_RESERVED_METADATA, null))
                // DEPRECATED: to be removed in 9.0.0
                .put("data_frame_transforms_user", new RoleDescriptor("data_frame_transforms_user",
                        new String[] { "monitor_data_frame_transforms" },
                        new RoleDescriptor.IndicesPrivileges[]{
                            RoleDescriptor.IndicesPrivileges.builder()
                                .indices(TransformInternalIndexConstants.AUDIT_INDEX_PATTERN,
                                         TransformInternalIndexConstants.AUDIT_INDEX_PATTERN_DEPRECATED,
                                         TransformInternalIndexConstants.AUDIT_INDEX_READ_ALIAS)
                                .privileges("view_index_metadata", "read").build()
                        },
                        new RoleDescriptor.ApplicationResourcePrivileges[] {
                            RoleDescriptor.ApplicationResourcePrivileges.builder()
                                .application("kibana-*").resources("*").privileges("reserved_ml").build()
                        }, null, null, MetadataUtils.DEFAULT_RESERVED_METADATA, null))
                .put("transform_admin", new RoleDescriptor("transform_admin",
                        new String[] { "manage_transform" },
                        new RoleDescriptor.IndicesPrivileges[]{
                            RoleDescriptor.IndicesPrivileges.builder()
                                .indices(TransformInternalIndexConstants.AUDIT_INDEX_PATTERN,
                                         TransformInternalIndexConstants.AUDIT_INDEX_PATTERN_DEPRECATED,
                                         TransformInternalIndexConstants.AUDIT_INDEX_READ_ALIAS)
                                .privileges("view_index_metadata", "read").build()
                        }, null, null, null, MetadataUtils.DEFAULT_RESERVED_METADATA, null))
                .put("transform_user", new RoleDescriptor("transform_user",
                        new String[] { "monitor_transform" },
                        new RoleDescriptor.IndicesPrivileges[]{
                            RoleDescriptor.IndicesPrivileges.builder()
                                 .indices(TransformInternalIndexConstants.AUDIT_INDEX_PATTERN,
                                          TransformInternalIndexConstants.AUDIT_INDEX_PATTERN_DEPRECATED,
                                          TransformInternalIndexConstants.AUDIT_INDEX_READ_ALIAS)
                                .privileges("view_index_metadata", "read").build()
                        }, null, null, null, MetadataUtils.DEFAULT_RESERVED_METADATA, null))
                .put("watcher_admin", new RoleDescriptor("watcher_admin", new String[] { "manage_watcher" },
                        new RoleDescriptor.IndicesPrivileges[] {
                                RoleDescriptor.IndicesPrivileges.builder().indices(Watch.INDEX, TriggeredWatchStoreField.INDEX_NAME,
                                        HistoryStoreField.INDEX_PREFIX + "*").privileges("read").build() },
                        null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("watcher_user", new RoleDescriptor("watcher_user", new String[] { "monitor_watcher" },
                        new RoleDescriptor.IndicesPrivileges[] {
                                RoleDescriptor.IndicesPrivileges.builder().indices(Watch.INDEX)
                                        .privileges("read")
                                        .build(),
                                RoleDescriptor.IndicesPrivileges.builder().indices(HistoryStoreField.INDEX_PREFIX + "*")
                                        .privileges("read")
                                        .build() }, null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("logstash_admin", new RoleDescriptor("logstash_admin", null, new RoleDescriptor.IndicesPrivileges[] {
                        RoleDescriptor.IndicesPrivileges.builder().indices(".logstash*")
                                .privileges("create", "delete", "index", "manage", "read").build() },
                        null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("rollup_user", new RoleDescriptor("rollup_user", new String[] { "monitor_rollup" },
                        null, null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("rollup_admin", new RoleDescriptor("rollup_admin", new String[] { "manage_rollup" },
                        null, null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .put("snapshot_user", new RoleDescriptor("snapshot_user", new String[] { "create_snapshot", GetRepositoriesAction.NAME },
                        new RoleDescriptor.IndicesPrivileges[] { RoleDescriptor.IndicesPrivileges.builder()
                                .indices("*")
                                .privileges("view_index_metadata")
                                .allowRestrictedIndices(true)
                                .build() }, null, null, null, MetadataUtils.DEFAULT_RESERVED_METADATA, null))
                .put("enrich_user", new RoleDescriptor("enrich_user", new String[]{ "manage_enrich", "manage_ingest_pipelines", "monitor" },
                        new RoleDescriptor.IndicesPrivileges[]{  RoleDescriptor.IndicesPrivileges.builder()
                            .indices(".enrich-*")
                            .privileges("manage", "read", "write")
                            .build() }, null, MetadataUtils.DEFAULT_RESERVED_METADATA))
                .immutableMap();
    }

    private static RoleDescriptor kibanaAdminUser(String name, Map<String, Object> metadata) {
        return new RoleDescriptor(name, null, null,
            new RoleDescriptor.ApplicationResourcePrivileges[] {
                RoleDescriptor.ApplicationResourcePrivileges.builder()
                    .application("kibana-.kibana")
                    .resources("*").privileges("all")
                    .build() },
            null, null, metadata, null);
    }

    public static boolean isReserved(String role) {
        return RESERVED_ROLES.containsKey(role) || UsernamesField.SYSTEM_ROLE.equals(role) || UsernamesField.XPACK_ROLE.equals(role);
    }

    public Map<String, Object> usageStats() {
        return Collections.emptyMap();
    }

    public RoleDescriptor roleDescriptor(String role) {
        return RESERVED_ROLES.get(role);
    }

    public Collection<RoleDescriptor> roleDescriptors() {
        return RESERVED_ROLES.values();
    }

    public static Set<String> names() {
        return RESERVED_ROLES.keySet();
    }

    @Override
    public void accept(Set<String> roleNames, ActionListener<RoleRetrievalResult> listener) {
        final Set<RoleDescriptor> descriptors = roleNames.stream()
            .map(RESERVED_ROLES::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        listener.onResponse(RoleRetrievalResult.success(descriptors));
    }

    @Override
    public String toString() {
        return "reserved roles store";
    }
}
