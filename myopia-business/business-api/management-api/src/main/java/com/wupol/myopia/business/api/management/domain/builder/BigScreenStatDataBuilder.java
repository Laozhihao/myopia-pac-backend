package com.wupol.myopia.business.api.management.domain.builder;

import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.stat.domain.dto.BigScreenStatDataDTO;
import lombok.experimental.UtilityClass;

/**
 * @Author HaoHao
 * @Date 2021/4/27
 **/
@UtilityClass
public class BigScreenStatDataBuilder {
    /**
     * 获取实例
     *
     * @param statConclusion
     * @return
     */
    public static BigScreenStatDataDTO build(StatConclusion statConclusion) {
        BigScreenStatDataDTO bigScreenStatDataDTO = new BigScreenStatDataDTO();
        bigScreenStatDataDTO.setAge(statConclusion.getAge());
        bigScreenStatDataDTO.setGender(statConclusion.getGender());
        bigScreenStatDataDTO.setIsLowVision(statConclusion.getIsLowVision());
        bigScreenStatDataDTO.setIsMyopia(statConclusion.getIsMyopia());
        bigScreenStatDataDTO.setIsRefractiveError(statConclusion.getIsRefractiveError());
        bigScreenStatDataDTO.setSchoolAge(statConclusion.getSchoolAge());
        bigScreenStatDataDTO.setWarningLevel(statConclusion.getWarningLevel());
        bigScreenStatDataDTO.setVisionL(statConclusion.getVisionL());
        bigScreenStatDataDTO.setVisionR(statConclusion.getVisionR());
        bigScreenStatDataDTO.setIsValid(statConclusion.getIsValid());
        bigScreenStatDataDTO.setDistrictId(statConclusion.getDistrictId());
        bigScreenStatDataDTO.setMyopiaLevel(statConclusion.getMyopiaLevel());
        bigScreenStatDataDTO.setIsAstigmatism(statConclusion.getIsAstigmatism());
        bigScreenStatDataDTO.setSchoolId(statConclusion.getSchoolId());
        bigScreenStatDataDTO.setScreeningMyopia(statConclusion.getScreeningMyopia());
        return bigScreenStatDataDTO;
    }
}
