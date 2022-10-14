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

    /**
     * 幼儿园
     */
    private Map<String,Integer> kindergartenMap;
    /**
     * 小学
     */
    private Map<String,Integer> primaryMap;
    /**
     * 初中
     */
    private Map<String,Integer> juniorMap;
    /**
     * 高中
     */
    private Map<String,Integer> highMap;
    /**
     * 职高
     */
    private Map<String,Integer> vocationalHighMap;
    /**
     * 大学
     */
    private Map<String,Integer> universityMap;

    public GradeCodeYear() {
        this.kindergartenMap = Maps.newHashMap();
        this.primaryMap = Maps.newHashMap();
        this.juniorMap = Maps.newHashMap();
        this.highMap = Maps.newHashMap();
        this.vocationalHighMap = Maps.newHashMap();
        this.universityMap = Maps.newHashMap();
    }

    /**
     * 学校学生算年份使用
     */
    public static GradeCodeYear getGradeCodeYear(){
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
        Map<String,Integer> map = Maps.newHashMap();
        for (GradeCodeEnum gradeCodeEnum : kindergartenSchoolList) {
            switch (gradeCodeEnum){
                case ONE_KINDERGARTEN:
                    map.put(gradeCodeEnum.getCode(),0);
                    break;
                case TWO_KINDERGARTEN:
                    map.put(gradeCodeEnum.getCode(),1);
                    break;
                case THREE_KINDERGARTEN:
                    map.put(gradeCodeEnum.getCode(),2);
                    break;
                default:
                    break;
            }
        }
        gradeCodeYear.setKindergartenMap(map);
    }

    private static void primary(GradeCodeYear gradeCodeYear){
        Map<String,Integer> map = Maps.newHashMap();
        for (GradeCodeEnum gradeCodeEnum : GradeCodeEnum.privateSchool()) {
            switch (gradeCodeEnum){
                case ONE_PRIMARY_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),0);
                    break;
                case TWO_PRIMARY_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),1);
                    break;
                case THREE_PRIMARY_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),2);
                    break;
                case FOUR_PRIMARY_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),3);
                    break;
                case FIVE_PRIMARY_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),4);
                    break;
                case SIX_PRIMARY_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),5);
                    break;
                default:
                    break;
            }
        }
        gradeCodeYear.setPrimaryMap(map);

    }
    private static void junior(GradeCodeYear gradeCodeYear){
        Map<String,Integer> map = Maps.newHashMap();
        for (GradeCodeEnum gradeCodeEnum : GradeCodeEnum.juniorSchool()) {
            switch (gradeCodeEnum){
                case ONE_JUNIOR_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),0);
                    break;
                case TWO_JUNIOR_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),1);
                    break;
                case THREE_JUNIOR_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),2);
                    break;
                case FOUR_JUNIOR_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),3);
                    break;
                default:
                    break;
            }
        }
        gradeCodeYear.setJuniorMap(map);

    }
    private static void high(GradeCodeYear gradeCodeYear){
        Map<String,Integer> map = Maps.newHashMap();
        for (GradeCodeEnum gradeCodeEnum : GradeCodeEnum.highSchool()) {
            switch (gradeCodeEnum){
                case ONE_HIGH_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),0);
                    break;
                case TWO_HIGH_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),1);
                    break;
                case THREE_HIGH_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),2);
                    break;
                default:
                    break;
            }
        }
        gradeCodeYear.setHighMap(map);

    }
    private static void vocationalHigh(GradeCodeYear gradeCodeYear){
        Map<String,Integer> map = Maps.newHashMap();
        for (GradeCodeEnum gradeCodeEnum : GradeCodeEnum.vocationalHighSchool()) {
            switch (gradeCodeEnum){
                case ONE_VOCATIONAL_HIGH_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),0);
                    break;
                case TWO_VOCATIONAL_HIGH_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),1);
                    break;
                case THREE_VOCATIONAL_HIGH_SCHOOL:
                    map.put(gradeCodeEnum.getCode(),2);
                    break;
                default:
                    break;
            }
        }
        gradeCodeYear.setVocationalHighMap(map);

    }
    private static void university(GradeCodeYear gradeCodeYear){
        Map<String,Integer> map = Maps.newHashMap();
        for (GradeCodeEnum gradeCodeEnum : GradeCodeEnum.universitySchool()) {
            switch (gradeCodeEnum){
                case ONE_UNIVERSITY:
                    map.put(gradeCodeEnum.getCode(),0);
                    break;
                case TWO_UNIVERSITY:
                    map.put(gradeCodeEnum.getCode(),1);
                    break;
                case THREE_UNIVERSITY:
                    map.put(gradeCodeEnum.getCode(),2);
                    break;
                case FOUR_UNIVERSITY:
                    map.put(gradeCodeEnum.getCode(),3);
                    break;
                default:
                    break;
            }
        }
        gradeCodeYear.setUniversityMap(map);
    }
}
