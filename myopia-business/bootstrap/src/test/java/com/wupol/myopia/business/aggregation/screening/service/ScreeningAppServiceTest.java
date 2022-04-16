package com.wupol.myopia.business.aggregation.screening.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.screening.app.enums.SysEnum;
import com.wupol.myopia.business.api.screening.app.service.ScreeningAppService;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @Author xz
 * @Date 2022/4/16 11:55
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
class ScreeningAppServiceTest {
    @Autowired
    private ScreeningAppService screeningAppService;

    /**
     * 获得各项检查数据
     */
    @Test
    void testGetVisionScreeningResultByPlanStudentIdAndState() {
        Integer isState = 0;
        Integer planStudentId = 0;
        Integer orgId = CurrentUserUtil.getCurrentUser().getOrgId();
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentIdAndState(planStudentId, orgId, isState);
        if (Objects.nonNull(screeningResult)) {
            System.out.println(JSON.toJSONString(screeningResult));
        } else {
            System.out.println("查询失败");
        }
    }

    /**
     * 班级筛查进度
     */
    @Test
    void testGetClassScreeningProgress() {
        Integer schoolId = 0;
        Integer gradeId = 0;
        Integer classId = 0;
        Integer screeningOrgId = 0;
        Boolean isFilter = true;
        String studentName = null;
        screeningAppService.getClassScreeningProgress(schoolId, gradeId, classId, CurrentUserUtil.getCurrentUser().getOrgId(), isFilter, studentName);
    }
}