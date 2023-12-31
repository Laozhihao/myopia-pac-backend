package com.wupol.myopia.business.aggregation.screening.domain.vos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 筛查学校响应对象
 *
 * @author hang.yuan 2022/9/13 17:41
 */
@Data
@Accessors(chain = true)
public class ScreeningStudentListVO implements Serializable {


    /**
     * 学校学生ID
     */
    private Integer id;
    /**
     * 学生id
     */
    private Integer studentId;
    /**
     * 筛查计划学生Id
     */
    private Integer planStudentId;
    /**
     * 学生编码
     */
    private Long screeningCode;
    /**
     * 学号
     */
    private String sno;
    /**
     * 学生姓名
     */
    private String name;
    /**
     * 性别
     */
    private Integer gender;

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 裸视力 (右/左)
     */
    private String nakedVision;

    /**
     * 矫正 (右/左)
     */
    private String correctedVision;

    /**
     * 球镜(右/左)
     */
    private String sph;

    /**
     * 柱镜(右/左)
     */
    private String cyl;

    /**
     * 轴位 (右/左)
     */
    private String axial;
    /**
     * 戴镜类型描述
     */
    private String glassesTypeDes;

    /**
     * 未做检查说明【0:无；1：请假；2：转学;3:其他】
     */
    private Integer state;

    /**
     * 数据完整性
     */
    private String dataIntegrity;

    /**
     * 是否已经筛查过
     **/
    private Boolean hasScreening;

}
