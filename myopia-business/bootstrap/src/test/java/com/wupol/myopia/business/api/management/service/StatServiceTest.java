package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.business.api.management.domain.vo.RescreenReportVO;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @Author wulizhou
 * @Date 2021/5/21 15:53
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class StatServiceTest {

    @Autowired
    private StatService statService;

    @Test
    public void testGetRescreenStatInfo() {
        List<RescreenReportVO> rescreenStatInfo = statService.getRescreenStatInfo(118, 12, "吴某周", "吴队长");
        System.out.println(JSON.toJSONString(rescreenStatInfo));
    }

}
