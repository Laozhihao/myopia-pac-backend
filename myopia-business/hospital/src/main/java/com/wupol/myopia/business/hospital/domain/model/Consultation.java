package com.wupol.myopia.business.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.hospital.domain.handler.DiseaseTypeHandler;
import com.wupol.myopia.business.management.domain.handler.NotificationConfigTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 学生在医院问诊
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("h_consultation")
public class Consultation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 学生id */
//    @NotBlank(message = "学生id不能为空")
    Integer studentId;
    /** 医院id */
    private Integer hospitalId;
    /** 病种列表 */
    @TableField(typeHandler = DiseaseTypeHandler.class)
    List<Disease> diseaseList;
    /** 创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 病种信息
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Disease {
        /** 名称 */
        private String name;
    }
}
