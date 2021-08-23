package com.wupol.myopia.business.api.screening.app.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021/8/23
 **/
@Accessors(chain = true)
@Data
public class ClassScreeningProgress {
    /** 计划筛查人数 */
    private Long planCount;
    /** 实际筛查人数 */
    private Long screeningCount;
    /** 有异常筛查人数，仅统计有初步结果的：眼位、视力检查、电脑验光、小瞳验光 */
    private Long abnormalCount;
    /** 筛查未完成学生数 */
    private Long unfinishedCount;
    /** 学龄段：5-幼儿园、0-小学、1-初中、2-高中、3-职业高中 */
    private Integer schoolAge;
    /** 当前班级的学生筛查进度情况 */
    private List<StudentScreeningProgressVO> studentScreeningProgressList;
}
