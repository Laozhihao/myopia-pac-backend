package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningNoticeBizFacadeService;
import com.wupol.myopia.business.api.school.management.domain.vo.ScreeningNoticeListVO;
import com.wupol.myopia.business.api.school.management.facade.SchoolScreeningNoticeFacade;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 学校筛查通知
 *
 * @author hang.yuan 2022/9/27 15:26
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/screeningNotice")
public class SchoolScreeningNoticeController {

    @Autowired
    private SchoolScreeningNoticeFacade schoolScreeningNoticeFacade;

    @Autowired
    private ScreeningNoticeBizFacadeService screeningNoticeBizFacadeService;

    /**
     * 筛查通知列表
     */
    @GetMapping("/page")
    public IPage<ScreeningNoticeListVO> page(PageRequest pageRequest){
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return schoolScreeningNoticeFacade.page(currentUser,pageRequest);
    }

    /**
     * 获取关联的通知
     *
     * @return List<ScreeningNoticeDTO>
     */
    @GetMapping("planLinkNotice/list")
    public List<ScreeningNoticeDTO> getPlanLinkNoticeList() {
        Integer schoolId = CurrentUserUtil.getCurrentUser().getOrgId();
        return screeningNoticeBizFacadeService.getCanLinkNotice(schoolId, ScreeningNotice.TYPE_SCHOOL);
    }
}
