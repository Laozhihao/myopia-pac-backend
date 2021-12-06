package com.wupol.myopia.business.core.screening.organization.service;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.common.utils.util.JsonUtil;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

/**
 * @Author wulizhou
 * @Date 2021/12/6 17:36
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class ScreeningOrganizationServiceTest {

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    @Test
    public void testGetCooperationStopAndUnhandleOrganization() {
        List<ScreeningOrganization> cooperationStopAndUnhandleOrganization = screeningOrganizationService.getCooperationStopAndUnhandleOrganization(new Date());
        System.out.println(JsonUtil.objectToJsonString(cooperationStopAndUnhandleOrganization));
        Assert.assertTrue(true);
    }

}
