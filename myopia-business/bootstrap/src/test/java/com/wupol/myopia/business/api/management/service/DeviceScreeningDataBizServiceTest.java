package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.JsonUtil;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataQueryDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author wulizhou
 * @Date 2021/6/29 18:28
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class DeviceScreeningDataBizServiceTest {

    @Autowired
    private DeviceScreeningDataBizService deviceScreeningDataBizService;

    @Test
    public void testGetPage() {
        DeviceScreeningDataQueryDTO query = new DeviceScreeningDataQueryDTO();
        PageRequest pageRequest = new PageRequest();
        System.out.println(JsonUtil.objectToJsonString(deviceScreeningDataBizService.getPage(query, pageRequest)));
        query.setScreeningTimeStart(new Date());
        System.out.println(JsonUtil.objectToJsonString(deviceScreeningDataBizService.getPage(query, pageRequest)));
        query.setScreeningOrgNameSearch("机构");
        System.out.println(JsonUtil.objectToJsonString(deviceScreeningDataBizService.getPage(query, pageRequest)));
        query.setSphEnd(new BigDecimal("5.0"));
        System.out.println(JsonUtil.objectToJsonString(deviceScreeningDataBizService.getPage(query, pageRequest)));
        query.setPatientGender(1);
        System.out.println(JsonUtil.objectToJsonString(deviceScreeningDataBizService.getPage(query, pageRequest)));
    }

}
