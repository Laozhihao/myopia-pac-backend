package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 导出vs666数据
 *
 * @author tastyb
 */
@Service("vsDataExcelService")
public class VsDataExcelService extends BaseExportExcelFileService {
    @Override
    public List getExcelData(ExportCondition exportCondition) {

        return null;
    }

    @Override
    public Class getHeadClass() {
        return null;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return null;
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return null;
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return null;
    }
}
