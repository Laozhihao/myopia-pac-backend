package com.wupol.myopia.business.core.hospital.domain.query;

import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @Author wulizhou
 * @Date 2022/1/4 20:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PreschoolCheckRecordQuery extends PreschoolCheckRecordDTO {

    /**
     * 检查开始时间
     */
    private Date checkDateStart;

    /**
     * 检查结束时间
     */
    private Date checkDateEnd;

    /**
     *
     * 是否检查后转诊[0 否; 1 是]
     */
    private Integer isToReferral;

    /**
     * 检查月龄（搜索）
     */
    private List<Integer> monthAges;

    /**
     * 是否取以学生维度获取检查信息
     */
    private Boolean isGroupByStudent = false;

    public void setStartDate(Long startDate) {
        checkDateStart = new Date(startDate);
    }

    public void setEndDate(Long endDate) {
        checkDateEnd = new Date(endDate);
    }

}
