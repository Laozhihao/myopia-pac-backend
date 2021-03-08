package com.wupol.myopia.business.screening.domain.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * <p> 安全认证用户详情信息 </p>
 *
 * @author : zhengqing
 * @description :
 * @date : 2019/10/14 10:11
 */
@Data
@Slf4j
public class AppUserInfo implements Serializable {

    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 筛查机构id
     */
    private Integer deptId;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 签名
     */
    private String autImage;

}
