package com.wupol.myopia.migrate.service.migrate;

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.OtherEyeDiseasesDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.domain.model.SysStudentEyeReview;
import com.wupol.myopia.migrate.service.SysStudentEyeReviewService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2022/5/10
 **/
@Log4j2
@Service
public class ScreeningDataService {

    @Autowired
    private VisionScreeningBizService visionScreeningBizService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private SysStudentEyeReviewService sysStudentEyeReviewService;

    /**
     * 逐个学校迁移筛查结果数据
     *
     * @param sysStudentEyeList                 待迁移学生筛查数据（同个学校的）
     * @param schoolId                          新的学校ID
     * @param screeningOrgId                    新的筛查机构ID
     * @param screeningStaffUserId              筛查人员用户ID
     * @param planId                            筛查计划ID
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void migrateScreeningDataOfSameSchool(List<SysStudentEye> sysStudentEyeList, Integer schoolId, Integer screeningOrgId,
                                                  Integer screeningStaffUserId, Integer planId) {
        log.info("【迁移筛查数据】{}......", sysStudentEyeList.get(0).getSchoolName());
        List<ScreeningPlanSchoolStudent> planStudentList = screeningPlanSchoolStudentService.findByList(new ScreeningPlanSchoolStudent().setScreeningPlanId(planId).setSchoolId(schoolId));
        Map<String, Integer> certificateIdAndPlanStudentIdMap = planStudentList.stream().collect(Collectors.toMap(x -> StringUtils.isNotBlank(x.getIdCard()) ? x.getIdCard() : x.getMigrateStudentScreeningId(), ScreeningPlanSchoolStudent::getId));
        // 遍历逐个学生迁移筛查数据
        sysStudentEyeList.forEach(sysStudentEye -> {
            Integer planStudentId = getPlanStudentId(sysStudentEye, certificateIdAndPlanStudentIdMap);
            if (Objects.isNull(planStudentId)) {
                return;
            }
            String planStudentIdStr = String.valueOf(planStudentId);
            try {
                // 初筛
                migrateScreeningData(schoolId, screeningOrgId, screeningStaffUserId, planStudentIdStr, sysStudentEye, 0, planStudentId);
                // 复测
                migrateReviewScreeningData(schoolId, screeningOrgId, screeningStaffUserId, planStudentIdStr, sysStudentEye, planStudentId);
            } catch (Exception e) {
                log.error("数据迁移异常：{}" + JSONObject.toJSONString(sysStudentEye));
                throw new BusinessException("数据迁移异常", e);
            }
        });
    }

    /**
     * 迁移筛查数据
     *
     * @param schoolId
     * @param screeningOrgId
     * @param screeningStaffUserId
     * @param planStudentIdStr
     * @param sysStudentEye
     * @param isDoubleScreen
     * @param planStudentId
     */
    private void migrateScreeningData(Integer schoolId, Integer screeningOrgId, Integer screeningStaffUserId, String planStudentIdStr, SysStudentEye sysStudentEye, int isDoubleScreen, Integer planStudentId) {
        // 视力
        migrateVisionData(schoolId, screeningOrgId, screeningStaffUserId, planStudentIdStr, sysStudentEye, isDoubleScreen);
        // 屈光
        migrateComputerOptometryData(schoolId, screeningOrgId, screeningStaffUserId, planStudentIdStr, sysStudentEye, isDoubleScreen);
        // 其他眼病F
        migrateOtherEyeDiseases(schoolId, screeningOrgId, screeningStaffUserId, planStudentIdStr, sysStudentEye, isDoubleScreen);
        // 更新创建时间为实际时间
        VisionScreeningResult visionScreeningResult = visionScreeningResultService.findOne(new VisionScreeningResult().setScreeningPlanSchoolStudentId(planStudentId).setIsDoubleScreen(isDoubleScreen == 1));
        if (Objects.isNull(visionScreeningResult)) {
            return;
        }
        visionScreeningResultService.updateById(new VisionScreeningResult().setId(visionScreeningResult.getId()).setCreateTime(sysStudentEye.getCreateTime()));
        statConclusionService.update(new StatConclusion().setCreateTime(sysStudentEye.getCreateTime()), new StatConclusion().setResultId(visionScreeningResult.getId()));
        // 更新最近筛查时间为实际时间
        studentService.updateById(new Student().setLastScreeningTime(sysStudentEye.getCreateTime()).setId(visionScreeningResult.getStudentId()));
    }

    /**
     * 迁移复测数据
     *
     * @param schoolId
     * @param screeningOrgId
     * @param screeningStaffUserId
     * @param planStudentIdStr
     * @param firstScreeningData
     * @param planStudentId
     */
    private void migrateReviewScreeningData(Integer schoolId, Integer screeningOrgId, Integer screeningStaffUserId, String planStudentIdStr, SysStudentEye firstScreeningData, Integer planStudentId) {
        // 获取复测数据
        SysStudentEye reviewData = sysStudentEyeReviewService.getOneStudentReview(new SysStudentEyeReview().setStudentId(firstScreeningData.getStudentId()).setSchoolId(firstScreeningData.getSchoolId()).setDeptId(firstScreeningData.getDeptId()).setCreateTime(firstScreeningData.getCreateTime()));
        if (Objects.isNull(reviewData)) {
            return;
        }
        // 迁移复测数据
        migrateScreeningData(schoolId, screeningOrgId, screeningStaffUserId, planStudentIdStr, reviewData, 1, planStudentId);
    }

    /**
     * 视力
     *
     * @param schoolId
     * @param screeningOrgId
     * @param userId
     * @param planStudentId
     * @param sysStudentEye
     * @param isDoubleScreen
     * @return void
     **/
    private void migrateVisionData(Integer schoolId, Integer screeningOrgId, Integer userId, String planStudentId, SysStudentEye sysStudentEye, int isDoubleScreen) {
        if (StringUtils.isAllBlank(sysStudentEye.getLLsl(), sysStudentEye.getLJzsl(), sysStudentEye.getRLsl(), sysStudentEye.getRJzsl())) {
            return;
        }
        VisionDataDTO visionDataDTO = new VisionDataDTO();
        setBasicInfo(visionDataDTO, schoolId, screeningOrgId, userId, planStudentId, isDoubleScreen);
        visionDataDTO.setLeftNakedVision(getBigDecimalValue(sysStudentEye.getLLsl()))
                .setLeftCorrectedVision(getBigDecimalValue(sysStudentEye.getLJzsl()))
                .setRightNakedVision(getBigDecimalValue(sysStudentEye.getRLsl()))
                .setRightCorrectedVision(getBigDecimalValue(sysStudentEye.getRJzsl()))
                .setGlassesType(getGlassesType(sysStudentEye.getGlasses()))
                .setIsCooperative(0);
        visionScreeningBizService.saveOrUpdateStudentScreenData(visionDataDTO);
    }

    /**
     * 屈光
     *
     * @param schoolId
     * @param screeningOrgId
     * @param userId
     * @param planStudentId
     * @param sysStudentEye
     * @param isDoubleScreen
     * @return void
     **/
    private void migrateComputerOptometryData(Integer schoolId, Integer screeningOrgId, Integer userId, String planStudentId, SysStudentEye sysStudentEye, int isDoubleScreen) {
        if (StringUtils.isAllBlank(sysStudentEye.getLSph(), sysStudentEye.getLCyl(), sysStudentEye.getLAxial(), sysStudentEye.getRSph(), sysStudentEye.getRCyl(), sysStudentEye.getRAxial())) {
            return;
        }
        ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
        setBasicInfo(computerOptometryDTO, schoolId, screeningOrgId, userId, planStudentId, isDoubleScreen);
        computerOptometryDTO.setLSph(getBigDecimalValue(sysStudentEye.getLSph()))
                .setLCyl(getBigDecimalValue(sysStudentEye.getLCyl()))
                .setLAxial(getBigDecimalValue(sysStudentEye.getLAxial()))
                .setRSph(getBigDecimalValue(sysStudentEye.getRSph()))
                .setRCyl(getBigDecimalValue(sysStudentEye.getRCyl()))
                .setRAxial(getBigDecimalValue(sysStudentEye.getRAxial()));
        visionScreeningBizService.saveOrUpdateStudentScreenData(computerOptometryDTO);
    }

    /**
     * 其他眼病
     *
     * @param schoolId
     * @param screeningOrgId
     * @param userId
     * @param planStudentId
     * @param sysStudentEye
     * @param isDoubleScreen
     * @return void
     **/
    private void migrateOtherEyeDiseases(Integer schoolId, Integer screeningOrgId, Integer userId, String planStudentId, SysStudentEye sysStudentEye, int isDoubleScreen) {
        if (StringUtils.isAllBlank(sysStudentEye.getLDisease(), sysStudentEye.getRDisease())) {
            return;
        }
        OtherEyeDiseasesDTO otherEyeDiseasesDTO = new OtherEyeDiseasesDTO();
        setBasicInfo(otherEyeDiseasesDTO, schoolId, screeningOrgId, userId, planStudentId, isDoubleScreen);
        otherEyeDiseasesDTO.setLDiseaseStr(sysStudentEye.getLDisease())
                .setRDiseaseStr(sysStudentEye.getRDisease());
        visionScreeningBizService.saveOrUpdateStudentScreenData(otherEyeDiseasesDTO);
    }

    /**
     * 设置基础信息
     *
     * @param screeningResultBasicData
     * @param schoolId
     * @param screeningOrgId
     * @param userId
     * @param planStudentId
     * @param isDoubleScreen
     */
    private void setBasicInfo(ScreeningResultBasicData screeningResultBasicData, Integer schoolId, Integer screeningOrgId, Integer userId, String planStudentId, int isDoubleScreen) {
        screeningResultBasicData.setSchoolId(String.valueOf(schoolId))
                .setDeptId(screeningOrgId)
                .setCreateUserId(userId)
                .setPlanStudentId(planStudentId)
                .setIsState(isDoubleScreen);
    }

    /**
     * 获取筛查计划学生ID
     *
     * @param sysStudentEye
     * @param certificateIdAndPlanStudentIdMap
     * @return java.lang.Integer
     **/
    private Integer getPlanStudentId(SysStudentEye sysStudentEye, Map<String, Integer> certificateIdAndPlanStudentIdMap) {
        if (SysStudentEye.isValidIdCard(sysStudentEye.getStudentIdcard())) {
            return certificateIdAndPlanStudentIdMap.get(sysStudentEye.getStudentIdcard().toUpperCase());
        }
        return certificateIdAndPlanStudentIdMap.get(sysStudentEye.getEyeId());
    }

    /**
     * 把字符串数值转为BigDecimal类型
     *
     * @param valStr 字符串数值
     * @return java.math.BigDecimal
     **/
    private static BigDecimal getBigDecimalValue(String valStr) {
        try {
            return StringUtils.isBlank(valStr) ? null : new BigDecimal(valStr.trim().replace("..", ".").replace("+.0", "+0").replace("-.0", "-0").replace("--", "-"));
        } catch (Exception e) {
            log.error("转换数值异常：[{}]", valStr, e);
            return null;
        }
    }

    /**
     * 获取带镜类型
     *
     * @param glassType 带镜类型
     * @return java.lang.String
     **/
    private static String getGlassesType(String glassType) {
        if (StringUtils.isBlank(glassType)) {
            return WearingGlassesSituation.NOT_WEARING_GLASSES_TYPE;
        }
        try {
            WearingGlassesSituation.getKey(glassType);
            return glassType;
        } catch (Exception e) {
            if (glassType.contains("夜戴角膜塑形")) {
                return WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_TYPE;
            }
            if (glassType.contains("框架")) {
                return WearingGlassesSituation.WEARING_FRAME_GLASSES_TYPE;
            }
            if (glassType.contains("隐形")) {
                return WearingGlassesSituation.WEARING_CONTACT_LENS_TYPE;
            }
        }
        return WearingGlassesSituation.NOT_WEARING_GLASSES_TYPE;
    }

}
