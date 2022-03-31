package com.wupol.myopia.migrate.service.migrate;

import cn.hutool.core.util.IdcardUtil;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.OtherEyeDiseasesDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 迁移筛查数据
 *
 * @Author HaoHao
 * @Date 2022/3/30
 **/
@Service
public class MigrateScreeningDataService {

    @Autowired
    private VisionScreeningBizService visionScreeningBizService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    /**
     * 迁移筛查结果数据
     *
     * @param sysStudentEyeList
     * @param schoolId
     * @param screeningOrgId
     * @param userId
     * @param planId
     * @param sysStudentIdAndScreeningCodeMap
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    public void migrateScreeningResult(List<SysStudentEye> sysStudentEyeList, Integer schoolId, Integer screeningOrgId, Integer userId, Integer planId,
                                        Map<String, String> sysStudentIdAndScreeningCodeMap) {
        List<ScreeningPlanSchoolStudent> planStudentList = screeningPlanSchoolStudentService.findByList(new ScreeningPlanSchoolStudent().setScreeningPlanId(planId).setSchoolId(schoolId));
        Map<String, Integer> certificateAndPlanStudentIdMap = planStudentList.stream().collect(Collectors.toMap(x -> StringUtils.isNotBlank(x.getIdCard()) ? x.getIdCard() : String.valueOf(x.getScreeningCode()), ScreeningPlanSchoolStudent::getId));
        // 遍历逐个学生迁移筛查数据
        sysStudentEyeList.forEach(sysStudentEye -> {
            String planStudentId = String.valueOf(getPlanStudentId(sysStudentEye, sysStudentIdAndScreeningCodeMap, certificateAndPlanStudentIdMap));
            // 视力
            migrateVisionData(schoolId, screeningOrgId, userId, planStudentId, sysStudentEye);
            // 屈光
            migrateComputerOptometryData(schoolId, screeningOrgId, userId, planStudentId, sysStudentEye);
            // 生物测量

            // 其他眼病
            migrateOtherEyeDiseases(schoolId, screeningOrgId, userId, planStudentId, sysStudentEye);
        });
    }

    /**
     * 视力
     *
     * @param schoolId
     * @param screeningOrgId
     * @param userId
     * @param planStudentId
     * @param sysStudentEye
     * @return void
     **/
    private void migrateVisionData(Integer schoolId, Integer screeningOrgId, Integer userId, String planStudentId, SysStudentEye sysStudentEye) {
        VisionDataDTO visionDataDTO = new VisionDataDTO();
        visionDataDTO.setSchoolId(String.valueOf(schoolId))
                .setDeptId(screeningOrgId)
                .setCreateUserId(userId)
                .setPlanStudentId(planStudentId)
                .setIsState(0);
        visionDataDTO.setLeftNakedVision(new BigDecimal(sysStudentEye.getLLsl()))
                .setLeftCorrectedVision(new BigDecimal(sysStudentEye.getLJzsl()))
                .setRightNakedVision(new BigDecimal(sysStudentEye.getRLsl()))
                .setRightCorrectedVision(new BigDecimal(sysStudentEye.getRJzsl()))
                .setGlassesType(StringUtils.isNotBlank(sysStudentEye.getGlasses()) ? sysStudentEye.getGlasses() : WearingGlassesSituation.NOT_WEARING_GLASSES_TYPE)
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
     * @return void
     **/
    private void migrateComputerOptometryData(Integer schoolId, Integer screeningOrgId, Integer userId, String planStudentId, SysStudentEye sysStudentEye) {
        ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
        computerOptometryDTO.setSchoolId(String.valueOf(schoolId))
                .setDeptId(screeningOrgId)
                .setCreateUserId(userId)
                .setPlanStudentId(planStudentId)
                .setIsState(0);
        computerOptometryDTO.setLSph(new BigDecimal(sysStudentEye.getLSph()))
                .setLCyl(new BigDecimal(sysStudentEye.getLCyl()))
                .setLAxial(new BigDecimal(sysStudentEye.getLAxial()))
                .setRSph(new BigDecimal(sysStudentEye.getRSph()))
                .setRCyl(new BigDecimal(sysStudentEye.getRCyl()))
                .setRAxial(new BigDecimal(sysStudentEye.getRAxial()));
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
     * @return void
     **/
    private void migrateOtherEyeDiseases(Integer schoolId, Integer screeningOrgId, Integer userId, String planStudentId, SysStudentEye sysStudentEye) {
        OtherEyeDiseasesDTO otherEyeDiseasesDTO = new OtherEyeDiseasesDTO();
        otherEyeDiseasesDTO.setSchoolId(String.valueOf(schoolId))
                .setDeptId(screeningOrgId)
                .setCreateUserId(userId)
                .setPlanStudentId(planStudentId)
                .setIsState(0);
        otherEyeDiseasesDTO.setLDiseaseStr(sysStudentEye.getLDisease())
                .setRDiseaseStr(sysStudentEye.getRDisease());
        visionScreeningBizService.saveOrUpdateStudentScreenData(otherEyeDiseasesDTO);
    }

    /**
     * 获取筛查计划学生ID
     *
     * @param sysStudentEye
     * @param sysStudentIdAndScreeningCodeMap
     * @param certificateAndPlanStudentIdMap
     * @return java.lang.Integer
     **/
    private Integer getPlanStudentId(SysStudentEye sysStudentEye, Map<String, String> sysStudentIdAndScreeningCodeMap, Map<String, Integer> certificateAndPlanStudentIdMap) {
        if (StringUtils.isNotBlank(sysStudentEye.getStudentIdcard()) && IdcardUtil.isValidCard(sysStudentEye.getStudentIdcard())) {
            return certificateAndPlanStudentIdMap.get(sysStudentEye.getStudentIdcard());
        }
        return certificateAndPlanStudentIdMap.get(sysStudentIdAndScreeningCodeMap.get(sysStudentEye.getStudentId()));
    }

}
