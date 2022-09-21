package com.wupol.myopia.business.api.device.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Dicom数据
 *
 * @author Simple4H
 */
@Getter
@Setter
public class DicomDTO {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * mac地址
     */
    private String macAddress;

    /**
     * dcm文件名
     */
    private String dcmName;

    /**
     * 判断是否重复上传
     */
    private String md5;

    /**
     * 基本信息-JSON文件名字
     */
    private String base;

    /**
     * 0-图片 1-pdf
     */
    private Integer fileType;

    /**
     * 患者Id
     */
    private Integer patientId;
}
