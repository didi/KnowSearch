/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.plan.logical;

import org.elasticsearch.xpack.sql.analysis.index.EsIndex;
import org.elasticsearch.xpack.sql.expression.Attribute;
import org.elasticsearch.xpack.sql.expression.FieldAttribute;
import org.elasticsearch.xpack.sql.tree.NodeInfo;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.type.EsField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class EsRelation extends LeafPlan {

    private final EsIndex index;
    private final List<Attribute> attrs;
    private final boolean frozen;

    public EsRelation(Source source, EsIndex index, boolean frozen) {
        super(source);
        this.index = index;
        this.attrs = flatten(source, index.mapping());
        this.frozen = frozen;
    }

    @Override
    protected NodeInfo<EsRelation> info() {
        return NodeInfo.create(this, EsRelation::new, index, frozen);
    }

    private static List<Attribute> flatten(Source source, Map<String, EsField> mapping) {
        return flatten(source, mapping, null);
    }

    private static List<Attribute> flatten(Source source, Map<String, EsField> mapping, FieldAttribute parent) {
        List<Attribute> list = new ArrayList<>();

        for (Entry<String, EsField> entry : mapping.entrySet()) {
            String name = entry.getKey();
            EsField t = entry.getValue();

            if (t != null) {
                FieldAttribute f = new FieldAttribute(source, parent, parent != null ? parent.name() + "." + name : name, t);
                list.add(f);
                // object or nested
                if (t.getProperties().isEmpty() == false) {
                    list.addAll(flatten(source, t.getProperties(), f));
                }
            }
        }
        return list;
    }

    public EsIndex index() {
        return index;
    }

    public boolean frozen() {
        return frozen;
    }

    @Override
    public List<Attribute> output() {
        return attrs;
    }

    @Override
    public boolean expressionsResolved() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, frozen);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        EsRelation other = (EsRelation) obj;
        return Objects.equals(index, other.index)
                && frozen == other.frozen;
    }

    private static final int TO_STRING_LIMIT = 52;

    private static <E> String limitedToString(Collection<E> c) {
        Iterator<E> it = c.iterator();
        if (!it.hasNext()) {
            return "[]";
        }

        // ..]
        StringBuilder sb = new StringBuilder(TO_STRING_LIMIT + 4);
        sb.append('[');
        for (;;) {
            E e = it.next();
            String next = e == c ? "(this Collection)" : String.valueOf(e);
            if (next.length() + sb.length() > TO_STRING_LIMIT) {
                sb.append(next.substring(0, Math.max(0, TO_STRING_LIMIT - sb.length())));
                sb.append('.').append('.').append(']');
                return sb.toString();
            } else {
                sb.append(next);
            }
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }

    @Override
    public String nodeString() {
        return nodeName() + "[" + index + "]" + limitedToString(attrs);
    }
}