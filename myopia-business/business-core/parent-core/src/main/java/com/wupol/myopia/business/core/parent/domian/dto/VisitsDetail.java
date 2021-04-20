package com.wupol.myopia.business.core.parent.domian.dto;

import com.wupol.myopia.business.hospital.domain.vo.ReportAndRecordVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 就诊报告统计详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class VisitsDetail {

    /**
     * 总数
     */
    private Integer total;

    /**
     * 详情
     */
    private List<ReportAndRecordVo> items;
}
