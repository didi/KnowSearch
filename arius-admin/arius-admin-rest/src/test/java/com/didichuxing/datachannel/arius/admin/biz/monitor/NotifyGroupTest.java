package com.didichuxing.datachannel.arius.admin.biz.monitor;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.NotifyGroupDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.QueryNotifyGroupDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor.NotifyGroupVO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.NotifyGroupPO;
import com.didichuxing.datachannel.arius.admin.core.service.monitor.MonitorService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor.NotifyGroupDAO;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class NotifyGroupTest extends AriusAdminApplicationTest {


    @Autowired
    private NotifyGroupDAO notifyGroupDAO;

    @Autowired
    private MonitorService monitorService;


    // @Test
    public void testDAOInsert() {
        NotifyGroupPO po = new NotifyGroupPO();
        po.setAppId(99L);
        po.setName("22");
        po.setMembers("aa");
        po.setComment("bb");
        po.setStatus(1);
        po.setUserGroupId(1L);
        po.setOperator("fitz");
        final int insert = notifyGroupDAO.insert(po);
        Assertions.assertNotNull(po.getId());
        Assertions.assertTrue(po.getId() > 0);
        System.out.println(insert);
        System.out.println(po.getId());

    }

    // @Test
    public void testDAOFindNotifyGroup() {
        NotifyGroupPO po = new NotifyGroupPO();
        po.setAppId(99L);
        po.setName("22");
        po.setOperator("kent");

        List<NotifyGroupPO> notifyGroupPOS = notifyGroupDAO.selectList(new QueryWrapper<>(po));
        Assertions.assertTrue(CollectionUtils.isNotEmpty(notifyGroupPOS));
        System.out.println(notifyGroupPOS);
    }

    // @Test
    public void testDAOGetNotifyGroup() {
        NotifyGroupPO notifyGroupPO = notifyGroupDAO.selectById(1L);
        Assertions.assertNotNull(notifyGroupPO);
        System.out.println(notifyGroupPO);
    }

    // @Test
    public void testDAOUpdateNotifyGroup() {
        NotifyGroupPO po = new NotifyGroupPO();
        po.setId(1L);
        po.setName("22x");
        po.setOperator("kentx");
        int i = notifyGroupDAO.updateById(po);
        Assertions.assertTrue(i > 0);
        System.out.println(i);
    }

    //test service

    // @Test
    public void testFindNotifyGroups() {
        QueryNotifyGroupDTO dto = new QueryNotifyGroupDTO();
//        dto.setAppId(77L);
        dto.setOperator("55y");
//        dto.setName("2");
        dto.setMembers("aa");
        dto.setPageSize(2);
        dto.setPageNo(1);
//        dto.setStatus(0);
        PaginationResult<NotifyGroupVO> notifyGroupPage = monitorService.findNotifyGroupPage(dto);
        Assertions.assertNotNull(notifyGroupPage);
        System.out.println(JSON.toJSONString(notifyGroupPage));
    }

    // @Test
    public void testGetNotifyGroup() {
        final NotifyGroupPO notifyGroup = monitorService.getNotifyGroup(3L);
        Assertions.assertNotNull(notifyGroup);
        System.out.println(JSON.toJSONString(notifyGroup));
    }

    // @Test
    public void testSaveNotifyGroup() {
        NotifyGroupDTO notifyGroupDTO = new NotifyGroupDTO();
        notifyGroupDTO.setName("es_0728");
        notifyGroupDTO.setMembers("13;fitz,14;allen");
        notifyGroupDTO.setAppId(66L);
        notifyGroupDTO.setComment("es test comment");

        int i = monitorService.saveNotifyGroup(notifyGroupDTO);
        Assertions.assertTrue(i > 0);
        System.out.println(i);
    }

    // @Test
    public void testUpdateNotifyGroup() throws Exception {
        NotifyGroupDTO dto = new NotifyGroupDTO();
        dto.setId(13L);
        dto.setAppId(44L);
        dto.setName("44");
        dto.setMembers("44;allen,55;fitz,66;kent");
        dto.setComment("44");
        int i = monitorService.modifyNotifyGroup(dto);
        Assertions.assertNotNull(dto.getId());
        Assertions.assertTrue(dto.getId() > 0);
        System.out.println(dto.getId());
        System.out.println(i);
    }

}
