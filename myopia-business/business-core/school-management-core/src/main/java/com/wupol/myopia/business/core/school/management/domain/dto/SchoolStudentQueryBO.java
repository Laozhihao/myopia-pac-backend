package com.wupol.myopia.business.core.school.management.domain.dto;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学校学生查询
 *
 * @author hang.yuan 2022/9/29 19:14
 */
@Data
@Accessors(chain = true)
public class SchoolStudentQueryBO implements Serializable {

    /**
     * 学生名称
     */
    private String name;
    /**
     * 学号
     */
    private String sno;
    /**
     * 身份证
     */
    private String idCard;
    /**
     * 护照
     */
    private String passport;

    /**
     * 绑定手机号
     */
    private String parentPhone;
    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 学校Id集合
     */
    private List<Integer> schoolIds;

    /**
     * 年级ID
     */
    private Integer gradeId;
    /**
     * 班级ID
     */
    private Integer classId;

    /**
     * 视力标签字符串
     */
    private String visionLabels;


    public List<Integer> getVisionLabels() {
        if (StrUtil.isNotBlank(visionLabels)){
            return Arrays.stream(visionLabels.split(StrUtil.COMMA)).map(Integer::valueOf).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }
}
