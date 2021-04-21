package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.dos.HospitalStudentDO;
import com.wupol.myopia.business.core.hospital.domain.mapper.HospitalStudentMapper;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalStudentQuery;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 医院的学生管理的App接口
 *
 * @Author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class HospitalStudentService extends BaseService<HospitalStudentMapper, HospitalStudent> {

    /** 获取HospitalStudentDTO的数据 */
    public List<HospitalStudentDO> getHospitalStudentDoList(HospitalStudentQuery query) {
        return baseMapper.getHospitalStudentDoList(query);
    }

    /**
     *  该医院已建档的学生的map数据
     *  1. key是studentId
     *  2. value是HospitalStudentVo
     */
    public Map<Integer, HospitalStudentDO> getHospitalStudentVoMap(HospitalStudentQuery query) {
        return getHospitalStudentDoList(query).stream()
                .collect(Collectors.toMap(HospitalStudentDO::getStudentId, Function.identity()));

    }

    /** 保存医院与学生的 */
    public void saveHospitalStudentArchive(Integer hospitalId, Integer studentId) {
        // 保存医院与学生的关系
        saveOrUpdate(new HospitalStudent(hospitalId, studentId));
    }

    /** 校验学生与医院关系 */
    public Boolean existHospitalAndStudentRelationship(Integer hospitalId, Integer studentId) throws IOException {
        HospitalStudent student = findOne(new HospitalStudent(hospitalId, studentId));
        return Objects.nonNull(student);
    }

    public List<HospitalStudent> getBy(HospitalStudentQuery query) {
        return baseMapper.getBy(query);
    }

}