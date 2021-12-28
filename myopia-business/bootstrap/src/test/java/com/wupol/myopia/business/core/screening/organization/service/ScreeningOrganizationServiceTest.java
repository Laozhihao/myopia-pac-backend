package com.wupol.myopia.business.core.screening.organization.service;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author wulizhou
 * @Date 2021/12/6 17:36
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class ScreeningOrganizationServiceTest {

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

}
