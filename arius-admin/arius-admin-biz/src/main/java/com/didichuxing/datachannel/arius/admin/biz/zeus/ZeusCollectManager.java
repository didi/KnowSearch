package com.didichuxing.datachannel.arius.admin.biz.zeus;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESZeusHostInfoDTO;

/**
 * Created by linyunan on 2021-09-14
 */
public interface ZeusCollectManager {
	/**
	 * @param esZeusHostInfoDTO      Zeus采集节点信息实体
	 * @return
	 */
	@Deprecated
	Result<Boolean> updateHttpAddressFromZeus(ESZeusHostInfoDTO esZeusHostInfoDTO);
}
