package com.wupol.myopia.business.api.school.management.domain.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.core.hospital.domain.dto.MonthAgeStatusDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 0-6检查记录
 *
 * @author hang.yuan 2022/9/16 18:39
 */
@Data
@Accessors(chain = true)
public class PreschoolCheckRecordVO implements Serializable {


    /**
     *  学生信息
     */
    private StudentInfo studentInfo;

    /**
     * 记录
     */
    private IPage<PreschoolCheckRecordDTO> pageData;

    @Data
    @Accessors(chain = true)
    public static class StudentInfo implements Serializable{
        /** 学生姓名 */
        private String name;

        /** 性别 -1未知 0-男 1-女 */
        private Integer gender;
        /**
         * 检查建档编码
         */
        private String recordNo;

        /** 年龄 */
        private String birthdayInfo;

        /**
         * 年龄段对应的检查的状态
         */
        private List<MonthAgeStatusDTO> ageStageStatusList;
    }
}
