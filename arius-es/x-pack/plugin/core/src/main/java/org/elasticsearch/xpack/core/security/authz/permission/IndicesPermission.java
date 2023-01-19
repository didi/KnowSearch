/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.authz.permission;

import org.apache.logging.log4j.LogManager;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.Operations;
import org.apache.lucene.util.automaton.TooComplexToDeterminizeException;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.cluster.metadata.AliasOrIndex;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.regex.Regex;
import org.elasticsearch.xpack.core.security.authz.accesscontrol.IndicesAccessControl;
import org.elasticsearch.xpack.core.security.authz.privilege.IndexPrivilege;
import org.elasticsearch.xpack.core.security.index.RestrictedIndicesNames;
import org.elasticsearch.xpack.core.security.support.Automatons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * A permission that is based on privileges for index related actions executed
 * on specific indices
 */
public final class IndicesPermission {

    public static final IndicesPermission NONE = new IndicesPermission();

    private final ConcurrentMap<String, Predicate<String>> allowedIndicesMatchersForAction = new ConcurrentHashMap<>();

    private final Group[] groups;

    public IndicesPermission(Group... groups) {
        this.groups = groups;
    }

    public static Predicate<String> indexMatcher(Collection<String> indices) {
        Set<String> exactMatch = new HashSet<>();
        List<String> nonExactMatch = new ArrayList<>();
        for (String indexPattern : indices) {
            if (indexPattern.startsWith("/") || indexPattern.contains("*") || indexPattern.contains("?")) {
                nonExactMatch.add(indexPattern);
            } else {
                exactMatch.add(indexPattern);
            }
        }

        if (exactMatch.isEmpty() && nonExactMatch.isEmpty()) {
            return s -> false;
        } else if (exactMatch.isEmpty()) {
            return buildAutomataPredicate(nonExactMatch);
        } else if (nonExactMatch.isEmpty()) {
            return buildExactMatchPredicate(exactMatch);
        } else {
            return buildExactMatchPredicate(exactMatch).or(buildAutomataPredicate(nonExactMatch));
        }
    }

    private static Predicate<String> buildExactMatchPredicate(Set<String> indices) {
        if (indices.size() == 1) {
            final String singleValue = indices.iterator().next();
            return singleValue::equals;
        }
        return indices::contains;
    }

    private static Predicate<String> buildAutomataPredicate(List<String> indices) {
        try {
            return Automatons.predicate(indices);
        } catch (TooComplexToDeterminizeException e) {
            LogManager.getLogger(IndicesPermission.class).debug("Index pattern automaton [{}] is too complex", indices);
            String description = Strings.collectionToCommaDelimitedString(indices);
            if (description.length() > 80) {
                description = Strings.cleanTruncate(description, 80) + "...";
            }
            throw new ElasticsearchSecurityException("The set of permitted index patterns [{}] is too complex to evaluate", e, description);
        }
    }

    public Group[] groups() {
        return groups;
    }

    /**
     * @return A predicate that will match all the indices that this permission
     * has the privilege for executing the given action on.
     */
    public Predicate<String> allowedIndicesMatcher(String action) {
        return allowedIndicesMatchersForAction.computeIfAbsent(action, a -> Group.buildIndexMatcherPredicateForAction(a, groups));
    }

    /**
     * Checks if the permission matches the provided action, without looking at indices.
     * To be used in very specific cases where indices actions need to be authorized regardless of their indices.
     * The usecase for this is composite actions that are initially only authorized based on the action name (indices are not
     * checked on the coordinating node), and properly authorized later at the shard level checking their indices as well.
     */
    public boolean check(String action) {
        for (Group group : groups) {
            if (group.check(action)) {
                return true;
            }
        }
        return false;
    }

    /**
     * For given index patterns and index privileges determines allowed privileges and creates an instance of {@link ResourcePrivilegesMap}
     * holding a map of resource to {@link ResourcePrivileges} where resource is index pattern and the map of index privilege to whether it
     * is allowed or not.
     *
     * @param checkForIndexPatterns check permission grants for the set of index patterns
     * @param allowRestrictedIndices if {@code true} then checks permission grants even for restricted indices by index matching
     * @param checkForPrivileges check permission grants for the set of index privileges
     * @return an instance of {@link ResourcePrivilegesMap}
     */
    public ResourcePrivilegesMap checkResourcePrivileges(Set<String> checkForIndexPatterns, boolean allowRestrictedIndices,
                                                         Set<String> checkForPrivileges) {
        final ResourcePrivilegesMap.Builder resourcePrivilegesMapBuilder = ResourcePrivilegesMap.builder();
        final Map<IndicesPermission.Group, Automaton> predicateCache = new HashMap<>();
        for (String forIndexPattern : checkForIndexPatterns) {
            Automaton checkIndexAutomaton = Automatons.patterns(forIndexPattern);
            if (false == allowRestrictedIndices && false == isConcreteRestrictedIndex(forIndexPattern)) {
                checkIndexAutomaton = Automatons.minusAndMinimize(checkIndexAutomaton, RestrictedIndicesNames.NAMES_AUTOMATON);
            }
            if (false == Operations.isEmpty(checkIndexAutomaton)) {
                Automaton allowedIndexPrivilegesAutomaton = null;
                for (Group group : groups) {
                    final Automaton groupIndexAutomaton = predicateCache.computeIfAbsent(group,
                            g -> IndicesPermission.Group.buildIndexMatcherAutomaton(g.allowRestrictedIndices(), g.indices()));
                    if (Operations.subsetOf(checkIndexAutomaton, groupIndexAutomaton)) {
                        if (allowedIndexPrivilegesAutomaton != null) {
                            allowedIndexPrivilegesAutomaton = Automatons
                                    .unionAndMinimize(Arrays.asList(allowedIndexPrivilegesAutomaton, group.privilege().getAutomaton()));
                        } else {
                            allowedIndexPrivilegesAutomaton = group.privilege().getAutomaton();
                        }
                    }
                }
                for (String privilege : checkForPrivileges) {
                    IndexPrivilege indexPrivilege = IndexPrivilege.get(Collections.singleton(privilege));
                    if (allowedIndexPrivilegesAutomaton != null
                            && Operations.subsetOf(indexPrivilege.getAutomaton(), allowedIndexPrivilegesAutomaton)) {
                        resourcePrivilegesMapBuilder.addResourcePrivilege(forIndexPattern, privilege, Boolean.TRUE);
                    } else {
                        resourcePrivilegesMapBuilder.addResourcePrivilege(forIndexPattern, privilege, Boolean.FALSE);
                    }
                }
            } else {
                // the index pattern produced the empty automaton, presumably because the requested pattern expands exclusively inside the
                // restricted indices namespace - a namespace of indices that are normally hidden when granting/checking privileges - and
                // the pattern was not marked as `allowRestrictedIndices`. We try to anticipate this by considering _explicit_ restricted
                // indices even if `allowRestrictedIndices` is false.
                // TODO The `false` result is a _safe_ default but this is actually an error. Make it an error.
                for (String privilege : checkForPrivileges) {
                    resourcePrivilegesMapBuilder.addResourcePrivilege(forIndexPattern, privilege, Boolean.FALSE);
                }
            }
        }
        return resourcePrivilegesMapBuilder.build();
    }

    public Automaton allowedActionsMatcher(String index) {
        List<Automaton> automatonList = new ArrayList<>();
        for (Group group : groups) {
            if (group.indexNameMatcher.test(index)) {
                automatonList.add(group.privilege.getAutomaton());
            }
        }
        return automatonList.isEmpty() ? Automatons.EMPTY : Automatons.unionAndMinimize(automatonList);
    }

    /**
     * Authorizes the provided action against the provided indices, given the current cluster metadata
     */
    public Map<String, IndicesAccessControl.IndexAccessControl> authorize(String action, Set<String> requestedIndicesOrAliases,
                                                                          Map<String, AliasOrIndex> allAliasesAndIndices,
                                                                          FieldPermissionsCache fieldPermissionsCache) {
        // now... every index that is associated with the request, must be granted
        // by at least one indices permission group
        Map<String, Set<FieldPermissions>> fieldPermissionsByIndex = new HashMap<>();
        Map<String, DocumentLevelPermissions> roleQueriesByIndex = new HashMap<>();
        Map<String, Boolean> grantedBuilder = new HashMap<>();

        for (String indexOrAlias : requestedIndicesOrAliases) {
            boolean granted = false;
            Set<String> concreteIndices = new HashSet<>();
            AliasOrIndex aliasOrIndex = allAliasesAndIndices.get(indexOrAlias);
            if (aliasOrIndex != null) {
                for (IndexMetaData indexMetaData : aliasOrIndex.getIndices()) {
                    concreteIndices.add(indexMetaData.getIndex().getName());
                }
            }

            for (Group group : groups) {
                if (group.check(action, indexOrAlias)) {
                    granted = true;
                    for (String index : concreteIndices) {
                        Set<FieldPermissions> fieldPermissions = fieldPermissionsByIndex.computeIfAbsent(index, (k) -> new HashSet<>());
                        fieldPermissionsByIndex.put(indexOrAlias, fieldPermissions);
                        fieldPermissions.add(group.getFieldPermissions());
                        DocumentLevelPermissions permissions =
                                roleQueriesByIndex.computeIfAbsent(index, (k) -> new DocumentLevelPermissions());
                        roleQueriesByIndex.putIfAbsent(indexOrAlias, permissions);
                        if (group.hasQuery()) {
                            permissions.addAll(group.getQuery());
                        } else {
                            // if more than one permission matches for a concrete index here and if
                            // a single permission doesn't have a role query then DLS will not be
                            // applied even when other permissions do have a role query
                            permissions.setAllowAll(true);
                        }
                    }
                }
            }

            if (concreteIndices.isEmpty()) {
                grantedBuilder.put(indexOrAlias, granted);
            } else {
                grantedBuilder.put(indexOrAlias, granted);
                for (String concreteIndex : concreteIndices) {
                    grantedBuilder.put(concreteIndex, granted);
                }
            }
        }

        Map<String, IndicesAccessControl.IndexAccessControl> indexPermissions = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : grantedBuilder.entrySet()) {
            String index = entry.getKey();
            DocumentLevelPermissions permissions = roleQueriesByIndex.get(index);
            final Set<BytesReference> roleQueries;
            if (permissions != null && permissions.isAllowAll() == false) {
                roleQueries = unmodifiableSet(permissions.queries);
            } else {
                roleQueries = null;
            }

            final FieldPermissions fieldPermissions;
            final Set<FieldPermissions> indexFieldPermissions = fieldPermissionsByIndex.get(index);
            if (indexFieldPermissions != null && indexFieldPermissions.isEmpty() == false) {
                fieldPermissions = indexFieldPermissions.size() == 1 ? indexFieldPermissions.iterator().next() :
                        fieldPermissionsCache.getFieldPermissions(indexFieldPermissions);
            } else {
                fieldPermissions = FieldPermissions.DEFAULT;
            }
            indexPermissions.put(index, new IndicesAccessControl.IndexAccessControl(entry.getValue(), fieldPermissions,
                    (roleQueries != null) ? DocumentPermissions.filteredBy(roleQueries) : DocumentPermissions.allowAll()));
        }
        return unmodifiableMap(indexPermissions);
    }

    private boolean isConcreteRestrictedIndex(String indexPattern) {
        if (Regex.isSimpleMatchPattern(indexPattern) || Automatons.isLuceneRegex(indexPattern)) {
            return false;
        }
        return RestrictedIndicesNames.isRestricted(indexPattern);
    }

    public static class Group {
        private final IndexPrivilege privilege;
        private final Predicate<String> actionMatcher;
        private final String[] indices;
        private final Predicate<String> indexNameMatcher;
        private final FieldPermissions fieldPermissions;
        private final Set<BytesReference> query;
        // by default certain restricted indices are exempted when granting privileges, as they should generally be hidden for ordinary
        // users. Setting this flag true eliminates the special status for the purpose of this permission - restricted indices still have
        // to be covered by the "indices"
        private final boolean allowRestrictedIndices;

        public Group(IndexPrivilege privilege, FieldPermissions fieldPermissions, @Nullable Set<BytesReference> query,
                boolean allowRestrictedIndices, String... indices) {
            assert indices.length != 0;
            this.privilege = privilege;
            this.actionMatcher = privilege.predicate();
            this.indices = indices;
            this.indexNameMatcher = indexMatcher(Arrays.asList(indices));
            this.fieldPermissions = Objects.requireNonNull(fieldPermissions);
            this.query = query;
            this.allowRestrictedIndices = allowRestrictedIndices;
        }

        public IndexPrivilege privilege() {
            return privilege;
        }

        public String[] indices() {
            return indices;
        }

        @Nullable
        public Set<BytesReference> getQuery() {
            return query;
        }

        public FieldPermissions getFieldPermissions() {
            return fieldPermissions;
        }

        private boolean check(String action) {
            return actionMatcher.test(action);
        }

        private boolean check(String action, String index) {
            assert index != null;
            return check(action) && indexNameMatcher.test(index)
                    && (allowRestrictedIndices || (false == RestrictedIndicesNames.isRestricted(index)));
        }

        boolean hasQuery() {
            return query != null;
        }

        public boolean allowRestrictedIndices() {
            return allowRestrictedIndices;
        }

        public static Automaton buildIndexMatcherAutomaton(boolean allowRestrictedIndices, String... indices) {
            final Automaton indicesAutomaton = Automatons.patterns(indices);
            if (allowRestrictedIndices) {
                return indicesAutomaton;
            } else {
                return Automatons.minusAndMinimize(indicesAutomaton, RestrictedIndicesNames.NAMES_AUTOMATON);
            }
        }

        private static Predicate<String> buildIndexMatcherPredicateForAction(String action, Group... groups) {
            final Set<String> ordinaryIndices = new HashSet<>();
            final Set<String> restrictedIndices = new HashSet<>();
            for (final Group group : groups) {
                if (group.actionMatcher.test(action)) {
                    if (group.allowRestrictedIndices) {
                        restrictedIndices.addAll(Arrays.asList(group.indices()));
                    } else {
                        ordinaryIndices.addAll(Arrays.asList(group.indices()));
                    }
                }
            }
            final Predicate<String> predicate;
            if (restrictedIndices.isEmpty()) {
                predicate = indexMatcher(ordinaryIndices)
                    .and(index -> false == RestrictedIndicesNames.isRestricted(index));
            } else if (ordinaryIndices.isEmpty()) {
                predicate = indexMatcher(restrictedIndices);
            } else {
                predicate = indexMatcher(restrictedIndices)
                    .or(indexMatcher(ordinaryIndices)
                         .and(index -> false == RestrictedIndicesNames.isRestricted(index)));
            }
            return predicate;
        }
    }

    private static class DocumentLevelPermissions {

        private Set<BytesReference> queries = null;
        private boolean allowAll = false;

        private void addAll(Set<BytesReference> query) {
            if (allowAll == false) {
                if (queries == null) {
                    queries = new HashSet<>();
                }
                queries.addAll(query);
            }
        }

        private boolean isAllowAll() {
            return allowAll;
        }

        private void setAllowAll(boolean allowAll) {
            this.allowAll = allowAll;
        }
    }
}
