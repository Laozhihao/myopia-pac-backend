package com.wupol.myopia.business.core.hospital.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 儿童眼保健及视力检查记录
 *
 * @author Simple4H
 */
@Getter
@Setter
public class EyeHealthyReportResponseDTO {

    /**
     * Id
     */
    private Integer id;

    /**
     * 0-新生儿；1-满月；2-3月龄；3-6月龄；4-8月龄；5-12月龄；6-18月龄；7-24月龄；8-30月龄；9-36月龄；10-4岁；11-5岁；12-6岁；
     */
    private Integer monthAge;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;

    /**
     * 医院名称
     */
    private String hospitalName;
}
