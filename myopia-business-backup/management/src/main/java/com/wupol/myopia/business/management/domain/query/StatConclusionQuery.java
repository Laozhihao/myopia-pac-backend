package com.wupol.myopia.business.management.domain.query;

import com.wupol.myopia.business.management.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * 筛查结论查询
 *
 * @Author Bain
 * @Date 2021-02-24
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class StatConclusionQuery extends StatConclusion {
    private static final long serialVersionUID = -221210415537729707L;

    /** 层级ID列表 **/
    private List<Integer> districtIds;
    /** 查询开始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startTime;
    /** 查询结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endTime;
}
