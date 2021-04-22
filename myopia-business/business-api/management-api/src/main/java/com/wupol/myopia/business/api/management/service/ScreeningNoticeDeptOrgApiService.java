package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningNoticeVO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.government.domain.model.District;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.DistrictService;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScreeningNoticeDeptOrgApiService {

    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningNoticeVO> getPage(ScreeningNoticeQueryDTO query, PageRequest pageRequest) {
        Page<ScreeningNotice> page = (Page<ScreeningNotice>) pageRequest.toPage();
        IPage<ScreeningNoticeDTO> screeningNoticeIPage = screeningNoticeDeptOrgService.selectPageByQuery(page, query);
        List<Integer> allGovDeptIds = screeningNoticeIPage.getRecords().stream().filter(vo -> ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType())).map(ScreeningNoticeDTO::getAcceptOrgId).distinct().collect(Collectors.toList());
        Map<Integer, String> govDeptIdNameMap = CollectionUtils.isEmpty(allGovDeptIds) ? Collections.emptyMap() : govDeptService.getByIds(allGovDeptIds).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        // 设置地址信息
        IPage<ScreeningNoticeVO> screeningNoticeVOIPage = screeningNoticeIPage.convert(dto -> {
            ScreeningNoticeVO vo = new ScreeningNoticeVO(dto);
            List<District> districtPositionDetailById = districtService.getDistrictPositionDetailById(vo.getDistrictId());
            vo.setDistrictDetail(districtPositionDetailById).setDistrictName(districtService.getDistrictNameByDistrictPositionDetail(districtPositionDetailById));
            if (ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType())) {
                vo.setGovDeptName(govDeptIdNameMap.getOrDefault(vo.getAcceptOrgId(), ""));
            }
            return vo;
        });
        return screeningNoticeVOIPage;
    }

}
