package com.wupol.myopia.business.core.school.util;

import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class SchoolUtil {

    /**
     * 根据学校类型返回对应学龄
     *
     * @param schoolEnum 学校类型
     * @return
     */
    public static List<GradeCodeEnum> getGradeCodeEnumListBySchoolType(SchoolEnum schoolEnum) {
        List<GradeCodeEnum> gradeCodeEnumList = Collections.emptyList();
        if (schoolEnum == null) {
            return gradeCodeEnumList;
        }
        switch (schoolEnum) {
            case TYPE_PRIMARY:
                gradeCodeEnumList = Arrays.stream(GradeCodeEnum.values()).filter(x -> x.getType().equals(SchoolAge.PRIMARY.code)).collect(Collectors.toList());
                break;
            case TYPE_MIDDLE:
                gradeCodeEnumList = Arrays.stream(GradeCodeEnum.values()).filter(x -> x.getType().equals(SchoolAge.JUNIOR.code)).collect(Collectors.toList());
                break;
            case TYPE_HIGH:
                gradeCodeEnumList = Arrays.stream(GradeCodeEnum.values()).filter(x -> x.getType().equals(SchoolAge.HIGH.code)).collect(Collectors.toList());
                break;
            case TYPE_INTEGRATED_MIDDLE:
                gradeCodeEnumList = Arrays.stream(GradeCodeEnum.values()).filter(x -> x.getType().equals(SchoolAge.JUNIOR.code)).collect(Collectors.toList());
                gradeCodeEnumList.addAll(Arrays.stream(GradeCodeEnum.values()).filter(x -> x.getType().equals(SchoolAge.HIGH.code)).collect(Collectors.toList()));
                break;
            case TYPE_9:
                gradeCodeEnumList = Arrays.stream(GradeCodeEnum.values()).filter(x -> x.getType().equals(SchoolAge.PRIMARY.code)).collect(Collectors.toList());
                gradeCodeEnumList.addAll(Arrays.stream(GradeCodeEnum.values()).filter(x -> x.getType().equals(SchoolAge.JUNIOR.code)).collect(Collectors.toList()));
                break;
            case TYPE_12:
                gradeCodeEnumList = Arrays.stream(GradeCodeEnum.values()).filter(x -> x.getType().equals(SchoolAge.PRIMARY.code)).collect(Collectors.toList());
                gradeCodeEnumList.addAll(Arrays.stream(GradeCodeEnum.values()).filter(x -> x.getType().equals(SchoolAge.JUNIOR.code)).collect(Collectors.toList()));
                gradeCodeEnumList.addAll(Arrays.stream(GradeCodeEnum.values()).filter(x -> x.getType().equals(SchoolAge.HIGH.code)).collect(Collectors.toList()));
                break;
            case TYPE_VOCATIONAL:
                gradeCodeEnumList = Arrays.stream(GradeCodeEnum.values()).filter(x -> x.getType().equals(SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
                break;
            default:
        }
        return gradeCodeEnumList;
    }

}
