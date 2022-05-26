package com.wupol.myopia.migrate.domain.dos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * @Author HaoHao
 * @Date 2022/3/25
 **/
@AllArgsConstructor
@Data
public class SchoolAndGradeClassDO {
    private Map<String, Integer> schoolMap;
    private Map<String, Integer> gradeMap;
    private Map<String, Integer> classMap;
}
