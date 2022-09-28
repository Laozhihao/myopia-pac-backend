package com.wupol.myopia.business.api.school.management.facade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.builder.ScreeningBizBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 学校管理
 *
 * @author hang.yuan 2022/9/26 10:07
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SchoolManagementFacade {

    private final SchoolGradeService schoolGradeService;
    private final ScreeningPlanSchoolService screeningPlanSchoolService;

    /**
     * 删除年级
     * @param id
     * @param currentUser
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedGrade(Integer id, CurrentUser currentUser){
        Integer update = schoolGradeService.deletedGrade(id, currentUser);

        //删除年级时，筛查计划中的年级也需要删除
        List<ScreeningPlanSchool> screeningPlanSchoolList = screeningPlanSchoolService.listBySchoolIdAndOrgId(currentUser.getOrgId(), currentUser.getOrgId(), ScreeningTypeEnum.VISION.getType());
        screeningPlanSchoolList.forEach(screeningPlanSchool -> changeScreeningGradeIds(id, screeningPlanSchool));
        screeningPlanSchoolService.updateBatchById(screeningPlanSchoolList);
        return update;
    }

    /**
     * 改变筛查计划中年级ID集合
     * @param id
     * @param screeningPlanSchool
     */
    private void changeScreeningGradeIds(Integer id, ScreeningPlanSchool screeningPlanSchool) {
        List<Integer> screeningGradeIds = ScreeningBizBuilder.getScreeningGradeIds(screeningPlanSchool.getScreeningGradeIds());
        Iterator<Integer> it = screeningGradeIds.iterator();
        while (it.hasNext()){
            Integer gradeId = it.next();
            if (!Objects.equals(gradeId,id)){
                continue;
            }
            it.remove();
        }
        screeningPlanSchool.setScreeningGradeIds(CollUtil.join(screeningGradeIds, StrUtil.COMMA));
    }
}
