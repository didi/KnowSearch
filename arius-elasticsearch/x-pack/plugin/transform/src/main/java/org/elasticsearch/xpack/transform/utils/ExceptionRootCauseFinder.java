/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.transform.utils;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.ShardSearchFailure;

/**
 * Set of static utils to find the cause of a search exception.
 */
public final class ExceptionRootCauseFinder {

    /**
     * Unwrap the exception stack and return the most likely cause.
     *
     * @param t raw Throwable
     * @return unwrapped throwable if possible
     */
    public static Throwable getRootCauseException(Throwable t) {
        // circuit breaking exceptions are at the bottom
        Throwable unwrappedThrowable = org.elasticsearch.ExceptionsHelper.unwrapCause(t);

        if (unwrappedThrowable instanceof SearchPhaseExecutionException) {
            SearchPhaseExecutionException searchPhaseException = (SearchPhaseExecutionException) t;
            for (ShardSearchFailure shardFailure : searchPhaseException.shardFailures()) {
                Throwable unwrappedShardFailure = org.elasticsearch.ExceptionsHelper.unwrapCause(shardFailure.getCause());

                if (unwrappedShardFailure instanceof ElasticsearchException) {
                    return unwrappedShardFailure;
                }
            }
        }

        return t;
    }

    /**
     * Return the best error message possible given a already unwrapped exception.
     *
     * @param t the throwable
     * @return the message string of the given throwable
     */
    public static String getDetailedMessage(Throwable t) {
        if (t instanceof ElasticsearchException) {
            return ((ElasticsearchException) t).getDetailedMessage();
        }

        return t.getMessage();
    }

    private ExceptionRootCauseFinder() {}

}
