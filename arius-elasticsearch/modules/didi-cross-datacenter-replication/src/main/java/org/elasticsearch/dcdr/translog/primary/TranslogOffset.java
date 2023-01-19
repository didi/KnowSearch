package org.elasticsearch.dcdr.translog.primary;

import java.util.Objects;

import org.elasticsearch.index.translog.Translog;

/**
 * author weizijun
 * date：2019-08-06
 */
public class TranslogOffset implements Comparable<TranslogOffset> {
    private final Translog.Location current;
    private final Translog.Location next;

    private TranslogOffset(Translog.Location current, Translog.Location next) {
        this.current = current;
        this.next = next;
    }

    @Override
    public String toString() {
        return "TranslogOffset{" +
            "current=" + current +
            ", next=" + next +
            '}';
    }

    public String toReadableString() {
        return current.generation + "g/" + current.translogLocation + "t/" + current.size + "s";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TranslogOffset that = (TranslogOffset) o;
        return Objects.equals(current, that.current) &&
            Objects.equals(next, that.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(current, next);
    }

    public static TranslogOffset createOffset(Translog.Location current, Translog.Location next) {
        return new TranslogOffset(current, next);
    }

    public Translog.Location getCurrent() {
        return current;
    }

    public Translog.Location getNext() {
        return next;
    }

    @Override
    public int compareTo(TranslogOffset o) {
        return this.current.compareTo(o.current);
    }
}
