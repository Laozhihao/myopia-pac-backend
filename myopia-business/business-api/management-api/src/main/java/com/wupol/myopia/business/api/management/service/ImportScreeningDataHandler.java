package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SchoolResultTemplateExcel;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.system.service.NoticeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * 导入筛查数据类
 *
 * @Author lzh
 * @Date 2023/4/24
 **/
@Service
public class ImportScreeningDataHandler {

    @Resource
    private NoticeService noticeService;
    @Resource
    private VisionScreeningBizService visionScreeningBizService;

    /**
     * 导入筛查数据
     *
     * @param templateExcels    上传的Excel数据
     * @param userId            当前用户ID
     * @param plan              筛查计划
     * @param school            学校
     */
    @Transactional(rollbackFor = Exception.class)
    public void action(List<SchoolResultTemplateExcel> templateExcels, Integer userId, ScreeningPlan plan, School school) {
        Integer schoolId = school.getId();
        Integer screeningOrgId = plan.getScreeningOrgId();
        templateExcels.forEach(templateExcel -> {
            generateHeightAndWeight(templateExcel, screeningOrgId, schoolId, userId);
            generateVisionData(templateExcel, screeningOrgId, schoolId, userId);
            generateComputerOptometry(templateExcel, screeningOrgId, schoolId, userId);
        });
        String content = String.format(CommonConst.SCHOOL_TEMPLATE_EXCEL_IMPORT_SUCCESS, plan.getTitle(), school.getName());
        noticeService.createExportNotice(userId, userId, content, content, null, CommonConst.NOTICE_STATION_LETTER);
    }


    /**
     * 生成身高体重信息
     *
     * @param data     数据
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @param userId   用户Id
     */
    private void generateHeightAndWeight(SchoolResultTemplateExcel data, Integer orgId, Integer schoolId, Integer userId) {
        if (StringUtils.isBlank(data.getHeight())) {
            return;
        }
        HeightAndWeightDataDTO heightAndWeightDataDTO = new HeightAndWeightDataDTO();
        heightAndWeightDataDTO.setHeight(new BigDecimal(data.getHeight()).setScale(1, RoundingMode.DOWN));
        heightAndWeightDataDTO.setWeight(new BigDecimal(data.getWeight()).setScale(1, RoundingMode.DOWN));
        heightAndWeightDataDTO.setBmi(heightAndWeightDataDTO.getWeight().divide(heightAndWeightDataDTO.getHeight().multiply(heightAndWeightDataDTO.getHeight()), 1, RoundingMode.DOWN));
        heightAndWeightDataDTO.setDeptId(orgId);
        heightAndWeightDataDTO.setCreateUserId(userId);
        heightAndWeightDataDTO.setPlanStudentId(data.getPlanStudentId());
        heightAndWeightDataDTO.setSchoolId(String.valueOf(schoolId));
        visionScreeningBizService.saveOrUpdateStudentScreenData(heightAndWeightDataDTO);
    }

    /**
     * 生成视力信息
     *
     * @param data     数据
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @param userId   用户Id
     */
    private void generateVisionData(SchoolResultTemplateExcel data, Integer orgId, Integer schoolId, Integer userId) {
        if (StringUtils.isBlank(data.getGlassesType())) {
            return;
        }
        VisionDataDTO visionDataDTO = new VisionDataDTO();
        visionDataDTO.setRightNakedVision(Optional.ofNullable(data.getRightNakedVision()).map(BigDecimal::new).orElse(null));
        visionDataDTO.setLeftNakedVision(Optional.ofNullable(data.getLeftNakedVision()).map(BigDecimal::new).orElse(null));
        visionDataDTO.setRightCorrectedVision(Optional.ofNullable(data.getRightCorrection()).map(BigDecimal::new).orElse(null));
        visionDataDTO.setLeftCorrectedVision(Optional.ofNullable(data.getLeftCorrection()).map(BigDecimal::new).orElse(null));
        visionDataDTO.setIsCooperative(0);
        visionDataDTO.setDeptId(orgId);
        visionDataDTO.setCreateUserId(userId);
        visionDataDTO.setPlanStudentId(data.getPlanStudentId());
        visionDataDTO.setSchoolId(String.valueOf(schoolId));
        visionDataDTO.setGlassesType(data.getGlassesType());
        visionScreeningBizService.saveOrUpdateStudentScreenData(visionDataDTO);
    }

    /**
     * 生成电脑验光信息
     *
     * @param data     数据
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @param userId   用户Id
     */
    private void generateComputerOptometry(SchoolResultTemplateExcel data, Integer orgId, Integer schoolId, Integer userId) {
        if (StringUtils.isBlank(data.getRightSph())) {
            return;
        }
        ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
        computerOptometryDTO.setLSph(new BigDecimal(replacePlusChar(data.getLeftSph())));
        computerOptometryDTO.setLCyl(new BigDecimal(replacePlusChar(data.getLeftCyl())));
        computerOptometryDTO.setLAxial(new BigDecimal(data.getLeftAxial()));
        computerOptometryDTO.setRSph(new BigDecimal(replacePlusChar(data.getRightSph())));
        computerOptometryDTO.setRCyl(new BigDecimal(replacePlusChar(data.getRightCyl())));
        computerOptometryDTO.setRAxial(new BigDecimal(data.getRightAxial()));
        computerOptometryDTO.setIsCooperative(0);
        computerOptometryDTO.setSchoolId(String.valueOf(schoolId));
        computerOptometryDTO.setDeptId(orgId);
        computerOptometryDTO.setCreateUserId(userId);
        computerOptometryDTO.setPlanStudentId(data.getPlanStudentId());
        computerOptometryDTO.setIsState(0);
        visionScreeningBizService.saveOrUpdateStudentScreenData(computerOptometryDTO);
    }

    /**
     * 除去+号
     *
     * @param val 值
     *
     * @return String
     */
    private String replacePlusChar(String val) {
        return StringUtils.replace(val, "+", "");
    }

}
