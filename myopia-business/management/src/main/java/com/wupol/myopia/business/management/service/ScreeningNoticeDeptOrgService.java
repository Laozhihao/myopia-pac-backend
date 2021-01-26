package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.management.domain.mapper.ScreeningNoticeDeptOrgMapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQuery;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningNoticeVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class ScreeningNoticeDeptOrgService extends BaseService<ScreeningNoticeDeptOrgMapper, ScreeningNoticeDeptOrg> {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private GovDeptService govDeptService;
    /**
     * 分页查询
     * @param query
     * @param pageNum
     * @param pageSize
     * @return
     */
    public IPage<ScreeningNoticeVo> getPage(ScreeningNoticeQuery query, Integer pageNum, Integer pageSize) {
        Page<ScreeningNotice> page = new Page<>(pageNum, pageSize);
        IPage<ScreeningNoticeVo> screeningNoticeIPage = baseMapper.selectPageByQuery(page, query);
        Map<Integer, String> districtIdNameMap = districtService.getAllDistrictIdNameMap();
//        Map<Integer, String> govDeptIdNameMap = govDeptService.getByIds(allGovDeptIds).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        screeningNoticeIPage.getRecords().forEach(vo -> {
            vo.setDistrictName(districtIdNameMap.getOrDefault(vo.getDistrictId(), ""));
            if (ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType())) {
//                vo.setGovDeptName(govDeptIdNameMap.getOrDefault(vo.getAcceptOrgId(), ""));
            }
        });
        return screeningNoticeIPage;
    }
}
