package org.elasticsearch.index.translog;

/**
 * author weizijun
 * date：2019-08-27
 */
public class TranslogOperation {
    /**
     * 当前translog位置的operation
     */
    private Translog.Operation operation;

    /**
     * 当前translog的位置
     */
    private Translog.Location current;

    /**
     * 下一条translog的位置，也是当前translog位置的末尾
     */
    private Translog.Location next;

    public TranslogOperation(Translog.Operation operation, Translog.Location current, Translog.Location next) {
        this.operation = operation;
        this.current = current;
        this.next = next;
    }

    public Translog.Operation getOperation() {
        return operation;
    }

    public void setOperation(Translog.Operation operation) {
        this.operation = operation;
    }

    public Translog.Location getCurrent() {
        return current;
    }

    public void setCurrent(Translog.Location current) {
        this.current = current;
    }

    public Translog.Location getNext() {
        return next;
    }

    public void setNext(Translog.Location next) {
        this.next = next;
    }
}
