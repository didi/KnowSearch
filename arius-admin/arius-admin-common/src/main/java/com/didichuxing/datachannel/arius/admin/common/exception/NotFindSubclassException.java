package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 * @author linyunan
 * @date 2021-04-25
 */
public class NotFindSubclassException extends AriusRunTimeException {

	public NotFindSubclassException(String message) {
		super(message, ResultType.NO_FIND_SUB_CLASS);
	}
}
