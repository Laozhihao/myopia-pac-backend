package com.wupol.myopia.business.core.screening.organization.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author wulizhou
 * @Date 2022/2/25 10:15
 */
@Data
public class CacheOverviewInfoDTO {

    /**
     * id
     */
    private Integer id;

    /**
     * 行政区域ID
     */
    private Integer districtId;

    /**
     * 行政区域json
     */
    private String districtDetail;

    /**
     * 配置类型，0：配置筛查机构、1：配置医院、2：配置筛查机构+医院
     */
    private Integer configType;

    /**
     * 医院服务类型（配置），0：居民健康系统(默认)、1：0-6岁眼保健、2：0-6岁眼保健+居民健康系统
     */
    private Integer hospitalServiceType;

    /**
     * 医院限制数量
     */
    private Integer hospitalLimitNum;

    /**
     * 筛查机构配置 0-省级配置 1-单点配置 2-VS666 3-单点配置+VS666
     */
    private Integer screeningOrganizationConfigType;

    /**
     * 筛查机构限制数量
     */
    private Integer screeningOrganizationLimitNum;

    /**
     * 合作类型 0-合作 1-试用
     */
    private Integer cooperationType;

    /**
     * 合作期限类型 -1-自定义 0-30天 1-60天 2-180天 3-1年 4-2年 5-3年
     */
    private Integer cooperationTimeType;

    /**
     * 合作开始时间
     */
    private Date cooperationStartTime;

    /**
     * 合作结束时间
     */
    private Date cooperationEndTime;

    /**
     * 绑定医院ids
     */
    private List<Integer> hospitalIds;
    /**
     * 绑定筛查机构ids
     */
    private List<Integer> screeningOrganizationIds;

    /**
     * 是否可以再增加绑定医院
     * @return
     */
    @JsonIgnore
    public boolean isCanAddHospital() {
        return hospitalLimitNum > hospitalIds.size();
    }

    /**
     * 是否可以再增加绑定筛查机构
     * @return
     */
    @JsonIgnore
    public boolean isCanAddScreeningOrganization() {
        return screeningOrganizationLimitNum > screeningOrganizationIds.size();
    }

}
