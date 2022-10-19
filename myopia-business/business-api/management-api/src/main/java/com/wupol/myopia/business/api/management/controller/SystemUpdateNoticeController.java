package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.dto.SystemNoticeSaveRequestDTO;
import com.wupol.myopia.business.core.common.domain.dto.SystemUpdateNoticeListResponse;
import com.wupol.myopia.business.core.common.service.SystemUpdateNoticeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 系统更新通知
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/systemNotice")
@Validated
public class SystemUpdateNoticeController {

    @Resource
    private SystemUpdateNoticeService systemUpdateNoticeService;

    /**
     * 获取列表
     *
     * @param pageRequest 分页请求
     *
     * @return IPage<SystemUpdateNotice>
     */
    @GetMapping("list")
    public IPage<SystemUpdateNoticeListResponse> getList(PageRequest pageRequest) {
        return systemUpdateNoticeService.getList(pageRequest);
    }

    /**
     * 保存通知
     *
     * @param requestDTO 请求入参
     */
    @PostMapping("save")
    public void saveNotice(@RequestBody SystemNoticeSaveRequestDTO requestDTO) {

        systemUpdateNoticeService.saveNotice(requestDTO, CurrentUserUtil.getCurrentUser().getId());
    }

    /**
     * 下线通知
     *
     * @param id id
     */
    @PostMapping("status/{id}")
    public void offlineNotice(@PathVariable("id") Integer id) {
        systemUpdateNoticeService.offlineNotice(id);
    }
}
