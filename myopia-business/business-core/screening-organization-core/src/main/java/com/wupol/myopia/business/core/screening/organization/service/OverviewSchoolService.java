package com.wupol.myopia.business.core.screening.organization.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.OverviewSchoolMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewSchool;
import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewScreeningOrganization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Simple4H
 */
@Service
public class OverviewSchoolService extends BaseService<OverviewSchoolMapper, OverviewSchool> {

    @Resource
    private OverviewService overviewService;

    /**
     * 批量插入总览机构医院绑定信息
     *
     * @param overviewId 总览Id
     * @param schoolIds  学校Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Integer overviewId, List<Integer> schoolIds) {
        if (Objects.isNull(overviewId) || CollectionUtils.isEmpty(schoolIds)) {
            return;
        }
        baseMapper.batchSave(overviewId, schoolIds);
    }

    /**
     * 更新绑定信息
     *
     * @param overviewId 总览Id
     * @param schoolIds  学校Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBindInfo(Integer overviewId, List<Integer> schoolIds) {
        // 先删除再绑定
        if (Objects.isNull(overviewId)) {
            return;
        }
        super.remove(new OverviewSchool().setOverviewId(overviewId));
        if (CollectionUtils.isEmpty(schoolIds)) {
            return;
        }
        batchSave(overviewId, schoolIds);
    }

    /**
     * 获取指定总览机构所绑定的学校id集
     *
     * @param overviewId 总览机构Id
     *
     * @return 学校id集
     */
    public List<Integer> getSchoolIdByOverviewId(Integer overviewId) {

        return findByList(new OverviewSchool().setOverviewId(overviewId))
                .stream()
                .map(OverviewSchool::getSchoolId)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定总览机构集所绑定的医院数量
     * @param overviewIds 总览机构Ids
     * @return Map<Integer, Long>
     */
    public Map<Integer, Long> getOverviewSchoolNum(List<Integer> overviewIds) {
        if (CollectionUtils.isEmpty(overviewIds)) {
            return Collections.emptyMap();
        }
        return baseMapper.getListByOverviewIds(overviewIds).stream()
                .collect(Collectors.groupingBy(OverviewSchool::getOverviewId, Collectors.counting()));
    }

    /**
     * 保存总览学校
     *
     * @param overviewId 总览Id
     * @param schoolId   id
     */
    public void saveOverviewSchool(Integer overviewId, Integer schoolId) {
        save(new OverviewSchool().setOverviewId(overviewId).setSchoolId(schoolId));
        overviewService.removeOverviewCache(overviewId);
    }

}
