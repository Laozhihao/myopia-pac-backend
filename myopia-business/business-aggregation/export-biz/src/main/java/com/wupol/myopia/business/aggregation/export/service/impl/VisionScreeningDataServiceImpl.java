package com.wupol.myopia.business.aggregation.export.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.base.util.ScreeningDataFormatUtils;
import com.wupol.myopia.business.aggregation.export.service.IScreeningDataService;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningResultPahtConst;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultExportDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 视力筛查实现
 *
 * @author Simple4H
 */
@Service
@Slf4j
public class VisionScreeningDataServiceImpl implements IScreeningDataService {

    @Resource
    private DistrictService districtService;


    @Override
    public List generateExportData(List<StatConclusionExportDTO> statConclusionExportDTOs,Boolean isHaiNan) {
        Map<Boolean, List<StatConclusionExportDTO>> isRescreenMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getIsRescreen));
        Map<Integer, StatConclusionExportDTO> rescreenPlanStudentIdVoMap = isRescreenMap.getOrDefault(true, Collections.emptyList()).stream().collect(Collectors.toMap(StatConclusionExportDTO::getScreeningPlanSchoolStudentId, Function.identity(), (x, y) -> x));
        List<VisionScreeningResultExportDTO> exportVos = new ArrayList<>();
        List<StatConclusionExportDTO> vos = isRescreenMap.getOrDefault(false, Collections.emptyList());
        for (StatConclusionExportDTO vo : vos) {
            VisionScreeningResultExportDTO exportVo = new VisionScreeningResultExportDTO();
            BeanUtils.copyProperties(vo, exportVo);
            // 基本信息
            exportVo.setGenderDesc(GenderEnum.getName(vo.getGender()))
                    .setNationDesc(StringUtils.defaultString(NationEnum.getNameByCode(vo.getNation())))
                    .setGlassesTypeDesc(StringUtils.defaultIfBlank(GlassesTypeEnum.getDescByCode(vo.getGlassesType()), "--"))
                    .setIsRescreenDesc("否").setWarningLevelDesc(StringUtils.defaultIfBlank(WarningLevel.getDescByCode(vo.getWarningLevel()), "--"))
                    .setAddress(districtService.getAddressDetails(vo.getProvinceCode(), vo.getCityCode(), vo.getAreaCode(), vo.getTownCode(), vo.getAddress()))
                    .setIsValid(Boolean.TRUE.equals(vo.getIsValid()) ? "有效" : "无效");
            // 视力检查、电脑验光
            genScreeningData(vo, exportVo);
            // 组装复筛数据
            genReScreeningData(rescreenPlanStudentIdVoMap, vo, exportVo);
            // 33cm眼位、裂隙灯、小瞳验光、生物测量、眼压、其他眼病、体测检查
            generateDate(vo, exportVo,isHaiNan);
            exportVos.add(exportVo);
        }
        return exportVos;
    }

    @Override
    public Integer getScreeningType() {
        return ScreeningTypeEnum.VISION.getType();
    }

    @Override
    public Class getExportClass() {
        return VisionScreeningResultExportDTO.class;
    }


    /**
     * 组装初筛数据
     *
     * @param dto       处理后筛查数据
     * @param exportDTO 筛查数据导出
     */
    private void genScreeningData(StatConclusionExportDTO dto, VisionScreeningResultExportDTO exportDTO) {
        exportDTO.setLeftNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION),1))
                .setRightNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION),1))
                .setLeftCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CORRECTED_VISION),1))
                .setRightCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CORRECTED_VISION),1))
                .setRightSphs(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_SPH)))
                .setLeftSphs(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_SPH)))
                .setRightCyls(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CYL)))
                .setLeftCyls(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CYL)))
                .setRightAxials(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_AXIAL)))
                .setLeftAxials(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_AXIAL)));
    }

    /**
     * 组装复筛数据
     *
     * @param rescreenPlanStudentIdDTOMap 复筛学生信息
     * @param dto                         处理后筛查数据
     * @param exportDTO                   筛查数据导出
     */
    private void genReScreeningData(Map<Integer, StatConclusionExportDTO> rescreenPlanStudentIdDTOMap, StatConclusionExportDTO dto, VisionScreeningResultExportDTO exportDTO) {
        StatConclusionExportDTO rescreenVo = rescreenPlanStudentIdDTOMap.get(dto.getScreeningPlanSchoolStudentId());
        if (Objects.nonNull(rescreenVo)) {
            exportDTO.setReScreenGlassesTypeDesc(ScreeningDataFormatUtils.getGlassesType(JSONPath.eval(rescreenVo, ScreeningResultPahtConst.PATH_GLASSES_TYPE)));
            exportDTO.setLeftReScreenNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION),1));
            exportDTO.setRightReScreenNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION),1));
            exportDTO.setLeftReScreenCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_CORRECTED_VISION),1));
            exportDTO.setRightReScreenCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_CORRECTED_VISION),1));
            exportDTO.setLeftReScreenSphs(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_SPH)));
            exportDTO.setRightReScreenSphs(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_SPH)));
            exportDTO.setLeftReScreenCyls(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_CYL)));
            exportDTO.setRightReScreenCyls(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_CYL)));
            exportDTO.setLeftReScreenAxials(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_AXIAL)));
            exportDTO.setRightReScreenAxials(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_AXIAL)));
            exportDTO.setIsRescreenDesc("是");
        }
    }

    /**
     * 生成Excel数据
     *
     * @param dto       处理后筛查数据
     * @param exportDTO 筛查数据导出
     */
    private void generateDate(StatConclusionExportDTO dto, VisionScreeningResultExportDTO exportDTO,Boolean isHaiNan) {
        exportDTO.setOcularInspectionSotropia(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OID_ESOTROPIA)));
        exportDTO.setOcularInspectionXotropia(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OID_EXOTROPIA)));
        exportDTO.setOcularInspectionVerticalStrabismus(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OID_VERTICAL_STRABISMUS)));

        exportDTO.setSlitLampLeftEye(ListUtil.objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SLD_LEFT_PATHOLOGICAL_TISSUES)));
        exportDTO.setSlitLampRightEye(ListUtil.objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SLD_RIGHT_PATHOLOGICAL_TISSUES)));

        exportDTO.setLeftPupilOptometrySph(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_SPN)));
        exportDTO.setRightPupilOptometrySph(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_SPN)));
        exportDTO.setLeftPupilOptometryCyl(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_CYL)));
        exportDTO.setRightPupilOptometryCyl(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_CYL)));
        exportDTO.setLeftPupilOptometryAxial(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_AXIAL)));
        exportDTO.setRightPupilOptometryAxial(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_AXIAL)));
        exportDTO.setLeftPupilOptometryCorrectedVision(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_CORRECTEDVISION),1));
        exportDTO.setRightPupilOptometryCorrectedVision(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_CORRECTEDVISION),1));

        exportDTO.setLeftBiometricK1(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K1)));
        exportDTO.setLeftBiometricK1Axis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K1_AXIS)));
        exportDTO.setRightBiometricK1(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K1)));
        exportDTO.setRightBiometricK1Axis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K1_AXIS)));
        exportDTO.setLeftBiometricK2(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K2)));
        exportDTO.setLeftBiometricK2Axis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K2_AXIS)));
        exportDTO.setRightBiometricK2(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K2)));
        exportDTO.setRightBiometricK2Axis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K2_AXIS)));
        exportDTO.setLeftBiometricAST(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_AST)));
        exportDTO.setLeftBiometricASTAxis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K1_AXIS)));
        exportDTO.setRightBiometricAST(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_AST)));
        exportDTO.setRightBiometricASTAxis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K1_AXIS)));
        exportDTO.setLeftBiometricPD(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_LEFT_PD));
        exportDTO.setRightBiometricPD(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_PD));
        exportDTO.setLeftBiometricWTW(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_LEFT_WTW));
        exportDTO.setRightBiometricWTW(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_WTW));
        exportDTO.setLeftBiometricAL(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_LEFT_AL));
        exportDTO.setRightBiometricAL(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_AL));
        exportDTO.setLeftBiometricAD(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_LEFT_AD));
        exportDTO.setRightBiometricAD(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_AD));
        exportDTO.setLeftBiometricLT(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_LEFT_LT));
        exportDTO.setRightBiometricLT(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_LT));
        exportDTO.setLeftBiometricVT(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_LEFT_VT));
        exportDTO.setRightBiometricVT(parseWithSuffix(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_VT));

        exportDTO.setLeftBiometricCCT(ScreeningDataFormatUtils.generateSingleSuffixUMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_CCT)));
        exportDTO.setRightBiometricCCT(ScreeningDataFormatUtils.generateSingleSuffixUMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_CCT)));

        exportDTO.setLeftEyePressureDate(ScreeningDataFormatUtils.ipDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_IPD_LEFT_PRESSURE)));
        exportDTO.setRightEyePressureDate(ScreeningDataFormatUtils.ipDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_IPD_RIGHT_PRESSURE)));
        exportDTO.setLeftFundusData(ScreeningDataFormatUtils.singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_DF_LEFT_HASABNORMAL)));
        exportDTO.setRightFundusData(ScreeningDataFormatUtils.singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_DF_RIGHT_HASABNORMAL)));

        exportDTO.setOtherEyeDiseasesLeftEyeDiseases(ListUtil.objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OED_LEFT_EYE_DISEASES)));
        exportDTO.setOtherEyeDiseasesRightEyeDiseases(ListUtil.objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OED_RIGHT_EYE_DISEASES)));
        exportDTO.setOtherEyeDiseasesSystemicDiseaseSymptom((String) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SYSTEMIC_DISEASE_SYMPTOM));
        exportDTO.setLeftOtherEyeDiseasesLevel(ScreeningDataFormatUtils.levelDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_VLLD_LEFT_LEVEL),isHaiNan));
        exportDTO.setRightOtherEyeDiseasesLevel(ScreeningDataFormatUtils.levelDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_VLLD_RIGHT_LEVEL),isHaiNan));

        exportDTO.setHeight(ScreeningDataFormatUtils.getHeight(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_HW_HEIGHT)));
        exportDTO.setWeight(ScreeningDataFormatUtils.getWeight(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_HW_WEIGHT)));
    }

    private String parseWithSuffix(StatConclusionExportDTO dto, String path) {
        Object eval = JSONPath.eval(dto, path);
        try {
            return ScreeningDataFormatUtils.generateSingleSuffixMMStr(eval);
        } catch (Exception e) {
            log.error("导出筛查数据异常！原始信息:{}", JSON.toJSONString(dto));
            return (String) eval;
        }
    }
}
