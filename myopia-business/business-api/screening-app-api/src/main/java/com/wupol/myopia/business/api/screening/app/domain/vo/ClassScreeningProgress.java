package com.wupol.myopia.business.api.screening.app.domain.vo;

import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO;
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
    private Integer planCount;
    /** 实际筛查人数（有筛查数据的） */
    private Integer screeningCount;
    /** 有异常筛查人数（优先判断复测是否异常，无再判断初诊是否异常） */
    private Integer abnormalCount;
    /** 无异常筛查人数 = 实际筛查人数 - 有异常筛查人数 */
    private Integer normalCount;
    /** 筛查未完成学生数（做了筛查，但未完成初诊或初诊异常未完成复测的） */
    private Integer unfinishedCount;
    /** 筛查完成学生数 = 实际筛查人数 - 筛查未完成学生数 */
    private Integer finishedCount;
    /** 需要复测学生数（初诊异常的） */
    private Integer needReScreeningCount;
    /** 学龄段：5-幼儿园、0-小学、1-初中、2-高中、3-职业高中 */
    private Integer schoolAge;
    /** 当前班级的学生筛查进度情况 */
    private List<StudentScreeningProgressVO> studentScreeningProgressList;
    /** 是否为人造的 */
    private Boolean artificial;
}
