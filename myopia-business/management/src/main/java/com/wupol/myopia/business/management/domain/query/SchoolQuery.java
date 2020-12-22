package com.wupol.myopia.business.management.domain.query;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.Student;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 学校查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@Data
public class SchoolQuery extends School {
    /** id */
    private String idLike;
    /** 名称 */
    private String nameLike;
    /** 地区编码 */
    private String code;
}
