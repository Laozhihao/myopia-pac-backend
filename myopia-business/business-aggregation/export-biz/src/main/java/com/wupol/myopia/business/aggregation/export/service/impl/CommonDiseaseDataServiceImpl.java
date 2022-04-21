package com.wupol.myopia.business.aggregation.export.service.impl;

import com.alibaba.fastjson.JSONPath;
import com.wupol.myopia.base.constant.SpineLevelEnum;
import com.wupol.myopia.base.constant.SpineTypeEnum;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.base.util.ScreeningDataFormatUtils;
import com.wupol.myopia.business.aggregation.export.service.IScreeningDataService;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.constant.SaprodontiaType;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningResultPahtConst;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.CommonDiseaseDataExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.system.constants.ScreeningTypeConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 常见病
 *
 * @author Simple4H
 */
@Service
public class CommonDiseaseDataServiceImpl implements IScreeningDataService {

    @Resource
    private DistrictService districtService;


    @Override
    public List generateExportData(List<StatConclusionExportDTO> statConclusionExportDTOs) {
        Map<Boolean, List<StatConclusionExportDTO>> isRescreenMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getIsRescreen));
        Map<Integer, StatConclusionExportDTO> rescreenPlanStudentIdVoMap = isRescreenMap.getOrDefault(true, Collections.emptyList()).stream().collect(Collectors.toMap(StatConclusionExportDTO::getScreeningPlanSchoolStudentId, Function.identity(), (x, y) -> x));
        List<CommonDiseaseDataExportDTO> exportVos = new ArrayList<>();
        List<StatConclusionExportDTO> vos = isRescreenMap.getOrDefault(false, Collections.emptyList());
        for (StatConclusionExportDTO vo : vos) {
            CommonDiseaseDataExportDTO exportVo = new CommonDiseaseDataExportDTO();
            BeanUtils.copyProperties(vo, exportVo);
            // 基本信息
            exportVo.setGenderDesc(GenderEnum.getName(vo.getGender()))
                    .setNationDesc(StringUtils.defaultString(NationEnum.getName(vo.getNation())))
                    .setGlassesTypeDesc(StringUtils.defaultIfBlank(GlassesTypeEnum.getDescByCode(vo.getGlassesType()), "--"))
                    .setIsRescreenDesc("否")
                    .setAddress(districtService.getAddressDetails(vo.getProvinceCode(), vo.getCityCode(), vo.getAreaCode(), vo.getTownCode(), vo.getAddress()))
                    .setIsValid(Boolean.TRUE.equals(vo.getIsValid()) ? "有效" : "无效");
            genScreeningData(vo, exportVo);
            // 组装复筛数据
            genReScreeningData(rescreenPlanStudentIdVoMap, vo, exportVo);
            // 以下为常见病相关
            generateSaprodontiaData(vo, exportVo);
            generateSpineData(vo, exportVo);
            generateBloodPressureData(vo, exportVo);
            generatePrivacyDiseasesHistoryData(vo, exportVo);
            exportVos.add(exportVo);
        }
        return exportVos;
    }

    @Override
    public Integer getScreeningType() {
        return ScreeningTypeConst.COMMON_DISEASE;
    }

    @Override
    public Class getExportClass() {
        return CommonDiseaseDataExportDTO.class;
    }


    /**
     * 组装初筛数据
     *
     * @param dto       处理后筛查数据
     * @param exportDTO 筛查数据导出
     */
    private void genScreeningData(StatConclusionExportDTO dto, CommonDiseaseDataExportDTO exportDTO) {
        exportDTO.setLeftNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION)))
                .setRightNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION)))
                .setLeftCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CORRECTED_VISION)))
                .setRightCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CORRECTED_VISION)))
                .setRightSphs(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_SPH)))
                .setLeftSphs(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_SPH)))
                .setRightCyls(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CYL)))
                .setLeftCyls(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CYL)))
                .setRightAxials(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_AXIAL)))
                .setLeftAxials(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_AXIAL)))
                .setHeight(ScreeningDataFormatUtils.getHeight(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_HW_HEIGHT)))
                .setWeight(ScreeningDataFormatUtils.getWeight(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_HW_WEIGHT)))
                .setOtherEyeDiseasesLeftEyeDiseases(ListUtil.objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OED_LEFT_EYE_DISEASES)))
                .setOtherEyeDiseasesRightEyeDiseases(ListUtil.objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OED_RIGHT_EYE_DISEASES)))
                .setOtherEyeDiseasesSystemicDiseaseSymptom((String) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SYSTEMIC_DISEASE_SYMPTOM))
                .setLeftOtherEyeDiseasesLevel(ScreeningDataFormatUtils.levelDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_VLLD_LEFT_LEVEL)))
                .setRightOtherEyeDiseasesLevel(ScreeningDataFormatUtils.levelDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_VLLD_RIGHT_LEVEL)));
    }

    /**
     * 组装复筛数据
     *
     * @param rescreenPlanStudentIdDTOMap 复筛学生信息
     * @param dto                         处理后筛查数据
     * @param exportDTO                   筛查数据导出
     */
    private void genReScreeningData(Map<Integer, StatConclusionExportDTO> rescreenPlanStudentIdDTOMap, StatConclusionExportDTO dto, CommonDiseaseDataExportDTO exportDTO) {
        StatConclusionExportDTO rescreenVo = rescreenPlanStudentIdDTOMap.get(dto.getScreeningPlanSchoolStudentId());
        if (Objects.nonNull(rescreenVo)) {
            exportDTO.setReScreenGlassesTypeDesc(ScreeningDataFormatUtils.getGlassesType(JSONPath.eval(rescreenVo, ScreeningResultPahtConst.PATH_GLASSES_TYPE)))
                    .setLeftReScreenNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION)))
                    .setRightReScreenNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION)))
                    .setLeftReScreenCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_CORRECTED_VISION)))
                    .setRightReScreenCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_CORRECTED_VISION)))
                    .setLeftReScreenSphs(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_SPH)))
                    .setRightReScreenSphs(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_SPH)))
                    .setLeftReScreenCyls(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_CYL)))
                    .setRightReScreenCyls(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_CYL)))
                    .setLeftReScreenAxials(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_AXIAL)))
                    .setRightReScreenAxials(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_AXIAL)))
                    .setIsRescreenDesc("是")
                    .setReHeight(ScreeningDataFormatUtils.getHeight(JSONPath.eval(rescreenVo, ScreeningResultPahtConst.PATH_HW_HEIGHT)))
                    .setReWeight(ScreeningDataFormatUtils.getWeight(JSONPath.eval(rescreenVo, ScreeningResultPahtConst.PATH_HW_WEIGHT)));
        }
    }

    /**
     * 龋齿数据
     *
     * @param dto       处理后筛查数据
     * @param exportDTO 筛查数据导出
     */
    private void generateSaprodontiaData(StatConclusionExportDTO dto, CommonDiseaseDataExportDTO exportDTO) {
        SaprodontiaDataDO saprodontiaData = dto.getSaprodontiaData();
        if (Objects.isNull(saprodontiaData)) {
            return;
        }
        TwoTuple<String, String> stringStringTwoTuple = generateSaprodontiaResult(saprodontiaData);

        exportDTO.setDeciduous(stringStringTwoTuple.getFirst())
                .setPermanent(stringStringTwoTuple.getSecond());
    }

    /**
     * 生成龋齿结论
     *
     * @param saprodontiaData 龋齿数据
     * @return 结论
     */
    private TwoTuple<String, String> generateSaprodontiaResult(SaprodontiaDataDO saprodontiaData) {
        List<SaprodontiaDataDO.SaprodontiaItem> above = saprodontiaData.getAbove();
        List<SaprodontiaDataDO.SaprodontiaItem> underneath = saprodontiaData.getUnderneath();

        List<SaprodontiaDataDO.SaprodontiaItem> saprodontiaItems = Stream.of(above, underneath).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(saprodontiaItems)) {
            return new TwoTuple<>(StringUtils.EMPTY, StringUtils.EMPTY);
        }
        return groupToothByList(saprodontiaItems);
    }

    /**
     * 通过牙齿类型分类
     *
     * @param saprodontiaItems 牙齿数据
     * @return TwoTuple<String, String> First-乳牙 Second-恒牙
     */
    private TwoTuple<String, String> groupToothByList(List<SaprodontiaDataDO.SaprodontiaItem> saprodontiaItems) {
        // 所有乳牙
        List<String> deciduousList = saprodontiaItems.stream().filter(Objects::nonNull).map(SaprodontiaDataDO.SaprodontiaItem::getDeciduous).collect(Collectors.toList());
        // 所有恒牙
        List<String> permanentList = saprodontiaItems.stream().filter(Objects::nonNull).map(SaprodontiaDataDO.SaprodontiaItem::getPermanent).collect(Collectors.toList());
        return new TwoTuple<>(countSaprodontiaNum(deciduousList), countSaprodontiaNum(permanentList));
    }

    /**
     * 统计龋齿类型个数
     *
     * @param list 所有数据
     * @return 结论
     */
    private String countSaprodontiaNum(List<String> list) {
        return list.stream().filter(s -> StringUtils.equalsAny(s, SaprodontiaType.DECIDUOUS_D.getName(), SaprodontiaType.PERMANENT_D.getName())).count() + ":" +
                list.stream().filter(s -> StringUtils.equalsAny(s, SaprodontiaType.DECIDUOUS_M.getName(), SaprodontiaType.PERMANENT_M.getName())).count() + ":" +
                list.stream().filter(s -> StringUtils.equalsAny(s, SaprodontiaType.DECIDUOUS_F.getName(), SaprodontiaType.PERMANENT_F.getName())).count();
    }

    /**
     * 脊柱弯曲
     *
     * @param dto       处理后筛查数据
     * @param exportDTO 筛查数据导出
     */
    private void generateSpineData(StatConclusionExportDTO dto, CommonDiseaseDataExportDTO exportDTO) {
        SpineDataDO spineData = dto.getSpineData();
        if (Objects.isNull(spineData)) {
            return;
        }
        SpineDataDO.SpineItem chest = spineData.getChest();
        SpineDataDO.SpineItem chestWaist = spineData.getChestWaist();
        SpineDataDO.SpineItem waist = spineData.getWaist();
        SpineDataDO.SpineItem entirety = spineData.getEntirety();
        exportDTO.setChest(SpineTypeEnum.getTypeName(chest.getLevel()) + "、" + SpineLevelEnum.getLevelName(chest.getType()))
                .setChestWaist(SpineTypeEnum.getTypeName(chestWaist.getLevel()) + "、" + SpineLevelEnum.getLevelName(chestWaist.getType()))
                .setWaist(SpineTypeEnum.getTypeName(waist.getLevel()) + "、" + SpineLevelEnum.getLevelName(waist.getType()))
                .setEntirety(SpineTypeEnum.getTypeName(entirety.getLevel()) + "、" + SpineLevelEnum.getLevelName(entirety.getType()));

    }

    /**
     * 血压
     *
     * @param dto       处理后筛查数据
     * @param exportDTO 筛查数据导出
     */
    private void generateBloodPressureData(StatConclusionExportDTO dto, CommonDiseaseDataExportDTO exportDTO) {
        exportDTO.setDbp(String.valueOf(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BLOOD_PRESSURE_DBP)))
                .setSbp(String.valueOf(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BLOOD_PRESSURE_SBP)));
    }

    /**
     * 疾病史、个人隐私、误差结果说明
     *
     * @param dto       处理后筛查数据
     * @param exportDTO 筛查数据导出
     */
    private void generatePrivacyDiseasesHistoryData(StatConclusionExportDTO dto, CommonDiseaseDataExportDTO exportDTO) {
        DiseasesHistoryDO diseasesHistoryData = dto.getDiseasesHistoryData();

        if (Objects.nonNull(diseasesHistoryData) && !CollectionUtils.isEmpty(diseasesHistoryData.getDiseases())) {
            exportDTO.setDiseasesHistory(String.join(",", diseasesHistoryData.getDiseases()));
        }
        PrivacyDataDO privacyData = dto.getPrivacyData();
        if (Objects.nonNull(privacyData)) {
            if (privacyData.getHasIncident()) {
                exportDTO.setPrivacyData("是 年龄：" + privacyData.getAge());
            } else {
                exportDTO.setPrivacyData("否");
            }
        }
        DeviationDO deviationData = dto.getDeviationData();
        if (Objects.nonNull(deviationData)) {
            exportDTO.setDeviationData(deviationData.getHeightWeightDeviation().getRemark());
        }
    }

}
