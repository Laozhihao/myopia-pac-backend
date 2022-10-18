package com.wupol.myopia.business.api.school.management.facade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.aggregation.screening.constant.SchoolConstant;
import com.wupol.myopia.business.api.school.management.domain.vo.ScreeningNoticeListVO;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校筛查通知
 *
 * @author hang.yuan 2022/9/27 15:28
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SchoolScreeningNoticeFacade {

    private final ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    private final GovDeptService govDeptService;
    private final ScreeningPlanService screeningPlanService;
    private final ScreeningTaskService screeningTaskService;

    /**
     * 分页获取筛查通知列表
     * @param currentUser 当前用户
     */
    public IPage<ScreeningNoticeListVO> page(CurrentUser currentUser, PageRequest pageRequest) {
        ScreeningNoticeQueryDTO query = new ScreeningNoticeQueryDTO();
        query.setGovDeptId(currentUser.getOrgId());
        query.setType(ScreeningNotice.TYPE_SCHOOL);
        IPage<ScreeningNoticeDTO> screeningNoticePage = screeningNoticeDeptOrgService.selectPageByQuery(pageRequest.toPage(), query);

        IPage<ScreeningNoticeListVO> screeningNoticeListVoPage = new Page<>(screeningNoticePage.getCurrent(),screeningNoticePage.getSize(),screeningNoticePage.getTotal());
        List<ScreeningNoticeDTO> records = screeningNoticePage.getRecords();
        if (CollUtil.isEmpty(records)){
            return screeningNoticeListVoPage;
        }

        Map<Integer, ScreeningPlan> screeningPlanMap = getScreeningPlanMap(currentUser, records);

        // 政府部门名称
        TwoTuple<Map<Integer, String>, Map<Integer, Integer>> noticeInfoMap = getNoticeInfoMap(records);
        List<ScreeningNoticeListVO> screeningNoticeListVOList = records.stream().map(screeningNoticeDTO -> getScreeningNoticeListVO(noticeInfoMap, screeningNoticeDTO,screeningPlanMap)).collect(Collectors.toList());
        screeningNoticeListVoPage.setRecords(screeningNoticeListVOList);
        return screeningNoticeListVoPage;
    }

    /**
     * 获取筛查计划集合
     * @param currentUser
     * @param records
     */
    private Map<Integer, ScreeningPlan> getScreeningPlanMap(CurrentUser currentUser, List<ScreeningNoticeDTO> records) {
        Set<Integer> taskIds = records.stream().map(ScreeningNoticeDTO::getScreeningTaskId).collect(Collectors.toSet());
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getByTaskIdsAndOrgIdAndOrgType(Lists.newArrayList(taskIds), currentUser.getOrgId(), ScreeningOrgTypeEnum.SCHOOL.getType());
        return screeningPlanList.stream().collect(Collectors.toMap(ScreeningPlan::getScreeningTaskId, Function.identity()));
    }

    /**
     * 获取筛查通知列表对象
     * @param noticeInfoMap
     * @param screeningNoticeDTO
     */
    private ScreeningNoticeListVO getScreeningNoticeListVO(TwoTuple<Map<Integer, String>, Map<Integer, Integer>>  noticeInfoMap, ScreeningNoticeDTO screeningNoticeDTO,Map<Integer, ScreeningPlan> screeningPlanMap) {
        ScreeningPlan screeningPlan = screeningPlanMap.get(screeningNoticeDTO.getScreeningTaskId());
        int status = Objects.nonNull(screeningPlan) ? SchoolConstant.Screening_NOTICE_STATUS_CREATED : SchoolConstant.Screening_NOTICE_STATUS_UNCREATED;
        return new ScreeningNoticeListVO()
                .setId(screeningNoticeDTO.getId())
                .setTitle(screeningNoticeDTO.getTitle())
                .setContent(screeningNoticeDTO.getContent())
                .setStartTime(screeningNoticeDTO.getStartTime())
                .setEndTime(screeningNoticeDTO.getEndTime())
                .setStatus(status)
                .setScreeningTaskId(screeningNoticeDTO.getScreeningTaskId())
                .setAcceptTime(screeningNoticeDTO.getAcceptTime())
                .setNoticeDeptName(noticeInfoMap.getFirst().getOrDefault(screeningNoticeDTO.getScreeningTaskId(), StrUtil.EMPTY))
                .setSrcScreeningNoticeId(noticeInfoMap.getSecond().getOrDefault(screeningNoticeDTO.getScreeningTaskId(),0))
                .setCanCreatePlan(canCreatePlan(status,screeningNoticeDTO.getScreeningType()))
                .setScreeningType(screeningNoticeDTO.getScreeningType());
    }

    /**
     * 判断筛查计划是否能创建
     * @param status
     * @param screeningType
     */
    private Boolean canCreatePlan(int status,Integer screeningType){
        if (Objects.equals(status,SchoolConstant.Screening_NOTICE_STATUS_CREATED)  ){
            return Boolean.FALSE;
        }else {
            return Objects.equals(screeningType, ScreeningTypeEnum.VISION.getType());
        }
    }

    /**
     * 政府部门名称
     * @param records
     */
    private TwoTuple<Map<Integer, String>,Map<Integer, Integer>> getNoticeInfoMap(List<ScreeningNoticeDTO> records) {
        Set<Integer> taskIds = records.stream()
                .filter(vo -> ScreeningNotice.TYPE_SCHOOL.equals(vo.getType()))
                .map(ScreeningNoticeDTO::getScreeningTaskId)
                .collect(Collectors.toSet());
        Map<Integer, String> govDeptIdNameMap = Maps.newHashMap();
        Map<Integer, Integer> noticeIdMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(taskIds)){
            List<ScreeningTask> screeningTaskList = screeningTaskService.listByIds(taskIds);
            Set<Integer> govDeptIds = screeningTaskList.stream().map(ScreeningTask::getGovDeptId).collect(Collectors.toSet());
            Map<Integer, String> govDeptMap = govDeptService.getByIds(Lists.newArrayList(govDeptIds)).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
            screeningTaskList.forEach(screeningTask -> {
                String name = govDeptMap.get(screeningTask.getGovDeptId());
                if (StrUtil.isNotBlank(name)) {
                    govDeptIdNameMap.put(screeningTask.getId(),name);
                }
                noticeIdMap.put(screeningTask.getId(),screeningTask.getScreeningNoticeId());
            });
        }
        return TwoTuple.of(govDeptIdNameMap,noticeIdMap);
    }
}
