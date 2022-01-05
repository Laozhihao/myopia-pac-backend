package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/05/16:43
 * @Description: 导出学校学生筛查数据
 */
@Log4j2
@Service("exportPlanSchoolStudentDataExcelService")
public class ExportPlanSchoolStudentDataExcelService extends BaseExportExcelFileService{
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
