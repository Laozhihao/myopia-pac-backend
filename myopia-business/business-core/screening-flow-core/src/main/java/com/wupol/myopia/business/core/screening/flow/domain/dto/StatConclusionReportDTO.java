package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 处理后筛查数据（包括学校ID）
 * @author Alix
 * @Date 2021/3/5
 **/

@Data
@Accessors(chain = true)
public class StatConclusionReportDTO extends StatConclusion {
    /**
     * 筛查计划--参与筛查的学生名字
     */
    private String studentName;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 学校Id
     */
    private Integer schoolId;

    /**
     * 筛查计划--执行的学校名字
     */
    private String schoolName;

    /**
     * 筛查计划--年级名称
     */
    private String gradeName;

    /**
     * 筛查计划--年级名称
     */
    private String className;

    /**
     * 筛查计划--参与筛查的学生编号
     */
    private String studentNo;

    /** 裸眼视力级别 */
    private Integer nakedVisionWarningLevel;

    /**
     * 筛查结果--视力检查结果
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private VisionDataDO visionData;

    /**
     * 筛查结果--电脑验光
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ComputerOptometryDO computerOptometry;
}