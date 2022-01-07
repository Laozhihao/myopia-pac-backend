package com.wupol.myopia.business.core.hospital.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2022/1/7 17:35
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MonthAgeStatusDTO {

    /**
     * 月龄[0-新生儿；1-满月；2-3月龄；3-6月龄；4-8月龄；5-12月龄；6-18月龄；7-24月龄；8-30月龄；9-36月龄；10-4岁；11-5岁；12-6岁；]
     */
    private Integer monthAge;
    /**
     * [1 不可点击; 2 可点击，还没有检查数据; 3 可点击，有检查数据，检查当天查看可以修改，过来检查; 4 选中状态; 5 不可见; 6 不可更新]
     */
    private Integer status;

}
