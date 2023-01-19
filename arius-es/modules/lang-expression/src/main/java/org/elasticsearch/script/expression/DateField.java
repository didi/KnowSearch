package org.elasticsearch.script.expression;

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Calendar;

import org.apache.lucene.queries.function.ValueSource;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.search.MultiValueMode;

/**
 * Expressions API for date fields.
 */
final class DateField {
    // no instance
    private DateField() {}

    // supported variables
    static final String VALUE_VARIABLE          = "value";
    static final String EMPTY_VARIABLE          = "empty";
    static final String LENGTH_VARIABLE         = "length";

    // supported methods
    static final String GETVALUE_METHOD         = "getValue";
    static final String ISEMPTY_METHOD          = "isEmpty";
    static final String SIZE_METHOD             = "size";
    static final String MINIMUM_METHOD          = "min";
    static final String MAXIMUM_METHOD          = "max";
    static final String AVERAGE_METHOD          = "avg";
    static final String MEDIAN_METHOD           = "median";
    static final String SUM_METHOD              = "sum";
    static final String COUNT_METHOD            = "count";

    // date-specific
    static final String GET_YEAR_METHOD         = "getYear";
    static final String GET_MONTH_METHOD        = "getMonth";
    static final String GET_DAY_OF_MONTH_METHOD = "getDayOfMonth";
    static final String GET_HOUR_OF_DAY_METHOD  = "getHourOfDay";
    static final String GET_MINUTES_METHOD      = "getMinutes";
    static final String GET_SECONDS_METHOD      = "getSeconds";

    static ValueSource getVariable(IndexFieldData<?> fieldData, String fieldName, String variable) {
        switch (variable) {
            case VALUE_VARIABLE:
                return new FieldDataValueSource(fieldData, MultiValueMode.MIN);
            case EMPTY_VARIABLE:
                return new EmptyMemberValueSource(fieldData);
            case LENGTH_VARIABLE:
                return new CountMethodValueSource(fieldData);
            default:
                throw new IllegalArgumentException("Member variable [" + variable + "] does not exist for date field [" + fieldName + "].");
        }
    }

    static ValueSource getMethod(IndexFieldData<?> fieldData, String fieldName, String method) {
        switch (method) {
            case GETVALUE_METHOD:
                return new FieldDataValueSource(fieldData, MultiValueMode.MIN);
            case ISEMPTY_METHOD:
                return new EmptyMemberValueSource(fieldData);
            case SIZE_METHOD:
                return new CountMethodValueSource(fieldData);
            case MINIMUM_METHOD:
                return new FieldDataValueSource(fieldData, MultiValueMode.MIN);
            case MAXIMUM_METHOD:
                return new FieldDataValueSource(fieldData, MultiValueMode.MAX);
            case AVERAGE_METHOD:
                return new FieldDataValueSource(fieldData, MultiValueMode.AVG);
            case MEDIAN_METHOD:
                return new FieldDataValueSource(fieldData, MultiValueMode.MEDIAN);
            case SUM_METHOD:
                return new FieldDataValueSource(fieldData, MultiValueMode.SUM);
            case COUNT_METHOD:
                return new CountMethodValueSource(fieldData);
            case GET_YEAR_METHOD:
                return new DateMethodValueSource(fieldData, MultiValueMode.MIN, method, Calendar.YEAR);
            case GET_MONTH_METHOD:
                return new DateMethodValueSource(fieldData, MultiValueMode.MIN, method, Calendar.MONTH);
            case GET_DAY_OF_MONTH_METHOD:
                return new DateMethodValueSource(fieldData, MultiValueMode.MIN, method, Calendar.DAY_OF_MONTH);
            case GET_HOUR_OF_DAY_METHOD:
                return new DateMethodValueSource(fieldData, MultiValueMode.MIN, method, Calendar.HOUR_OF_DAY);
            case GET_MINUTES_METHOD:
                return new DateMethodValueSource(fieldData, MultiValueMode.MIN, method, Calendar.MINUTE);
            case GET_SECONDS_METHOD:
                return new DateMethodValueSource(fieldData, MultiValueMode.MIN, method, Calendar.SECOND);
            default:
                throw new IllegalArgumentException("Member method [" + method + "] does not exist for date field [" + fieldName + "].");
        }
    }
}
