package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author wulizhou
 * @Date 2021/12/15 15:30
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class HospitalDoctorServiceTest {

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    @Test
    public void testRepair() {
        Assert.assertTrue(hospitalDoctorService.repair(1));
    }

}
