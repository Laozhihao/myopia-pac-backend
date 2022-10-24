package com.wupol.myopia.business.api.screening.app.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.api.screening.app.domain.dto.DeviationDTO;
import com.wupol.myopia.business.api.screening.app.domain.vo.ClassScreeningProgress;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentVO;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.util.ResourceHelper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    @Autowired
    private VisionScreeningBizService visionScreeningBizService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;

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
        Assert.assertTrue(true);
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
        ClassScreeningProgress data = screeningAppService.getClassScreeningProgress(schoolId, gradeId, classId, new CurrentUser(), isFilter, 1,0);
        System.out.println(JSON.toJSONString(data));
        Assert.assertTrue(true);
    }

    /**
     * 个人筛查进度
     */
    @Test
    void testGetScreeningProgress() {
        Integer planStudentId = 19;
        Integer isState = 1;
        VisionScreeningResult screeningResult = visionScreeningResultService.findOne(new VisionScreeningResult().setScreeningPlanSchoolStudentId(planStudentId).setIsDoubleScreen(isState == 1));
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(planStudentId);
        StudentVO studentVO = StudentVO.getInstance(screeningPlanSchoolStudent, schoolGradeService.getById(screeningPlanSchoolStudent.getGradeId()), schoolClassService.getById(screeningPlanSchoolStudent.getClassId()));
        System.out.println(JSON.toJSONString(StudentScreeningProgressVO.getInstanceWithDefault(screeningResult, studentVO,screeningPlanSchoolStudent)));
        Assert.assertTrue(true);
    }

    /**
     * 是否可以复测
     */
    @Test
    void checkState() {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(19, 3);
        if (Objects.isNull(screeningResult)) {
            System.out.println("没有初筛");
        }
        visionScreeningBizService.verifyScreening(screeningResult,false);
        Assert.assertTrue(true);
    }

    /**
     * 不能检查原因
     */
    @Test
    void addNoExamine() {
        Integer planStudentId = 19;
        Integer state = 1;
        ScreeningPlanSchoolStudent screeningPlan = screeningPlanSchoolStudentService.findOne(new ScreeningPlanSchoolStudent().setId(planStudentId));
        Assert.assertNotNull("不存在筛查计划", screeningPlan);
        screeningPlan.setState(state);
        screeningPlanSchoolStudentService.updateById(screeningPlan);
        System.out.println("成功");
        Assert.assertTrue(true);
    }

    /**
     * 筛查不准说明
     */
    @Test
    void addInaccurate() {
        DeviationDTO deviationDTO = JSON.parseObject(ResourceHelper.getResourceAsString(getClass(), "/json/deviation.json"), DeviationDTO.class);
        if (deviationDTO.isValid()) {
            // 只是复测数据
            deviationDTO.setIsState(1);
            visionScreeningBizService.saveOrUpdateStudentScreenData(deviationDTO);
        } else {
            System.out.println("不是复测数据");
        }
        Assert.assertTrue(true);
    }
}