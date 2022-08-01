package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;

/**
 * 问卷学校填写情况 基类
 *
 * @author xz 2022 08 01 12:30
 */
@Data
public class QuestRecordVO {
    /**
     * 学校Id
     */
    private String schoolId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 区域id
     */
    private Integer areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 筛查机构名称，没有返回null
     */
    private String orgName;

    /**
     * 筛查机构id，没有返回null
     */
    private Integer orgId;
}
