package com.didichuxing.datachannel.arius.admin.common.function;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import java.util.Objects;
import java.util.function.Function;

/**
 * 可丢弃双函数
 *
 * @author shizeying
 * @date 2022/08/17
 */
@FunctionalInterface
public interface BiFunctionWithESOperateException<T, U, R> {
	
	/**
	 * 它接受两个参数并返回一个值。
	 *
	 * @param t 函数的第一个参数。
	 * @param u 函数的第一个参数。
	 * @return 一个接受两个参数并返回一个值的函数。
	 */
	R apply(T t, U u) throws ESOperateException;
	

	/**
	 * > 返回一个组合函数，该函数首先将此函数应用于其输入，然后将 after 函数应用于结果
	 *
	 * @param after 应用此功能后要应用的功能
	 * @return 一个接受两个参数并返回结果的函数。
	 */
	default <V> BiFunctionWithESOperateException<T, U, V> andThen(Function<? super R, ? extends V> after) throws ESOperateException {
		Objects.requireNonNull(after);
		return (T t, U u) -> after.apply(apply(t, u));
	}
}