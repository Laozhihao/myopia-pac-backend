package com.wupol.myopia.rec.facade;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.core.questionnaire.util.EpiDataUtil;
import com.wupol.myopia.rec.domain.dto.RecExportDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 导出REC门面
 *
 * @author hang.yuan 2022/8/9 16:20
 */
@Component
public class RecExportFacade {


    public void recExport(RecExportDTO recExportDTO){
        String qesPath ="";
        String recPath ="";
        List<String> headerList = Lists.newArrayList();
        List<List<String>> dataList = Lists.newArrayList();
        EpiDataUtil.exportRecFile(qesPath,recPath,headerList,dataList);
    }
}
