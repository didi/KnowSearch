/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.authz.accesscontrol;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.util.set.Sets;
import org.elasticsearch.xpack.core.security.authz.IndicesAndAliasesResolverField;
import org.elasticsearch.xpack.core.security.authz.permission.DocumentPermissions;
import org.elasticsearch.xpack.core.security.authz.permission.FieldPermissions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the field and document permissions per concrete index based on the current request.
 */
public class IndicesAccessControl {

    public static final IndicesAccessControl ALLOW_ALL = new IndicesAccessControl(true, Collections.emptyMap());
    public static final IndicesAccessControl ALLOW_NO_INDICES = new IndicesAccessControl(true,
            Collections.singletonMap(IndicesAndAliasesResolverField.NO_INDEX_PLACEHOLDER,
                    new IndicesAccessControl.IndexAccessControl(true, new FieldPermissions(), DocumentPermissions.allowAll())));
    public static final IndicesAccessControl DENIED = new IndicesAccessControl(false, Collections.emptyMap());

    private final boolean granted;
    private final Map<String, IndexAccessControl> indexPermissions;

    public IndicesAccessControl(boolean granted, Map<String, IndexAccessControl> indexPermissions) {
        this.granted = granted;
        this.indexPermissions = indexPermissions;
    }

    /**
     * @return The document and field permissions for an index if exist, otherwise <code>null</code> is returned.
     *         If <code>null</code> is being returned this means that there are no field or document level restrictions.
     */
    @Nullable
    public IndexAccessControl getIndexPermissions(String index) {
        return indexPermissions.get(index);
    }

    /**
     * @return Whether any role / permission group is allowed to access all indices.
     */
    public boolean isGranted() {
        return granted;
    }

    /**
     * Encapsulates the field and document permissions for an index.
     */
    public static class IndexAccessControl {

        private final boolean granted;
        private final FieldPermissions fieldPermissions;
        private final DocumentPermissions documentPermissions;

        public IndexAccessControl(boolean granted, FieldPermissions fieldPermissions, DocumentPermissions documentPermissions) {
            this.granted = granted;
            this.fieldPermissions = (fieldPermissions == null) ? FieldPermissions.DEFAULT : fieldPermissions;
            this.documentPermissions = (documentPermissions == null) ? DocumentPermissions.allowAll() : documentPermissions;
        }

        /**
         * @return Whether any role / permission group is allowed to this index.
         */
        public boolean isGranted() {
            return granted;
        }

        /**
         * @return The allowed fields for this index permissions.
         */
        public FieldPermissions getFieldPermissions() {
            return fieldPermissions;
        }

        /**
         * @return The allowed documents expressed as a query for this index permission. If <code>null</code> is returned
         *         then this means that there are no document level restrictions
         */
        @Nullable
        public DocumentPermissions getDocumentPermissions() {
            return documentPermissions;
        }

        /**
         * Returns a instance of {@link IndexAccessControl}, where the privileges for {@code this} object are constrained by the privileges
         * contained in the provided parameter.<br>
         * Allowed fields for this index permission would be an intersection of allowed fields.<br>
         * Allowed documents for this index permission would be an intersection of allowed documents.<br>
         *
         * @param limitedByIndexAccessControl {@link IndexAccessControl}
         * @return {@link IndexAccessControl}
         * @see FieldPermissions#limitFieldPermissions(FieldPermissions)
         * @see DocumentPermissions#limitDocumentPermissions(DocumentPermissions)
         */
        public IndexAccessControl limitIndexAccessControl(IndexAccessControl limitedByIndexAccessControl) {
            final boolean granted;
            if (this.granted == limitedByIndexAccessControl.granted) {
                granted = this.granted;
            } else {
                granted = false;
            }
            FieldPermissions fieldPermissions = getFieldPermissions().limitFieldPermissions(
                    limitedByIndexAccessControl.fieldPermissions);
            DocumentPermissions documentPermissions = getDocumentPermissions()
                    .limitDocumentPermissions(limitedByIndexAccessControl.getDocumentPermissions());
            return new IndexAccessControl(granted, fieldPermissions, documentPermissions);
        }

        @Override
        public String toString() {
            return "IndexAccessControl{" +
                    "granted=" + granted +
                    ", fieldPermissions=" + fieldPermissions +
                    ", documentPermissions=" + documentPermissions +
                    '}';
        }
    }

    /**
     * Returns a instance of {@link IndicesAccessControl}, where the privileges for {@code this}
     * object are constrained by the privileges contained in the provided parameter.<br>
     *
     * @param limitedByIndicesAccessControl {@link IndicesAccessControl}
     * @return {@link IndicesAccessControl}
     */
    public IndicesAccessControl limitIndicesAccessControl(IndicesAccessControl limitedByIndicesAccessControl) {
        final boolean granted;
        if (this.granted == limitedByIndicesAccessControl.granted) {
            granted = this.granted;
        } else {
            granted = false;
        }
        Set<String> indexes = indexPermissions.keySet();
        Set<String> otherIndexes = limitedByIndicesAccessControl.indexPermissions.keySet();
        Set<String> commonIndexes = Sets.intersection(indexes, otherIndexes);

        Map<String, IndexAccessControl> indexPermissions = new HashMap<>(commonIndexes.size());
        for (String index : commonIndexes) {
            IndexAccessControl indexAccessControl = getIndexPermissions(index);
            IndexAccessControl limitedByIndexAccessControl = limitedByIndicesAccessControl.getIndexPermissions(index);
            indexPermissions.put(index, indexAccessControl.limitIndexAccessControl(limitedByIndexAccessControl));
        }
        return new IndicesAccessControl(granted, indexPermissions);
    }

    @Override
    public String toString() {
        return "IndicesAccessControl{" +
                "granted=" + granted +
                ", indexPermissions=" + indexPermissions +
                '}';
    }
}
