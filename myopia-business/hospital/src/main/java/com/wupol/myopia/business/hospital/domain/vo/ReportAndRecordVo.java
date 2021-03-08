package com.wupol.myopia.business.hospital.domain.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * ReportAndRecordVo
 *
 * @author Simple4H
 */
@Getter
@Setter
@Accessors(chain = true)
public class ReportAndRecordVo {

    private Integer recordId;

    private Integer reportId;

    private Date createTime;
}
