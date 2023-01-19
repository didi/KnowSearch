package org.elasticsearch.dcdr.translog.replica.bulk;

import org.elasticsearch.action.ActionType;

public class TranslogSyncAction
    extends ActionType<TranslogSyncResponse> {

    public static final TranslogSyncAction INSTANCE = new TranslogSyncAction();
    public static final String NAME = "indices:data/write/dcdr/translog_operations[s]";

    private TranslogSyncAction() {
        super(NAME, TranslogSyncResponse::new);
    }
}
