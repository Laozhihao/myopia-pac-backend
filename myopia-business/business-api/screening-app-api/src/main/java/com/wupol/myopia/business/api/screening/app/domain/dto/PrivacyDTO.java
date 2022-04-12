package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.core.screening.flow.domain.dos.PrivacyDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * @Description 个人隐私
 * @Date 2021/04/07 1:08
 * @Author by xz
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PrivacyDTO extends ScreeningResultBasicData {
    /**
     * 是否隐私项
     */
    private boolean hasIncident;

    /**
     * 出现的年龄
     */
    private Integer age;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        PrivacyDataDO privacyDataDO = new PrivacyDataDO();
        privacyDataDO.setAge(age);
        privacyDataDO.setHasIncident(hasIncident);
        return visionScreeningResult.setPrivacyData(privacyDataDO);
    }

    public boolean isValid() {
        // 如果为是则年龄必填
        if (hasIncident && Objects.isNull(age)) {
            return false;
        }
        return true;
    }

    public static PrivacyDTO getInstance(PrivacyDataDO privacyDataDO) {
        if (Objects.isNull(privacyDataDO)) {
            return null;
        }
        PrivacyDTO privacyDTO = new PrivacyDTO();
        privacyDTO.setAge(privacyDTO.age);
        privacyDTO.setHasIncident(privacyDTO.hasIncident);
        return privacyDTO;
    }
}
