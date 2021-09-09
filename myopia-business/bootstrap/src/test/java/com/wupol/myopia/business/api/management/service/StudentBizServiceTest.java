package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author wulizhou
 * @Date 2021/9/7 16:23
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class StudentBizServiceTest {

    @Autowired
    private StudentBizService studentBizService;

}
