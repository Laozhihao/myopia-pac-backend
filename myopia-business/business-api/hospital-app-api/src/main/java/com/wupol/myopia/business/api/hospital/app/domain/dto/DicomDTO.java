package com.wupol.myopia.business.api.hospital.app.domain.dto;

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

    private String fileName;
    private String macAddress;
    private String dcmName;
    // 判断是否重复上传
    private String md5;
    // 基本信息-JSON文件名字
    private String base;
    // 0-图片 1-pdf
    private Integer fileType;

    /**
     * 患者Id
     */
    private Integer patientId;

    /**
     * 设备Id
     */
    private Integer deviceId;

    /**
     * 医院Id
     */
    private Integer hospitalId;
}
