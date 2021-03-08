package com.wupol.myopia.business.screening.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Date 2021/1/29 14:34
 * @Author by Jacob
 */
@NoArgsConstructor
@Data
public class OldRescreeningGenericStructure {

    private Integer number;
    /**
     * qualified
     */
    private Integer qualified;
    /**
     * gradeName
     */
    private String gradeName;
    /**
     * qualifiedCount
     */
    private Integer qualifiedCount;
    /**
     * eyeResult
     */
    private Integer eyeResult;
    /**
     * eyesCount
     */
    private Integer eyesCount;
    /**
     * reviewsCount
     */
    private Integer reviewsCount;
    /**
     * schoolName
     */
    private String schoolName;
    /**
     * clazzName
     */
    private Integer clazzName;

    /**
     * content
     */
    private List<EyeInfo> content;
}