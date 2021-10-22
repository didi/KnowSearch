package com.didi.arius.gateway.dsl.query_string.ast;

import com.didi.arius.gateway.dsl.query_string.parser.ParseException;
import com.didi.arius.gateway.dsl.query_string.visitor.QSVisitor;

public abstract class QSNode {

    String source;

    public int needValue;

    public QSNode(String source, int needValue) {
        this.source = source;
        this.needValue = needValue;
    }

    public String getSource() {
        return source;
    }


    public void addValue() throws ParseException {
        if (needValue == 0) {
            throw new ParseException("parse error, needValue <0");
        }
        needValue--;
    }

    public boolean completeParse() {
        return needValue <= 0;
    }

    public int getNeedValue() {
        return needValue;
    }

    public abstract void accept(QSVisitor vistor);
}
