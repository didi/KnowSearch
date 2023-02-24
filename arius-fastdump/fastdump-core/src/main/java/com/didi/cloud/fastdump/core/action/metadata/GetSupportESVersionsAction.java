package com.didi.cloud.fastdump.core.action.metadata;

import com.didi.cloud.fastdump.common.enums.ESClusterVersionEnum;
import com.didi.cloud.fastdump.core.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetSupportESVersionsAction implements Action<Void, List<String>> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(GetSupportESVersionsAction.class);
    @Override
    public List<String> doAction(Void unused) throws Exception {
        List<String> supportESVersions = Arrays.stream(ESClusterVersionEnum.values())
                .map(ESClusterVersionEnum::getVersion).collect(Collectors.toList());
        return supportESVersions;
    }
}
