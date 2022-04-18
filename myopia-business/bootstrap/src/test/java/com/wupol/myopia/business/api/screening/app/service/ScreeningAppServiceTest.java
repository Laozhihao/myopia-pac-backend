package com.wupol.myopia.business.api.screening.app.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanStudentBizService;
import com.wupol.myopia.business.api.screening.app.domain.vo.ClassScreeningProgress;
import com.wupol.myopia.business.api.screening.app.enums.SysEnum;
import com.wupol.myopia.business.api.screening.app.service.ScreeningAppService;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentVO;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import org.junit.jupiter.api.Test;
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
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    /**
     * 获得各项检查数据
     */
    @Test
    void testGetVisionScreeningResultByPlanStudentIdAndState() {
        Integer isState = 0;
        Integer planStudentId = 19;
        Integer orgId = 3;
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
        Integer schoolId = 4;
        Integer gradeId = 22;
        Integer classId = 82;
        Integer screeningOrgId = 3;
        Boolean isFilter = true;
        String studentName = null;
        ClassScreeningProgress data = screeningAppService.getClassScreeningProgress(schoolId, gradeId, classId, screeningOrgId, isFilter, studentName);
        System.out.println(JSON.toJSONString(data));
    }

    /**
     * 个人筛查进度
     */
    @Test
    void testGetScreeningProgress(){
        Integer planStudentId = 19;
        Integer isState = 1;
        VisionScreeningResult screeningResult = visionScreeningResultService.findOne(new VisionScreeningResult().setScreeningPlanSchoolStudentId(planStudentId).setIsDoubleScreen(isState == 1));
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(planStudentId);
        StudentVO studentVO = StudentVO.getInstance(screeningPlanSchoolStudent);
        System.out.println(JSON.toJSONString(StudentScreeningProgressVO.getInstanceWithDefault(screeningResult, studentVO)));
    }
}