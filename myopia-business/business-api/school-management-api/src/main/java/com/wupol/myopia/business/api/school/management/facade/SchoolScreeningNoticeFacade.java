package com.wupol.myopia.business.api.school.management.facade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.api.school.management.domain.vo.ScreeningNoticeListVO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final SchoolService schoolService;

    /**
     * 分页获取筛查通知列表
     * @param currentUser 当前用户
     */
    public IPage<ScreeningNoticeListVO> page(CurrentUser currentUser, PageRequest pageRequest) {
        ScreeningNoticeQueryDTO query = new ScreeningNoticeQueryDTO();
        query.setGovDeptId(currentUser.getOrgId());
        IPage<ScreeningNoticeDTO> screeningNoticePage = screeningNoticeDeptOrgService.selectPageByQuery(pageRequest.toPage(), query);

        IPage<ScreeningNoticeListVO> screeningNoticeListVoPage = new Page<>(screeningNoticePage.getCurrent(),screeningNoticePage.getSize(),screeningNoticePage.getTotal());
        List<ScreeningNoticeDTO> records = screeningNoticePage.getRecords();
        if (CollUtil.isEmpty(records)){
            return screeningNoticeListVoPage;
        }

        // 政府部门名称
        Map<Integer, String> govDeptIdNameMap = getGovDeptIdNameMap(records);
        Map<Integer, School> schoolMap = getSchoolMap(records);
        List<ScreeningNoticeListVO> screeningNoticeListVOList = records.stream().map(screeningNoticeDTO -> getScreeningNoticeListVO(govDeptIdNameMap, screeningNoticeDTO,schoolMap)).collect(Collectors.toList());
        screeningNoticeListVoPage.setRecords(screeningNoticeListVOList);
        return screeningNoticeListVoPage;
    }

    /**
     * 获取筛查通知列表对象
     * @param govDeptIdNameMap
     * @param screeningNoticeDTO
     */
    private ScreeningNoticeListVO getScreeningNoticeListVO(Map<Integer, String> govDeptIdNameMap, ScreeningNoticeDTO screeningNoticeDTO,Map<Integer, School> schoolMap) {
        School school = schoolMap.get(screeningNoticeDTO.getAcceptOrgId());
        //TODO: 学校配置
        Boolean canCreatePlan = Optional.ofNullable(school)
//                .map(x -> StringUtils.isNotBlank(x.getScreeningTypeConfig()) && x.getScreeningTypeConfig().contains(String.valueOf(screeningNoticeDTO.getScreeningType())))
                .map(x -> Boolean.TRUE)
                .orElse(Boolean.FALSE);
        return new ScreeningNoticeListVO()
                .setId(screeningNoticeDTO.getId())
                .setTitle(screeningNoticeDTO.getTitle())
                .setContent(screeningNoticeDTO.getContent())
                .setStartTime(screeningNoticeDTO.getStartTime())
                .setEndTime(screeningNoticeDTO.getEndTime())
                .setStatus(screeningNoticeDTO.getOperationStatus())
                .setAcceptTime(screeningNoticeDTO.getAcceptTime())
                .setNoticeDeptName(govDeptIdNameMap.getOrDefault(screeningNoticeDTO.getScreeningNoticeDeptOrgId(), StrUtil.EMPTY))
                .setCanCreatePlan(canCreatePlan)
                .setScreeningType(screeningNoticeDTO.getScreeningType());
    }

    /**
     * 政府部门名称
     * @param records
     */
    private Map<Integer, String> getGovDeptIdNameMap(List<ScreeningNoticeDTO> records) {
        List<Integer> allGovDeptIds = records.stream()
                .filter(vo -> ScreeningNotice.TYPE_ORG.equals(vo.getType()))
                .map(ScreeningNoticeDTO::getAcceptOrgId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> govDeptIdNameMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(allGovDeptIds)){
            govDeptIdNameMap = govDeptService.getByIds(allGovDeptIds).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        }
        return govDeptIdNameMap;
    }

    /**
     * 获取学校信息
     * @param records
     */
    private Map<Integer, School> getSchoolMap(List<ScreeningNoticeDTO> records) {
        List<Integer> schoolIds = records.stream()
                .filter(vo -> ScreeningNotice.TYPE_SCHOOL.equals(vo.getType()))
                .map(ScreeningNoticeDTO::getScreeningNoticeDeptOrgId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, School> govDeptIdNameMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(schoolIds)){
            govDeptIdNameMap = schoolService.getByIds(schoolIds).stream().collect(Collectors.toMap(School::getId, Function.identity()));
        }
        return govDeptIdNameMap;
    }
}
