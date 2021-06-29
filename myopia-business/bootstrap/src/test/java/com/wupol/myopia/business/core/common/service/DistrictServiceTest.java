package com.wupol.myopia.business.core.common.service;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.common.utils.util.JsonUtil;
import com.wupol.myopia.business.core.common.domain.model.District;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author wulizhou
 * @Date 2021/6/29 10:19
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class DistrictServiceTest {

    @Autowired
    private DistrictService districtService;

    @Test
    public void testGetProvinceDistrictTreePriorityCache() {
        District province = districtService.getProvinceDistrictTreePriorityCache(440582000L);
        System.out.println(JsonUtil.objectToJsonString(province));
    }

}
