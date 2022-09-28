package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 处理后筛查数据（包括学校ID）
 * @author Alix
 * @Date 2021/3/5
 **/

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class StatConclusionDTO extends StatConclusion {
    /** 学校Id */
    private Integer schoolId;
}