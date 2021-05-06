package com.wupol.myopia.business.api.hospital.app.facade;

import com.wupol.framework.core.util.DateFormatUtil;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.hospital.app.domain.vo.MedicalReportVO;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.dos.MedicalReportDO;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import com.wupol.myopia.business.core.hospital.domain.query.MedicalReportQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.MedicalRecordService;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021/4/21
 **/
@Service
public class MedicalReportFacade {

    @Autowired
    private MedicalReportService medicalReportService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private MedicalRecordService medicalRecordService;


}
