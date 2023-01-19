/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.predicate.conditional;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.Expressions;
import org.elasticsearch.xpack.sql.expression.gen.pipeline.Pipe;
import org.elasticsearch.xpack.sql.expression.gen.script.ParamsBuilder;
import org.elasticsearch.xpack.sql.expression.gen.script.ScriptTemplate;
import org.elasticsearch.xpack.sql.optimizer.Optimizer;
import org.elasticsearch.xpack.sql.tree.NodeInfo;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.type.DataType;
import org.elasticsearch.xpack.sql.type.DataTypeConversion;
import org.elasticsearch.xpack.sql.type.DataTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static org.elasticsearch.common.logging.LoggerMessageFormat.format;
import static org.elasticsearch.xpack.sql.expression.gen.script.ParamsBuilder.paramsBuilder;

/**
 * Implements the CASE WHEN ... THEN ... ELSE ... END expression
 */
public class Case extends ConditionalFunction {

    private final List<IfConditional> conditions;
    private final Expression elseResult;

    @SuppressWarnings("unchecked")
    public Case(Source source, List<Expression> expressions) {
        super(source, expressions);
        this.conditions = (List<IfConditional>) (List<?>) expressions.subList(0, expressions.size() - 1);
        this.elseResult = expressions.get(expressions.size() - 1);
    }

    public List<IfConditional> conditions() {
        return conditions;
    }

    public Expression elseResult() {
        return elseResult;
    }

    @Override
    public DataType dataType() {
        if (dataType == null) {
            if (conditions.isEmpty()) {
                dataType = elseResult().dataType();
            } else {
                dataType = DataType.NULL;

                for (IfConditional conditional : conditions) {
                    dataType = DataTypeConversion.commonType(dataType, conditional.dataType());
                }
                dataType = DataTypeConversion.commonType(dataType, elseResult.dataType());
            }
        }
        return dataType;
    }

    @Override
    public Expression replaceChildren(List<Expression> newChildren) {
        return new Case(source(), newChildren);
    }

    @Override
    protected NodeInfo<? extends Expression> info() {
        return NodeInfo.create(this, Case::new, children());
    }

    @Override
    protected TypeResolution resolveType() {
        DataType expectedResultDataType = null;
        for (IfConditional ifConditional : conditions) {
            if (ifConditional.result().dataType().isNull() == false) {
                expectedResultDataType = ifConditional.result().dataType();
                break;
            }
        }
        if (expectedResultDataType == null) {
            expectedResultDataType = elseResult().dataType();
        }

        for (IfConditional conditional : conditions) {
            if (conditional.condition().dataType() != DataType.BOOLEAN) {
                return new TypeResolution(format(null, "condition of [{}] must be [boolean], found value [{}] type [{}]",
                    conditional.sourceText(),
                    Expressions.name(conditional.condition()),
                    conditional.condition().dataType().typeName));
            }
            if (DataTypes.areTypesCompatible(expectedResultDataType, conditional.dataType()) == false) {
                return new TypeResolution(format(null, "result of [{}] must be [{}], found value [{}] type [{}]",
                    conditional.sourceText(),
                    expectedResultDataType.typeName,
                    Expressions.name(conditional.result()),
                    conditional.dataType().typeName));
            }
        }

        if (DataTypes.areTypesCompatible(expectedResultDataType, elseResult.dataType()) == false) {
            return new TypeResolution(format(null, "ELSE clause of [{}] must be [{}], found value [{}] type [{}]",
                elseResult.sourceText(),
                expectedResultDataType.typeName,
                Expressions.name(elseResult),
                elseResult.dataType().typeName));
        }

        return TypeResolution.TYPE_RESOLVED;
    }

    /**
     * All foldable conditions that fold to FALSE should have
     * been removed by the {@link Optimizer}#SimplifyCase.
     */
    @Override
    public boolean foldable() {
        if (conditions.isEmpty() && elseResult.foldable()) {
            return true;
        }
        if (conditions.size() == 1 && conditions.get(0).condition().foldable()) {
            if (conditions.get(0).condition().fold() == Boolean.TRUE) {
                return conditions().get(0).result().foldable();
            } else {
                return elseResult().foldable();
            }
        }
        return false;
    }

    @Override
    public Object fold() {
        if (conditions.isEmpty() == false && conditions.get(0).condition().fold() == Boolean.TRUE) {
            return conditions.get(0).result().fold();
        }
        return elseResult.fold();
    }

    @Override
    protected Pipe makePipe() {
        List<Pipe> pipes = new ArrayList<>(conditions.size() + 1);
        for (IfConditional ifConditional : conditions) {
            pipes.add(Expressions.pipe(ifConditional.condition()));
            pipes.add(Expressions.pipe(ifConditional.result()));
        }
        pipes.add(Expressions.pipe(elseResult));
        return new CasePipe(source(), this, pipes);
    }

    @Override
    public ScriptTemplate asScript() {
        List<ScriptTemplate> templates = new ArrayList<>();
        for (IfConditional ifConditional : conditions) {
            templates.add(asScript(ifConditional.condition()));
            templates.add(asScript(ifConditional.result()));
        }
        templates.add(asScript(elseResult));

        StringJoiner template = new StringJoiner(",", "{sql}.caseFunction([", "])");
        ParamsBuilder params = paramsBuilder();

        for (ScriptTemplate scriptTemplate : templates) {
            template.add(scriptTemplate.template());
            params.script(scriptTemplate.params());
        }

        return new ScriptTemplate(formatTemplate(template.toString()), params.build(), dataType());
    }
}
