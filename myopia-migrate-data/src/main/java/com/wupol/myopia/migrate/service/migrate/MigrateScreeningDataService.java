package com.wupol.myopia.migrate.service.migrate;

import cn.hutool.core.util.IdcardUtil;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.OtherEyeDiseasesDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.migrate.domain.dos.ScreeningDataDO;
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
 * 注意：
 *      1.如何合并生物测量数据？
 *      2.如何合并常见病筛查数据？新的筛查计划？
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
     * 逐个学校迁移筛查数据
     *
     * @param screeningDataList 筛查数据（多个计划的）
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    public void migrateScreeningDataBySchool(List<ScreeningDataDO> screeningDataList) {
        screeningDataList.forEach(screeningDataDO -> migrateScreeningDataByStudent(screeningDataDO.getSysStudentEyeList(),
                screeningDataDO.getSchoolId(),
                screeningDataDO.getScreeningOrgId(),
                screeningDataDO.getScreeningStaffUserId(),
                screeningDataDO.getPlanId(),
                screeningDataDO.getSysStudentIdAndScreeningCodeMap()));
    }

    /**
     * 逐个学生迁移筛查结果数据
     *
     * @param sysStudentEyeList                 待迁移学生筛查数据（同个学校的）
     * @param schoolId                          新的学校ID
     * @param screeningOrgId                    新的筛查机构ID
     * @param screeningStaffUserId              筛查人员用户ID
     * @param planId                            筛查计划ID
     * @param sysStudentIdAndScreeningCodeMap   学生ID和学生筛查编号对应 map
     * @return void
     **/
    private void migrateScreeningDataByStudent(List<SysStudentEye> sysStudentEyeList, Integer schoolId, Integer screeningOrgId,
                                               Integer screeningStaffUserId, Integer planId,
                                               Map<String, String> sysStudentIdAndScreeningCodeMap) {
        List<ScreeningPlanSchoolStudent> planStudentList = screeningPlanSchoolStudentService.findByList(new ScreeningPlanSchoolStudent().setScreeningPlanId(planId).setSchoolId(schoolId));
        Map<String, Integer> certificateAndPlanStudentIdMap = planStudentList.stream().collect(Collectors.toMap(x -> StringUtils.isNotBlank(x.getIdCard()) ? x.getIdCard() : String.valueOf(x.getScreeningCode()), ScreeningPlanSchoolStudent::getId));
        // 遍历逐个学生迁移筛查数据
        sysStudentEyeList.forEach(sysStudentEye -> {
            String planStudentId = String.valueOf(getPlanStudentId(sysStudentEye, sysStudentIdAndScreeningCodeMap, certificateAndPlanStudentIdMap));
            // 视力
            migrateVisionData(schoolId, screeningOrgId, screeningStaffUserId, planStudentId, sysStudentEye);
            // 屈光
            migrateComputerOptometryData(schoolId, screeningOrgId, screeningStaffUserId, planStudentId, sysStudentEye);
            // TODO: 生物测量

            // 其他眼病
            migrateOtherEyeDiseases(schoolId, screeningOrgId, screeningStaffUserId, planStudentId, sysStudentEye);

            // TODO：复测
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
