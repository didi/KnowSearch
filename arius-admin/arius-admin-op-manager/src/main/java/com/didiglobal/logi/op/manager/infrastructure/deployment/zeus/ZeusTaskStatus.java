package com.didiglobal.logi.op.manager.infrastructure.deployment.zeus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-15 2:55 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZeusTaskStatus {

    private List<String> success;

    private List<String> failed;

    private List<String> running;

    private List<String> waiting;

    private List<String> timeout;

    private List<String> killed;

    private List<String> cancelled;

    private List<String> ignored;

    public void addZeusTaskStatus(ZeusTaskStatus status) {
        if (null == success) {
            success = status.getSuccess();
        } else {
            if (null != status.getSuccess()) {
                success.addAll(status.getSuccess());
            }
        }

        if (null == failed) {
            failed = status.getFailed();
        } else {
            if (null != status.getFailed()) {
                failed.addAll(status.getFailed());
            }
        }

        if (null == running) {
            running = status.getRunning();
        } else {
            if (null != status.getRunning()) {
                running.addAll(status.getRunning());
            }
        }

        if (null == waiting) {
            waiting = status.getWaiting();
        } else {
            if (null != status.getWaiting()) {
                waiting.addAll(status.getWaiting());
            }
        }

        if (null == timeout) {
            timeout = status.getTimeout();
        } else {
            if (null != status.getTimeout()) {
                timeout.addAll(status.getTimeout());
            }
        }

        if (null == killed) {
            killed = status.getKilled();
        } else {
            if (null != status.getKilled()) {
                killed.addAll(status.getKilled());
            }
        }

        if (null == cancelled) {
            cancelled = status.getCancelled();
        } else {
            if (null != status.getCancelled()) {
                cancelled.addAll(status.getCancelled());
            }
        }
    }

}
