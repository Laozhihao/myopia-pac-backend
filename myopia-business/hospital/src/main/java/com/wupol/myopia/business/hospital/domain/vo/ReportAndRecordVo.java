package com.wupol.myopia.business.hospital.domain.vo;

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

    private Integer hospitalId;

    private Integer reportId;

    private Date createTime;

    private String hospitalName;
}
