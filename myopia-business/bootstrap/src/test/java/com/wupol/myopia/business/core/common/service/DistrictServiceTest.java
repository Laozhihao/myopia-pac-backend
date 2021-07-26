package com.wupol.myopia.business.core.common.service;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.core.common.domain.model.District;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @Author wulizhou
 * @Date 2021/6/29 10:19
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class DistrictServiceTest {

    @Autowired
    private DistrictService districtService;

    //@Test
    public void testGetProvinceDistrictTreePriorityCache() {
        // 潮阳区所在省份
        District province = districtService.getProvinceDistrictTreePriorityCache(440582000L);
        List<District> citys = province.getChild();
        for (District city : citys) {
            // 必有广州
            if (city.getCode().equals(440100000L)) {
                List<District> areas = city.getChild();
                for (District area : areas) {
                    // 必有天河区
                    if (area.getCode().equals(440106000L)) {
                        Assert.assertTrue(true);
                        return ;
                    }
                }
            }
        }
        Assert.assertTrue(false);
    }

}
