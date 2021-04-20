package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.management.domain.model.StatConclusion;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 处理后筛查数据（包括学校ID）
 * @author Alix
 * @Date 2021/3/5
 **/

@Data
@Accessors(chain = true)
public class StatConclusionVo extends StatConclusion {
    /** 学校Id */
    private Integer schoolId;
}