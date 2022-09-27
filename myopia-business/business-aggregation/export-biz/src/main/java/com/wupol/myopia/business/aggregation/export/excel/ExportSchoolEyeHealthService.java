package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.school.domain.dto.EyeHealthDataExportDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 眼健康中心数据
 *
 * @author Simple4H
 */
@Service(ExportExcelServiceNameConstant.EXPORT_SCHOOL_EYE_HEALTH_SERVICE)
public class ExportSchoolEyeHealthService extends BaseExportExcelFileService {

    @Resource
    private SchoolStudentService schoolStudentService;

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return PDFFileNameConstant.SCHOOL_EYE_HEALTH;
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_EXCEL_SCHOOL_EYE_HEALTH,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getSchoolId());
    }

    @Override
    public List getExcelData(ExportCondition exportCondition) {
        Integer schoolId = exportCondition.getSchoolId();
        List<SchoolStudent> schoolStudents = schoolStudentService.getBySchoolId(schoolId);

        return schoolStudents.stream().map(s -> {
            EyeHealthDataExportDTO exportDTO = new EyeHealthDataExportDTO();
            exportDTO.setSno(s.getSno());
            exportDTO.setName(s.getName());
            exportDTO.setGender(GenderEnum.getCnName(s.getGender()));
            exportDTO.setBirthday(DateFormatUtil.format(s.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE2));
//            exportDTO.setGradeAndClass();
//            exportDTO.setScreeningTime();
//            exportDTO.setWearingGlasses();
//            exportDTO.setLowVision();
//            exportDTO.setLowVisionResult();
//            exportDTO.setSph();
//            exportDTO.setCyl();
//            exportDTO.setAxial();
//            exportDTO.setRefractiveResult();
//            exportDTO.setCorrectedVision();
//            exportDTO.setCorrectedVisionResult();
//            exportDTO.setWarningLevel();
//            exportDTO.setReview();
//            exportDTO.setGlassesType();
//            exportDTO.setHeight();
//            exportDTO.setDesk();
//            exportDTO.setChair();
//            exportDTO.setSeat();
//            exportDTO.setIsBindMp();
            return exportDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public Class getHeadClass(ExportCondition exportCondition) {
        return EyeHealthDataExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return PDFFileNameConstant.SCHOOL_EYE_HEALTH;
    }
}
