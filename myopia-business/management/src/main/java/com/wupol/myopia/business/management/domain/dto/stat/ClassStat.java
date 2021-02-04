package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassStat {
    /** 占比 */
    private float ratio;
    /** 数量 */
    private Integer num;
    /** 男性 */
    private BasicStatParams male;
    /** 女性 */
    private BasicStatParams female;
    /** 幼儿园 */
    private BasicStatParams kindergarten;
    /** 小学 */
    private BasicStatParams primary;
    /** 初中 */
    private BasicStatParams junior;
    /** 高中 */
    private BasicStatParams high;
    /** 职业中学 */
    private BasicStatParams vocationalHigh;
}
