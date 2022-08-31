package com.wupol.myopia.business.core.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wupol.myopia.business.core.hospital.domain.handler.DiseaseTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 学生在医院问诊
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class Consultation implements Serializable {

    /** 学生id */
    private Integer studentId;
    /** 病种列表 */
    @TableField(typeHandler = DiseaseTypeHandler.class)
    private List<Disease> diseaseList;

    /**
     * 病种信息
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Disease implements Serializable {
        private Integer id;
        /** 名称 */
        private String name;
    }
}
