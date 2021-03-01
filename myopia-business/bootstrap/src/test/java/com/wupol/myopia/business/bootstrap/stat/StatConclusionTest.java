package com.wupol.myopia.business.bootstrap.stat;

import java.io.IOException;
import java.util.List;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.service.VisionScreeningResultService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import junit.framework.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class StatConclusionTest {
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    @Test
    public void testInsert() throws IOException {
        VisionScreeningResult query = new VisionScreeningResult();
        query.setTaskId(1);
        List<VisionScreeningResult> list = visionScreeningResultService.findByList(query);
        for (VisionScreeningResult result : list) {
            System.out.println(result.getVisionData());
        }
        Assert.assertTrue(list != null);
    }
}
