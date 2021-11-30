package com.didi.arius.gateway.remote.response;

import com.didi.arius.gateway.common.metadata.FieldInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class LargeFiledListResponse extends BaseAdminResponse {
	private Map<String, Map<String, FieldInfo>> data;

}
