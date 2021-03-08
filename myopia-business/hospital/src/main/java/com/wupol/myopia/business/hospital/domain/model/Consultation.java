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
public class Consultation {

    /** 学生id */
    Integer studentId;
    /** 病种列表 */
    @TableField(typeHandler = DiseaseTypeHandler.class)
    List<Disease> diseaseList;

    /**
     * 病种信息
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Disease {
        private Integer id;
        /** 名称 */
        private String name;
    }
}
