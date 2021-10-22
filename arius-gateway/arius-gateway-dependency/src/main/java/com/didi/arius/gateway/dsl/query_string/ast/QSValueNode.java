package com.didi.arius.gateway.dsl.query_string.ast;

import com.didi.arius.gateway.dsl.query_string.visitor.QSVisitor;

public class QSValueNode extends QSNode {
    private String fuzzySlop;
    private String boost;

    public QSValueNode(String source, String fuzzySlop, String boost) {
        super(source, 0);

        this.fuzzySlop = fuzzySlop;
        this.boost = boost;
    }

    @Override
    public void accept(QSVisitor vistor) {
        vistor.visit(this);
    }


    public String getFuzzySlop() {
        return fuzzySlop;
    }

    public void setFuzzySlop(String fuzzySlop) {
        this.fuzzySlop = fuzzySlop;
    }

    public String getBoost() {
        return boost;
    }

    public void setBoost(String boost) {
        this.boost = boost;
    }
}
