package com.wupol.myopia.business.api.management.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 在线用户统计响应实体
 *
 * @author hang.yuan 2022/5/10 10:03
 */
@Data
@Accessors(chain = true)
public class OnlineUserStatisticVO implements Serializable {
    /**
     * 管理端用户列表
     */
    private OnlineUser managementClientUser;
    /**
     * 学校端用户列表
     */
    private OnlineUser schoolClientUser;
    /**
     * 筛查端用户列表
     */
    private OnlineUser screeningClientUser;
    /**
     * 医院端用户列表
     */
    private OnlineUser hospitalClientUser;
    /**
     * 家长端用户列表
     */
    private OnlineUser parentClientUser;
    /**
     * 0-6岁客户端用户列表
     */
    private OnlineUser zeroToSixClientUser;
    /**
     * 问卷系统端用户列表
     */
    private OnlineUser questionnaireClientUser;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OnlineUser implements Serializable {
        private Integer count;
        private List<String> nameList;
    }

}
