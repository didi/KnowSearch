package com.didi.arius.gateway.common.utils;

import java.text.DecimalFormat;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

/**
 * 系统指标
 *
 * @author shizeying
 * @date 2022/12/14
 * @since 0.3.2
 */
public class OshiUtils {

    
    public static Double getMemUseInfo() {
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        // 总内存
        long totalByte = memory.getTotal();
        // 剩余
        long acaliableByte = memory.getAvailable();
        return Double.parseDouble(String.format("%.2f", ((totalByte - acaliableByte) * 1.0 / totalByte) * 100));
    }
    
    

  

    public static String formatByte(long byteNumber){
        //换算单位
        double FORMAT = 1024.0;
        double kbNumber = byteNumber/FORMAT;
        if(kbNumber<FORMAT){
            return new DecimalFormat("#.##KB").format(kbNumber);
        }
        double mbNumber = kbNumber/FORMAT;
        if(mbNumber<FORMAT){
            return new DecimalFormat("#.##MB").format(mbNumber);
        }
        double gbNumber = mbNumber/FORMAT;
        if(gbNumber<FORMAT){
            return new DecimalFormat("#.##GB").format(gbNumber);
        }
        double tbNumber = gbNumber/FORMAT;
        return new DecimalFormat("#.##TB").format(tbNumber);
    }
}