package com.didi.arius.gateway.core.component.log.process;

public interface LogProcess<T> {
    public void dealLog(T records);
}
