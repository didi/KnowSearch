package org.elasticsearch.region;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class GetRegionRequest extends ActionRequest {
    private String region;

    public GetRegionRequest() {
        this.region = "all";
    }

    public GetRegionRequest(StreamInput in) throws IOException {
        super(in);
        this.region = in.readString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(region);
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public ActionRequestValidationException validate() {
        if (Strings.isNullOrEmpty(region)) {
            ActionRequestValidationException validationException = new ActionRequestValidationException();
            validationException.addValidationError("region must not be null");
            return validationException;
        }
        return null;
    }
}
