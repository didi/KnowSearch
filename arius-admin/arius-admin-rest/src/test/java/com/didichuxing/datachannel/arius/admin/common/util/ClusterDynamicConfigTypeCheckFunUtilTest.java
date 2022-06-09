
package com.didichuxing.datachannel.arius.admin.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterDynamicConfigTypeCheckFunUtilTest {

    @Test
    void testBandwidthCheck() {

        assertFalse(ClusterDynamicConfigTypeCheckFunUtil.bandwidthCheck("proStoreSize"));
        assertTrue(ClusterDynamicConfigTypeCheckFunUtil.bandwidthCheck("100MB"));
        assertTrue(ClusterDynamicConfigTypeCheckFunUtil.bandwidthCheck("100Mb"));
        assertTrue(ClusterDynamicConfigTypeCheckFunUtil.bandwidthCheck("100Gb"));
        assertTrue(ClusterDynamicConfigTypeCheckFunUtil.bandwidthCheck("100kb"));
        assertTrue(ClusterDynamicConfigTypeCheckFunUtil.bandwidthCheck("100b"));
        assertTrue(ClusterDynamicConfigTypeCheckFunUtil.bandwidthCheck("100k"));
        assertTrue(ClusterDynamicConfigTypeCheckFunUtil.bandwidthCheck("100p"));
        assertTrue(ClusterDynamicConfigTypeCheckFunUtil.bandwidthCheck("100t"));

    }
}
