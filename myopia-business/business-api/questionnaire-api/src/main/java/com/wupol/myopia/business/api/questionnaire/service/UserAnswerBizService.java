package com.wupol.myopia.business.api.questionnaire.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.questionnaire.domain.SchoolListResponseDTO;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户答案
 *
 * @author Simple4H
 */
@Service
public class UserAnswerBizService {

    @Resource
    private UserAnswerFactory userAnswerFactory;

    @Resource
    private GovDeptService govDeptService;

    @Resource
    private DistrictService districtService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private ScreeningTaskService screeningTaskService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    /**
     * 保存答案
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveUserAnswer(UserAnswerDTO requestDTO, CurrentUser user) {
        Integer questionnaireId = requestDTO.getQuestionnaireId();
        List<UserAnswerDTO.QuestionDTO> questionList = requestDTO.getQuestionList();
        Integer userId = user.getExQuestionnaireUserId();
        Integer questionnaireUserType = user.getQuestionnaireUserType();

        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(questionnaireUserType);

        // 数据校验
        iUserAnswerService.preCheck(requestDTO);

        // 更新记录表
        Integer recordId = iUserAnswerService.saveUserQuestionRecord(questionnaireId, userId, requestDTO.getIsFinish(), requestDTO.getQuestionnaireIds(), requestDTO.getDistrictCode(), requestDTO.getSchoolId());

        // 答案为空不保存
        if (!CollectionUtils.isEmpty(questionList)) {
            // 先删除，后新增
            iUserAnswerService.deletedUserAnswer(questionnaireId, userId, questionList, recordId);

            // 保存用户答案
            iUserAnswerService.saveUserAnswer(requestDTO, userId, recordId);
        }

        // 保存进度
        iUserAnswerService.saveUserProgress(requestDTO, userId, requestDTO.getIsFinish());

        // 处理隐藏问题
        iUserAnswerService.hiddenQuestion(questionnaireId, userId, recordId);

        // 获取用户答题状态
        return iUserAnswerService.getUserAnswerIsFinish(userId);
    }

    /**
     * 是否完成问卷
     *
     * @param user 用户
     *
     * @return Boolean
     */
    public Boolean userAnswerIsFinish(CurrentUser user) {
        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(user.getQuestionnaireUserType());
        return iUserAnswerService.getUserAnswerIsFinish(user.getExQuestionnaireUserId());
    }

    /**
     * 获取学校名称
     *
     * @param user 用户
     *
     * @return 学校名称
     */
    public String getSchoolName(CurrentUser user) {
        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(user.getQuestionnaireUserType());
        return iUserAnswerService.getUserName(user.getExQuestionnaireUserId());
    }

    /**
     * 问卷是否完成
     *
     * @return 是否完成
     */
    public Boolean questionnaireIsFinish(Integer questionnaireId, CurrentUser user, Long districtCode, Integer schoolId) {
        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(user.getQuestionnaireUserType());
        return iUserAnswerService.questionnaireIsFinish(user.getExQuestionnaireUserId(), questionnaireId, districtCode, schoolId);
    }

    /**
     * 获取学校
     */
    public List<SchoolListResponseDTO> getSchoolList(String name, CurrentUser user) {
        List<School> schoolList = getGovOrgSchoolList(name, user);
        Map<Integer, District> districtMap = districtService.getByIds(schoolList.stream().map(School::getDistrictId).collect(Collectors.toList()));
        return schoolList.stream().map(s -> generateSchoolResponse(s, districtMap.get(s.getDistrictId()))).collect(Collectors.toList());
    }

    /**
     * 获取行政区域
     *
     * @return List<District>
     */
    public List<District> getDistrict(CurrentUser user, Integer schoolId) {
        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(user.getQuestionnaireUserType());
        return iUserAnswerService.getDistrict(schoolId);
    }

    /**
     * 获取学校信息
     *
     * @param user 用户
     *
     * @return SchoolListResponseDTO
     */
    public SchoolListResponseDTO getSchoolInfo(CurrentUser user) {
        if (!user.isQuestionnaireSchoolUser()) {
            throw new BusinessException("身份异常!");
        }
        School school = schoolService.getById(user.getExQuestionnaireUserId());
        if (Objects.isNull(school)) {
            throw new BusinessException("数据异常!");
        }
        SchoolListResponseDTO responseDTO = generateSchoolResponse(school, districtService.getById(school.getDistrictId()));
        ScreeningPlanSchool planSchool = screeningPlanSchoolService.getLastBySchoolIdAndScreeningType(school.getId(), ScreeningTypeEnum.COMMON_DISEASE.getType());
        responseDTO.setPlanId(planSchool.getScreeningPlanId());
        return responseDTO;
    }

    /**
     * 生成学校信息
     */
    private SchoolListResponseDTO generateSchoolResponse(School school, District district) {
        SchoolListResponseDTO responseDTO = new SchoolListResponseDTO();
        responseDTO.setSchoolId(school.getId());
        responseDTO.setName(school.getName());
        responseDTO.setSchoolNo(school.getSchoolNo());
        if (Objects.isNull(district)) {
            return responseDTO;
        }
        String code = String.valueOf(district.getCode());
        responseDTO.setProvinceNo(code.substring(0, 2));
        responseDTO.setCityNo(code.substring(2, 4));
        responseDTO.setAreaNo(code.substring(4, 6));
        responseDTO.setAreaType(school.getAreaType());
        responseDTO.setMonitorType(school.getMonitorType());
        Long areaCode = getAreaCode(school, district);
        responseDTO.setDistrict(districtService.districtCodeToTree(areaCode));
        responseDTO.setSchoolCommonDiseaseCode(schoolService.getSchoolCommonDiseaseCode(code, school.getAreaType(), school.getMonitorType(), school.getId()));
        return responseDTO;
    }


    /**
     * 政府获取下属于行政区域
     *
     * @return List<District>
     */
    public List<District> govNextDistrict(CurrentUser user) {
        List<School> schoolList = getGovOrgSchoolList(null, user);
        Map<Integer, District> districtMap = districtService.getByIds(schoolList.stream().map(School::getDistrictId).collect(Collectors.toList()));
        List<Long> areaCode = schoolList.stream().map(school -> getAreaCode(school, districtMap.get(school.getDistrictId()))).collect(Collectors.toList());

        List<District> result = new ArrayList<>();
        areaCode.forEach(s -> result.addAll(districtService.getTopDistrictByCode(s)));
        List<District> allDistrict = districtService.getAllDistrict(result, new ArrayList<>());
        return districtService.keepAreaDistrictsTree(allDistrict);
    }

    /**
     * 获取区域Code
     *
     * @param school   学校
     * @param district 区域
     *
     * @return 区域Code
     */
    private Long getAreaCode(School school, District district) {
        Long areaCode = null;
        List<District> list = JSON.parseArray(school.getDistrictDetail(), District.class);
        if (StringUtils.equalsAny(String.valueOf(district.getCode()).substring(0,2), "11", "12", "31", "50")) {
            if (list.size() == 1) {
                areaCode = list.get(0).getCode();
            }
            if (list.size() >= 2) {
                areaCode = list.get(1).getCode();
            }
        } else {
            if (list.size() == 2) {
                areaCode = list.get(1).getCode();
            }
            if (list.size() >= 3) {
                areaCode = list.get(2).getCode();
            }
        }
        return areaCode;
    }

    /**
     * 政府获取行政区域
     *
     * @return List<District>
     */
    public List<District> govDistrictDetail(CurrentUser user) {
        Integer districtId = getUserDistrictId(user);
        return districtService.getCurrentAreaDistrict(districtId);
    }

    /**
     * 获取政府人员的行政区域
     *
     * @return 行政区域Id
     */
    private Integer getUserDistrictId(CurrentUser user) {
        if (!user.isQuestionnaireGovUser()) {
            throw new BusinessException("政府身份异常!");
        }
        Integer orgId = user.getExQuestionnaireUserId();
        GovDept govDept = govDeptService.getById(orgId);
        return govDept.getDistrictId();
    }

    /**
     * 获取学校
     *
     * @param name 学校名称
     * @param user 用户
     *
     * @return List<School>
     */
    private List<School> getGovOrgSchoolList(String name, CurrentUser user) {
        ScreeningTask task = screeningTaskService.getOneByOrgId(user.getExQuestionnaireUserId());
        if (Objects.isNull(task)) {
            throw new BusinessException("你没有问卷需要填写");
        }
        List<ScreeningPlanSchool> planSchools = screeningPlanSchoolService.getByPlanIds(screeningPlanService.getByTaskId(task.getId()).stream().map(ScreeningPlan::getId).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(planSchools)) {
            throw new BusinessException("你没有问卷需要填写");
        }
        List<Integer> schoolIds = planSchools.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList());
        List<School> schoolList = schoolService.getByNameAndIds(name, schoolIds);
        if (CollectionUtils.isEmpty(schoolList)) {
            return new ArrayList<>();
        }
        return schoolList;
    }

    /**
     * 获取用户答案
     */
    public UserAnswerDTO getUserAnswerList(Integer questionnaireId, CurrentUser user, Long districtCode, Integer schoolId, Integer planId) {
        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(user.getQuestionnaireUserType());
        return iUserAnswerService.getUserAnswerList(questionnaireId, user.getExQuestionnaireUserId(), districtCode, schoolId, planId);
    }

}
