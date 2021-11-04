package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.school.management.service.VisionScreeningService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningListResponseDTO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 视力筛查
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/vision/screening")
public class VisionScreeningController {

    @Resource
    private VisionScreeningService visionScreeningService;

    /**
     * 获取学校计划
     *
     * @param pageRequest 分页请求
     * @param schoolId    学校Id
     * @return IPage<ScreeningListResponseDTO>
     */
    @GetMapping("list")
    public IPage<ScreeningListResponseDTO> getList(PageRequest pageRequest, Integer schoolId) {
        return visionScreeningService.getList(pageRequest, schoolId);
    }
}
