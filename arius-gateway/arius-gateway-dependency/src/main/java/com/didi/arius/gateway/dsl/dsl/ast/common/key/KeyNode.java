package com.didi.arius.gateway.dsl.dsl.ast.common.key;

import com.didi.arius.gateway.dsl.dsl.ast.common.Node;

public abstract class KeyNode extends Node {
    public String value;

    public KeyNode(Object obj) {
        value = (String) obj;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null) {
            return false;
        }

        if(!(obj instanceof KeyNode)) {
            return false;
        }

        KeyNode n = (KeyNode) obj;
        if(n.value.equals(value)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
