package com.wupol.myopia.business.aggregation.export.service.impl;

import com.wupol.myopia.business.aggregation.export.service.IScreeningDataService;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.GlassesTypeEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.CommonDiseaseDataExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.system.constants.ScreeningTypeConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
//                    .setGlassesTypeDesc(StringUtils.defaultIfBlank(GlassesTypeEnum.getDescByCode(vo.getGlassesType()), "--"))
//                    .setIsRescreenDesc("否").setWarningLevelDesc(StringUtils.defaultIfBlank(WarningLevel.getDesc(vo.getWarningLevel()), "--"))
                    .setAddress(districtService.getAddressDetails(vo.getProvinceCode(), vo.getCityCode(), vo.getAreaCode(), vo.getTownCode(), vo.getAddress()))
                    .setIsValid(Boolean.TRUE.equals(vo.getIsValid()) ? "有效" : "无效");
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
}
