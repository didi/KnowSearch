/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.client.ml;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.client.core.PageParams;
import org.elasticsearch.client.ml.calendars.Calendar;
import org.elasticsearch.client.ml.job.config.Job;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Objects;

/**
 * Get the Scheduled Events for a Calendar
 */
public class GetCalendarEventsRequest extends ActionRequest implements ToXContentObject {

    public static final ParseField START = new ParseField("start");
    public static final ParseField END = new ParseField("end");

    public static final ConstructingObjectParser<GetCalendarEventsRequest, Void> PARSER =
            new ConstructingObjectParser<>("get_calendar_events_request", a -> new GetCalendarEventsRequest((String)a[0]));

    static {
        PARSER.declareString(ConstructingObjectParser.constructorArg(), Calendar.ID);
        PARSER.declareString(GetCalendarEventsRequest::setStart, START);
        PARSER.declareString(GetCalendarEventsRequest::setEnd, END);
        PARSER.declareString(GetCalendarEventsRequest::setJobId, Job.ID);
        PARSER.declareObject(GetCalendarEventsRequest::setPageParams, PageParams.PARSER, PageParams.PAGE);
    }

    private final String calendarId;
    private String start;
    private String end;
    private String jobId;
    private PageParams pageParams;

    /**
     * Create a new request to get the ScheduledEvents for the given calendarId.
     *
     * @param calendarId The ID of the calendar.
     *                   Can be `_all` to get ALL ScheduledEvents for all calendars.
     */
    public GetCalendarEventsRequest(String calendarId) {
        this.calendarId = Objects.requireNonNull(calendarId, "[calendar_id] must not be null.");
    }

    public String getCalendarId() {
        return calendarId;
    }

    public PageParams getPageParams() {
        return pageParams;
    }

    /**
     * The paging parameters for the gathered ScheduledEvents
     * @param pageParams The desired paging params
     */
    public void setPageParams(PageParams pageParams) {
        this.pageParams = pageParams;
    }

    public String getStart() {
        return start;
    }

    /**
     * Specifies to get events with timestamps after this time.
     *
     * @param start String representation of a timestamp; may be an epoch seconds, epoch millis or an ISO string
     */
    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    /**
     * Specifies to get events with timestamps earlier than this time.
     *
     * @param end String representation of a timestamp; may be an epoch seconds, epoch millis or an ISO string
     */
    public void setEnd(String end) {
        this.end = end;
    }

    public String getJobId() {
        return jobId;
    }

    /**
     * The jobId for which to get the ScheduledEvents. When this option is used calendarId must be `_all`
     * @param jobId The job for which to get the events.
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(Calendar.ID.getPreferredName(), calendarId);
        if (start != null) {
            builder.field(START.getPreferredName(), start);
        }
        if (end != null) {
            builder.field(END.getPreferredName(), end);
        }
        if (jobId != null) {
            builder.field(Job.ID.getPreferredName(), jobId);
        }
        if (pageParams != null) {
            builder.field(PageParams.PAGE.getPreferredName(), pageParams);
        }
        builder.endObject();
        return builder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(calendarId, start, end, jobId, pageParams);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GetCalendarEventsRequest other = (GetCalendarEventsRequest) obj;
        return Objects.equals(calendarId, other.calendarId)
            && Objects.equals(pageParams, other.pageParams)
            && Objects.equals(start, other.start)
            && Objects.equals(end, other.end)
            && Objects.equals(jobId, other.jobId);
    }
}
