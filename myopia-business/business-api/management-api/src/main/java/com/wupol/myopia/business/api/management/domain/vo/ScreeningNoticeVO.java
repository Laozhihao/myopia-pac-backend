package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.government.domain.model.District;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ScreeningNoticeVO extends ScreeningNoticeDTO {

    public ScreeningNoticeVO(ScreeningNoticeDTO screeningNoticeDTO) {
        BeanUtils.copyProperties(screeningNoticeDTO, this);
    }

    /** 行政区明细 */
    private List<District> districtDetail;

}
