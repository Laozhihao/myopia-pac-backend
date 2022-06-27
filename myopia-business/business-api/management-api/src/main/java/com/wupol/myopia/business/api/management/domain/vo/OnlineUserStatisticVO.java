package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 在线用户统计响应实体
 *
 * @author hang.yuan 2022/5/10 10:03
 */
@Data
@Accessors(chain = true)
public class OnlineUserStatisticVO implements Serializable {
    /**
     * 管理端数
     */
    private Long managementClientNum;
    /**
     * 学校端数
     */
    private Long schoolClientNum;
    /**
     * 筛查端数
     */
    private Long screeningClientNum;
    /**
     * 医生端数
     */
    private Long hospitalClientNum;
    /**
     * 家长端数
     */
    private Long parentClientNum;
    /**
     * 0-6岁客户端
     */
    private Long zeroToSixClientNum;

}