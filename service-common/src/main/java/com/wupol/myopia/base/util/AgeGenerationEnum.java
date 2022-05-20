package com.wupol.myopia.base.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 年龄段
 *
 * @author Simple4H
 */
@Getter
public enum AgeGenerationEnum {


    AGE_1("年龄 < 3", 0, 3),
    AGE_2("年龄 < 6", 0, 6),
    ;


    private final String name;

    private final Integer left;

    private final Integer right;

    AgeGenerationEnum(String name, Integer left, Integer right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }

    public static List<AgeGeneration> getKList(Integer min, Integer max) {
        List<AgeGeneration> list = getAgeGenerations(min);
        int age = min > 3 ? min : 3;
        while (age < 6 && age <= max) {
            AgeGeneration ageGeneration = new AgeGeneration(age + "<=" + "年龄<" + (age + 1), age, age + 1);
            list.add(ageGeneration);
            age++;
        }
        if (max >= 6) {
            AgeGeneration ageGeneration = new AgeGeneration("年龄" + ">=" + 6, age, 200);
            list.add(ageGeneration);
        }
        return list;
    }

    public static List<AgeGeneration> getPList(Integer min, Integer max) {
        List<AgeGeneration> list = new ArrayList<>();
        int age = min > 6 ? min : 6;
        if (min < 6) {
            AgeGeneration ageGeneration = new AgeGeneration(AGE_2.getName(), AGE_2.getLeft(), AGE_2.getRight());
            list.add(ageGeneration);
        }
        return getAgeGenerations(max, list, age);
    }


    public static List<AgeGeneration> getAllList(Integer min, Integer max) {
        List<AgeGeneration> list = getAgeGenerations(min);
        int age = min > 3 ? min : 3;
        return getAgeGenerations(max, list, age);
    }

    private static List<AgeGeneration> getAgeGenerations(Integer min) {
        List<AgeGeneration> list = new ArrayList<>();
        if (min < 3) {
            AgeGeneration ageGeneration = new AgeGeneration(AGE_1.getName(), AGE_1.getLeft(), AGE_1.getRight());
            list.add(ageGeneration);
        }
        return list;
    }

    private static List<AgeGeneration> getAgeGenerations(Integer max, List<AgeGeneration> list, int age) {
        while (age < 18 && age <= max) {
            AgeGeneration ageGeneration = new AgeGeneration(age + "<=" + "年龄<" + (age + 1), age, age + 1);
            list.add(ageGeneration);
            age++;
        }
        if (max >= 18) {
            AgeGeneration ageGeneration = new AgeGeneration("年龄" + ">=" + 18, age, 200);
            list.add(ageGeneration);
        }
        return list;
    }
}
