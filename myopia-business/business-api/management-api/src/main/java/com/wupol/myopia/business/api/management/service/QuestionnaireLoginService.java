package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.AuthConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dto.QuestionnaireUser;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Classname ScreeningPlanService
 * @Description
 * @Date 2022/7/27 17:45
 * @Created by limy
 */
@Service
@Slf4j
public class QuestionnaireLoginService {

    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Resource
    private SchoolService schoolService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningTaskService screeningTaskService;

    @Resource
    private ScreeningPlanService screeningPlanService;
    /**
     * 根据学校编号跟密码 获取学校信息
     *
     * @param schoolNo
     * @return
     */
    public ApiResult getSchoolBySchoolNo(String schoolNo, String password) {
        School school = checkPassword(password, schoolNo);
        if (Objects.isNull(school)) {
            return ApiResult.failure(ResultCode.DATA_STUDENT_NOT_EXIST.getCode(), ResultCode.DATA_STUDENT_NOT_EXIST.getMessage());
        }
        //是否有筛查计划
        ScreeningPlanSchool screeningPlanSchool = screeningPlanSchoolService.getLastBySchoolIdAndScreeningType(school.getId(), ScreeningTypeEnum.COMMON_DISEASE.getType());
        if (Objects.nonNull(screeningPlanSchool) && SchoolEnum.checkNotKindergartenSchool(school.getType())) {
            return ApiResult.success(new QuestionnaireUser(school.getId(), school.getGovDeptId(), school.getName()));
        }
        return ApiResult.failure(ResultCode.DATA_STUDENT_PLAN_NOT_EXIST.getCode(), ResultCode.DATA_STUDENT_PLAN_NOT_EXIST.getMessage());
    }

    /**
     * 根据Id card跟学生姓名 获取学生信息
     * @param credentialNo
     * @return
     */
    public ApiResult getStudentByCredentialNo(String credentialNo, String studentName) {
        //查询该学生
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getLastByCredentialNoAndStudentName(credentialNo, studentName);
        if (!org.springframework.util.CollectionUtils.isEmpty(screeningPlanSchoolStudent)) {
            //是否有筛查计划
            ScreeningPlanSchoolStudent student = screeningPlanSchoolStudentService.getLastByCredentialNoAndStudentIds(ScreeningTypeEnum.COMMON_DISEASE.getType(),
                    screeningPlanSchoolStudent.stream().map(ScreeningPlanSchoolStudent::getScreeningPlanId).collect(Collectors.toList()),
                    screeningPlanSchoolStudent.stream().map(ScreeningPlanSchoolStudent::getStudentId).collect(Collectors.toList()));
            if (Objects.nonNull(student) && !(SchoolAge.checkKindergarten(student.getGradeType()))) {
                return ApiResult.success(new QuestionnaireUser(student.getId(), student.getSchoolId(), student.getStudentName()));
            }
            return ApiResult.failure(ResultCode.DATA_STUDENT_PLAN_NOT_EXIST.getCode(), ResultCode.DATA_STUDENT_PLAN_NOT_EXIST.getMessage());
        }
        return ApiResult.failure(ResultCode.DATA_STUDENT_NOT_EXIST.getCode(), ResultCode.DATA_STUDENT_NOT_EXIST.getMessage());
    }


    /**
     * 检测政府是否有筛查计划
     *
     * @param orgId
     * @return
     */
    public ApiResult checkGovernmentLogin(Integer orgId) {
        ScreeningTask screeningTask = screeningTaskService.getOneByOrgId(orgId);

        if (Objects.isNull(screeningTask)) {
            return ApiResult.failure(ResultCode.DATA_STUDENT_PLAN_NOT_EXIST.getCode(), ResultCode.DATA_STUDENT_PLAN_NOT_EXIST.getMessage());
        }
        List<ScreeningPlanSchool> planSchools = screeningPlanSchoolService.getByPlanIds(screeningPlanService.getByTaskId(screeningTask.getId()).stream().map(ScreeningPlan::getId).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(planSchools)) {
            return ApiResult.failure(ResultCode.DATA_STUDENT_PLAN_NOT_EXIST.getCode(), ResultCode.DATA_STUDENT_PLAN_NOT_EXIST.getMessage());
        }
        return ApiResult.success();
    }

    /**
     * 校验学校密码
     *
     * @param password
     * @param schoolNo
     * @return
     */
    public School checkPassword(String password, String schoolNo) {
        if (!StrUtil.equals(AuthConstant.QUESTIONNAIRE_SCHOOL_SECRET, password)) {
            return null;
        }
        return schoolService.getBySchoolNo(schoolNo);
    }
}
