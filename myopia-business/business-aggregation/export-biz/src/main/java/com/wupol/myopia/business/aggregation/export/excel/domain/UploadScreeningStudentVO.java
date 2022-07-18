package com.wupol.myopia.business.aggregation.export.excel.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 导入筛查计划的学生响应实体
 *
 * @author hang.yuan 2022/7/6 17:31
 */
@Data
public class UploadScreeningStudentVO implements Serializable {
    /**
     * 总共学生
     */
    private Integer totalStudentNum;
    /**
     * 成功学生
     */
    private Integer successStudentNum;
    /**
     * 失败学生
     */
    private Integer failStudentNum;

    /**
     * 失败数据下载链接
     */
    private String failDataUrl;
    /**
     * 文件名称
     */
    private String fileName;

    public UploadScreeningStudentVO buildNoData(){
        UploadScreeningStudentVO uploadScreeningStudentVO = new UploadScreeningStudentVO();
        uploadScreeningStudentVO.setTotalStudentNum(0);
        uploadScreeningStudentVO.setSuccessStudentNum(0);
        uploadScreeningStudentVO.setFailStudentNum(0);
        return uploadScreeningStudentVO;
    }
}
