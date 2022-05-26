package com.wupol.myopia.migrate.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 视力筛查数据简单版
 *
 * @Author HaoHao
 * @Date 2022-03-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysStudentEyeSimple implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private String eyeId;

    /**
     * 学生ID
     */
    private String studentId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 学校ID
     */
    private String schoolId;

    /**
     * 学校名称
     */
    private String schoolName;

}
