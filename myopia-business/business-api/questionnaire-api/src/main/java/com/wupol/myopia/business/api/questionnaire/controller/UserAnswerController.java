package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.questionnaire.domain.SchoolListResponseDTO;
import com.wupol.myopia.business.api.questionnaire.service.UserAnswerBizService;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.school.service.SchoolService;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Simple4H
 */
@ResponseResultBody
@RestController
@RequestMapping("/questionnaire/userAnswer")
public class UserAnswerController {

    @Resource
    private UserAnswerBizService userAnswerBizService;

    @Resource
    private SchoolService schoolService;


    /**
     * 获取用户答案
     *
     * @param questionnaireId 问卷Id
     *
     * @return UserAnswerDTO
     */
    @GetMapping("list")
    public UserAnswerDTO getUserAnswerList(Integer questionnaireId, Long districtCode, Integer schoolId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.getUserAnswerList(questionnaireId, user, districtCode, schoolId);
    }

    /**
     * 保存用户答案
     */
    @PostMapping("save")
    public Boolean saveUserAnswer(@RequestBody UserAnswerDTO requestDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.saveUserAnswer(requestDTO, user);
    }

    /**
     * 是否完成问卷
     */
    @GetMapping("isFinish")
    public Boolean userAnswerIsFinish() {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.userAnswerIsFinish(user);
    }

    /**
     * 获取用户状态
     */
    @GetMapping("getUserStatus")
    public void getUserStatus() {
        CurrentUserUtil.getCurrentUser();
    }

    /**
     * 获取学校名称
     *
     * @return 学校名称
     */
    @GetMapping("getSchoolName")
    public ApiResult<String> getSchoolName() {
        return ApiResult.success(userAnswerBizService.getSchoolName(CurrentUserUtil.getCurrentUser()));
    }

    /**
     * 问卷是否完成
     *
     * @param questionnaireId 问卷Id
     *
     * @return UserAnswerDTO
     */
    @GetMapping("govAndSchool/isFinish")
    public Boolean questionnaireIsFinish(Integer questionnaireId, Long districtCode, Integer schoolId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.questionnaireIsFinish(questionnaireId, user, districtCode, schoolId);
    }

    /**
     * 获取学校
     */
    @GetMapping("schoolList")
    public List<SchoolListResponseDTO> getSchoolList(String name) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.getSchoolList(name, currentUser);
    }

    /**
     * 获取行政区域
     *
     * @return List<District>
     */
    @GetMapping("getDistrict")
    public List<District> getDistrict(Integer schoolId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.getDistrict(user, schoolId);
    }

    /**
     * 获取学校信息
     *
     * @return SchoolListResponseDTO
     */
    @GetMapping("getSchoolInfo")
    public SchoolListResponseDTO getSchoolInfo() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.getSchoolInfo(currentUser);
    }

    /**
     * 获取政府人员下属行政区域
     *
     * @return List<District>
     */
    @GetMapping("gov/nextDistrict")
    public List<District> govNextDistrict() {
        return userAnswerBizService.govNextDistrict(CurrentUserUtil.getCurrentUser());
    }

    /**
     * 获取政府人员当前行政区域
     *
     * @return List<District>
     */
    @GetMapping("gov/districtDetail")
    public List<District> govDistrictDetail() {
        return userAnswerBizService.govDistrictDetail(CurrentUserUtil.getCurrentUser());
    }

    /**
     * 获取学校编码
     *
     * @param districtAreaCode 区/镇/县的行政区域编号，如：210103000
     * @param areaType         片区类型，如：2-中片区
     * @param monitorType      监测点类型，如：1-城区
     *
     * @return 学校编码
     */
    @GetMapping("/getLatestSchoolNo")
    public ApiResult<String> getLatestSchoolNo(@NotBlank(message = "districtAreaCode不能为空") @Length(min = 9, max = 9, message = "无效districtAreaCode") String districtAreaCode,
                                               @NotNull(message = "areaType不能为空") @Max(value = 3, message = "无效areaType") Integer areaType,
                                               @NotNull(message = "monitorType不能为空") @Max(value = 3, message = "无效monitorType") Integer monitorType) {
        return ApiResult.success(schoolService.getLatestSchoolNo(districtAreaCode, areaType, monitorType));
    }

}
