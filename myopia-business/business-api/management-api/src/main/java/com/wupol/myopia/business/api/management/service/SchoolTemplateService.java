package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SchoolResultTemplateExcel;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SchoolResultTemplateImportEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学校筛查数据模板
 *
 * @author Simple4H
 */
@Service
public class SchoolTemplateService {

    @Resource
    private VisionScreeningBizService visionScreeningBizService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    /**
     * 导入筛查数据
     *
     * @param templateExcels 筛查数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void importSchoolScreeningData(List<SchoolResultTemplateExcel> templateExcels) {
        Integer userId = -1;
        SchoolResultTemplateExcel resultTemplateExcel = templateExcels.get(0);
        ScreeningPlanSchoolStudent planSchoolStudent = screeningPlanSchoolStudentService.getById(Integer.valueOf(resultTemplateExcel.getPlanStudentId()));
        templateExcels.forEach(templateExcel -> {
            generateHeightAndWeight(templateExcel, planSchoolStudent.getScreeningOrgId(), planSchoolStudent.getSchoolId(), userId);
            generateVisionData(templateExcel, planSchoolStudent.getScreeningOrgId(), planSchoolStudent.getSchoolId(), userId);
            generateComputerOptometry(templateExcel, planSchoolStudent.getScreeningOrgId(), planSchoolStudent.getSchoolId(), userId);
        });
    }

    /**
     * 解析Excel数据
     *
     * @param listMap Excel数据
     *
     * @return List<SchoolResultTemplateExcel>
     */
    public List<SchoolResultTemplateExcel> parseExcelData(List<Map<Integer, String>> listMap) {
        List<SchoolResultTemplateExcel> templateExcels = listMap.stream().map(s -> {
            SchoolResultTemplateExcel resultExcelData = new SchoolResultTemplateExcel();
            resultExcelData.setPlanStudentId(s.get(SchoolResultTemplateImportEnum.PLAN_STUDENT_ID.getIndex()));
            resultExcelData.setGlassesType(s.get(SchoolResultTemplateImportEnum.GLASSES_TYPE.getIndex()));
            resultExcelData.setRightNakedVision(s.get(SchoolResultTemplateImportEnum.RIGHT_NAKED_VISION.getIndex()));
            resultExcelData.setLeftNakedVision(s.get(SchoolResultTemplateImportEnum.LEFT_NAKED_VISION.getIndex()));
            resultExcelData.setRightCorrection(s.get(SchoolResultTemplateImportEnum.RIGHT_CORRECTION.getIndex()));
            resultExcelData.setLeftCorrection(s.get(SchoolResultTemplateImportEnum.LEFT_CORRECTION.getIndex()));
            resultExcelData.setRightSph(s.get(SchoolResultTemplateImportEnum.RIGHT_SPH.getIndex()));
            resultExcelData.setRightCyl(s.get(SchoolResultTemplateImportEnum.RIGHT_CYL.getIndex()));
            resultExcelData.setRightAxial(s.get(SchoolResultTemplateImportEnum.RIGHT_AXIAL.getIndex()));
            resultExcelData.setLeftSph(s.get(SchoolResultTemplateImportEnum.LEFT_SPH.getIndex()));
            resultExcelData.setLeftCyl(s.get(SchoolResultTemplateImportEnum.LEFT_CYL.getIndex()));
            resultExcelData.setLeftAxial(s.get(SchoolResultTemplateImportEnum.LEFT_AXIAL.getIndex()));
            resultExcelData.setHeight(s.get(SchoolResultTemplateImportEnum.HEIGHT.getIndex()));
            resultExcelData.setWeight(s.get(SchoolResultTemplateImportEnum.WEIGHT.getIndex()));
            return resultExcelData;
        }).collect(Collectors.toList());
        preCheckData(templateExcels);
        return templateExcels;
    }

    /**
     * @param templateExcels 数据
     */
    private void preCheckData(List<SchoolResultTemplateExcel> templateExcels) {
        List<Integer> planStudentIds = templateExcels.stream().map(s -> Integer.valueOf(s.getPlanStudentId())).collect(Collectors.toList());
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByIds(planStudentIds);
        if (Objects.equals(planStudentIds.size(), planSchoolStudentList.size())) {
            throw new BusinessException("筛查学生数据异常");
        }

        templateExcels.forEach(s -> {
            if (!StringUtils.isAllBlank(s.getGlassesType(), s.getRightNakedVision(), s.getLeftNakedVision())) {
                if (StringUtils.isAnyBlank(s.getGlassesType(), s.getRightNakedVision(), s.getLeftNakedVision())) {
                    throw new BusinessException("视力数据异常");
                }
                WearingGlassesSituation.checkKeyByDesc(s.getGlassesType());
            }
            if (!StringUtils.isAllBlank(s.getLeftSph(), s.getLeftCyl(), s.getLeftAxial(), s.getRightSph(), s.getRightCyl(), s.getRightAxial())) {
                if (StringUtils.isAnyBlank(s.getLeftSph(), s.getLeftCyl(), s.getLeftAxial(), s.getRightSph(), s.getRightCyl(), s.getRightAxial())) {
                    throw new BusinessException("电脑验光数据数据异常");
                }
            }
            if (!StringUtils.isAllBlank(s.getHeight(), s.getWeight())) {
                if (StringUtils.isAnyBlank(s.getHeight(), s.getWeight())) {
                    throw new BusinessException("体重数据数据异常");
                }
            }
        });
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
        visionDataDTO.setRightNakedVision(new BigDecimal(data.getRightNakedVision()));
        visionDataDTO.setRightCorrectedVision(new BigDecimal(data.getRightCorrection()));
        visionDataDTO.setLeftNakedVision(new BigDecimal(data.getLeftNakedVision()));
        visionDataDTO.setLeftCorrectedVision(new BigDecimal(data.getLeftCorrection()));
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
