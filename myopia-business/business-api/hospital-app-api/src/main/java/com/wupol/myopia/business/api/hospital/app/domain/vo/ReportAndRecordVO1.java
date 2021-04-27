package com.wupol.myopia.business.api.hospital.app.domain.vo;

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
public class ReportAndRecordVO1 {

    private Integer hospitalId;

    private Integer reportId;

    private Date createTime;

    private Date updateTime;

    private String hospitalName;
}
