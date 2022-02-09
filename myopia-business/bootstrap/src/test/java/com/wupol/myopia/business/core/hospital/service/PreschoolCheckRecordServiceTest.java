package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.common.utils.util.JsonUtil;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentPreschoolCheckRecordDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author wulizhou
 * @Date 2022/2/9 16:55
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class PreschoolCheckRecordServiceTest {

    @Autowired
    private PreschoolCheckRecordService preschoolCheckRecordService;

    @Test
    public void testGetInit() {
        HospitalStudentPreschoolCheckRecordDTO init = preschoolCheckRecordService.getInit(108, null, 521772);
        System.out.println(JsonUtil.objectToJsonString(init));
    }

}
