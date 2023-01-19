/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.predicate.conditional;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.Expressions;
import org.elasticsearch.xpack.sql.expression.Literal;
import org.elasticsearch.xpack.sql.tree.NodeInfo;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.type.DataType;
import org.elasticsearch.xpack.sql.type.DataTypes;

import java.util.Arrays;
import java.util.List;

import static org.elasticsearch.common.logging.LoggerMessageFormat.format;
import static org.elasticsearch.xpack.sql.expression.TypeResolutions.isBoolean;
import static org.elasticsearch.xpack.sql.util.CollectionUtils.combine;

public class Iif extends Case {

    public Iif(Source source, Expression condition, Expression thenResult, Expression elseResult) {
        super(source, Arrays.asList(new IfConditional(source, condition, thenResult), elseResult != null ? elseResult : Literal.NULL));
    }

    Iif(Source source, List<Expression> expressions) {
        super(source, expressions);
    }

    @Override
    protected NodeInfo<? extends Iif> info() {
        return NodeInfo.create(this, Iif::new, combine(conditions(), elseResult()));
    }

    @Override
    public Expression replaceChildren(List<Expression> newChildren) {
        return new Iif(source(), newChildren);
    }

    @Override
    protected TypeResolution resolveType() {
        if (conditions().isEmpty()) {
            return TypeResolution.TYPE_RESOLVED;
        }

        TypeResolution conditionTypeResolution = isBoolean(conditions().get(0).condition(), sourceText(), Expressions.ParamOrdinal.FIRST);
        if (conditionTypeResolution.unresolved()) {
            return conditionTypeResolution;
        }

        DataType resultDataType = conditions().get(0).dataType();
        if (DataTypes.areTypesCompatible(resultDataType, elseResult().dataType()) == false) {
            return new TypeResolution(format(null, "third argument of [{}] must be [{}], found value [{}] type [{}]",
                sourceText(),
                resultDataType.typeName,
                Expressions.name(elseResult()),
                elseResult().dataType().typeName));
        }
        return TypeResolution.TYPE_RESOLVED;
    }
}
