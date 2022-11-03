package com.wupol.myopia.business.core.school.domain.dos;

import com.google.common.collect.Maps;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.wupol.myopia.business.common.utils.constant.SchoolAge.get;

/**
 * 年级编码和学年
 *
 * @author hang.yuan 2022/10/14 11:48
 */
@Data
public class GradeCodeYear implements Serializable {

    private Map<String,Integer> gradeCodeYearMap;

    public GradeCodeYear() {
        this.gradeCodeYearMap = Maps.newHashMap();
    }

    /**
     * 学校学生算年份使用
     */
    private static GradeCodeYear getGradeCodeYear(){
        GradeCodeYear gradeCodeYear = new GradeCodeYear();
        GradeCodeEnum.gradeByMap.forEach((code,gradeCodeEnumList)->{
            SchoolAge schoolAge = get(code);
            switch (schoolAge){
                case KINDERGARTEN:
                    kindergarten(gradeCodeYear);
                    break;
                case PRIMARY:
                    primary(gradeCodeYear);
                    break;
                case JUNIOR:
                    junior(gradeCodeYear);
                    break;
                case HIGH:
                    high(gradeCodeYear);
                    break;
                case VOCATIONAL_HIGH:
                    vocationalHigh(gradeCodeYear);
                    break;
                case UNIVERSITY:
                    university(gradeCodeYear);
                    break;
                default:
                    break;
            }
        });
        return gradeCodeYear;
    }

    private static void kindergarten(GradeCodeYear gradeCodeYear){
        List<GradeCodeEnum> kindergartenSchoolList = GradeCodeEnum.kindergartenSchool().stream().filter(gradeCodeEnum -> !(Objects.equals(gradeCodeEnum.getCode(), "49") || Objects.equals(gradeCodeEnum.getCode(), "50"))).collect(Collectors.toList());
        for (GradeCodeEnum gradeCodeEnum : kindergartenSchoolList) {
            switch (gradeCodeEnum){
                case ONE_KINDERGARTEN:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),0);
                    break;
                case TWO_KINDERGARTEN:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),1);
                    break;
                case THREE_KINDERGARTEN:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),2);
                    break;
                default:
                    break;
            }
        }
    }

    private static void primary(GradeCodeYear gradeCodeYear){
        for (GradeCodeEnum gradeCodeEnum : GradeCodeEnum.privateSchool()) {
            switch (gradeCodeEnum){
                case ONE_PRIMARY_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),0);
                    break;
                case TWO_PRIMARY_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),1);
                    break;
                case THREE_PRIMARY_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),2);
                    break;
                case FOUR_PRIMARY_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),3);
                    break;
                case FIVE_PRIMARY_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),4);
                    break;
                case SIX_PRIMARY_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),5);
                    break;
                default:
                    break;
            }
        }
    }
    private static void junior(GradeCodeYear gradeCodeYear){
        for (GradeCodeEnum gradeCodeEnum : GradeCodeEnum.juniorSchool()) {
            switch (gradeCodeEnum){
                case PRELIMINARY_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),0);
                    break;
                case ONE_JUNIOR_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),1);
                    break;
                case TWO_JUNIOR_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),2);
                    break;
                case THREE_JUNIOR_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),3);
                    break;
                case FOUR_JUNIOR_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),4);
                    break;
                default:
                    break;
            }
        }
    }
    private static void high(GradeCodeYear gradeCodeYear){
        for (GradeCodeEnum gradeCodeEnum : GradeCodeEnum.highSchool()) {
            switch (gradeCodeEnum){
                case ONE_HIGH_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),0);
                    break;
                case TWO_HIGH_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),1);
                    break;
                case THREE_HIGH_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),2);
                    break;
                default:
                    break;
            }
        }
    }

    private static void vocationalHigh(GradeCodeYear gradeCodeYear){
        for (GradeCodeEnum gradeCodeEnum : GradeCodeEnum.vocationalHighSchool()) {
            switch (gradeCodeEnum){
                case ONE_VOCATIONAL_HIGH_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),0);
                    break;
                case TWO_VOCATIONAL_HIGH_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),1);
                    break;
                case THREE_VOCATIONAL_HIGH_SCHOOL:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),2);
                    break;
                default:
                    break;
            }
        }
    }
    private static void university(GradeCodeYear gradeCodeYear){
        for (GradeCodeEnum gradeCodeEnum : GradeCodeEnum.universitySchool()) {
            switch (gradeCodeEnum){
                case ONE_UNIVERSITY:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),0);
                    break;
                case TWO_UNIVERSITY:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),1);
                    break;
                case THREE_UNIVERSITY:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),2);
                    break;
                case FOUR_UNIVERSITY:
                    gradeCodeYear.gradeCodeYearMap.put(gradeCodeEnum.getCode(),3);
                    break;
                default:
                    break;
            }
        }
    }

    public static Integer getYear(String gradeCode){
        GradeCodeYear gradeCodeYear = getGradeCodeYear();
        return gradeCodeYear.gradeCodeYearMap.entrySet().stream()
                .filter(entry->Objects.equals(entry.getKey(),gradeCode))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

}
