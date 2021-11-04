package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.school.management.service.VisionScreeningService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningListResponseDTO;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import org.springframework.web.bind.annotation.*;

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

    @Resource
    private SchoolVisionStatisticService schoolVisionStatisticService;

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

    /**
     * 获取结果统计分析
     *
     * @param schoolStatisticId 结果统计
     * @return SchoolVisionStatistic
     */
    @GetMapping("{schoolStatisticId}")
    public SchoolVisionStatistic getSchoolStatistic(@PathVariable("schoolStatisticId") Integer schoolStatisticId) {
        return schoolVisionStatisticService.getById(schoolStatisticId);
    }
}
