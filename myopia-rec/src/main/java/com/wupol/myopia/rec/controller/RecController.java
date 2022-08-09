package com.wupol.myopia.rec.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.rec.domain.dto.RecExportDTO;
import com.wupol.myopia.rec.facade.RecExportFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REC控制器
 *
 * @author hang.yuan 2022/8/9 16:16
 */
@ResponseResultBody
@RestController
@RequestMapping("/rec")
public class RecController {
    @Autowired
    private RecExportFacade recExportFacade;

    @PostMapping("/export")
    public void export(@RequestBody @Valid RecExportDTO recExportDTO){
        recExportFacade.recExport(recExportDTO);
    }
}
