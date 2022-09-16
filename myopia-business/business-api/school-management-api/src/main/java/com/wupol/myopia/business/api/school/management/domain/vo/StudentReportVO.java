package com.wupol.myopia.business.api.school.management.domain.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 学生就诊记录
 *
 * @author hang.yuan 2022/9/16 19:23
 */
@Data
@Accessors(chain = true)
public class StudentReportVO implements Serializable {


    /**
     * 学生信息
     */
    private StudentInfo studentInfo;

    /**
     * 就诊记录
     */
    private IPage<ReportAndRecordDO> pageData;

    @Data
    @Accessors(chain = true)
    public static class StudentInfo implements Serializable{
        /**
         * 性别 0-男 1-女
         */
        private Integer gender;
        /**
         * 学生姓名
         */
        private String name;

        /** 年龄 */
        private String birthdayInfo;
    }
}
