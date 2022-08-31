package com.wupol.myopia.rec.server.controller;

import com.wupol.myopia.rec.server.domain.dto.RecExportDTO;
import com.wupol.myopia.rec.server.domain.vo.ApiResult;
import com.wupol.myopia.rec.server.domain.vo.RecExportVO;
import com.wupol.myopia.rec.server.facade.RecExportFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


/**
 * REC控制器
 *
 * @author hang.yuan 2022/8/9 16:16
 */
@RestController
@RequestMapping("/rec")
public class RecController {
    @Autowired
    private RecExportFacade recExportFacade;

    /**
     * 导出Rec文件
     * @param recExportDTO 导出条件
     */
    @PostMapping("/export")
    public ApiResult<RecExportVO> export(@RequestBody @Valid RecExportDTO recExportDTO) {
        return ApiResult.success(recExportFacade.recExport(recExportDTO));
    }

}
