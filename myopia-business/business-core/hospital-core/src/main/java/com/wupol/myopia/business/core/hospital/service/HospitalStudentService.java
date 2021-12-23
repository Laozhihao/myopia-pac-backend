package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dos.HospitalStudentDO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.HospitalStudentMapper;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalStudentQuery;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 医院-学生管理
 *
 * @Author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class HospitalStudentService extends BaseService<HospitalStudentMapper, HospitalStudent> {

    /**
     * 获取HospitalStudentDTO的数据
     */
    public List<HospitalStudentDO> getHospitalStudentDoList(HospitalStudentQuery query) {
        return baseMapper.getHospitalStudentDoList(query);
    }

    /**
     * 该医院已建档的学生的map数据
     * 1. key是studentId
     * 2. value是HospitalStudentVo
     */
    public Map<Integer, HospitalStudentDO> getHospitalStudentVoMap(HospitalStudentQuery query) {
        return getHospitalStudentDoList(query).stream()
                .collect(Collectors.toMap(HospitalStudentDO::getStudentId, Function.identity()));

    }

    /**
     * 保存医院与学生的关系
     */
    public void saveHospitalStudentArchive(Integer hospitalId, Integer studentId) {
        saveOrUpdate(new HospitalStudent(hospitalId, studentId));
    }

    /**
     * 校验学生与医院关系
     */
    public Boolean existHospitalAndStudentRelationship(Integer hospitalId, Integer studentId) {
        HospitalStudent student = findOne(new HospitalStudent(hospitalId, studentId));
        return Objects.nonNull(student);
    }

    public List<HospitalStudent> getBy(HospitalStudentQuery query) {
        return baseMapper.getBy(query);
    }

    /**
     * 通过身份证查找学生
     *
     * @param idCard 身份证
     * @return Student
     */
    public HospitalStudent getByIdCard(String idCard) {
        return findOne(new HospitalStudent().setIdCard(idCard));
    }

    /**
     * 获取医院学生
     *
     * @param pageRequest 分页请求
     * @param requestDTO  条件
     * @return IPage<HospitalStudentResponseDTO>
     */
    public IPage<HospitalStudentResponseDTO> getByList(PageRequest pageRequest, HospitalStudentRequestDTO requestDTO) {
        return baseMapper.getByList(pageRequest.toPage(), requestDTO);
    }

    /**
     * 通过Id删除学生
     *
     * @param id 医院学生Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletedById(Integer id) {
        baseMapper.deletedById(id);
    }

    /**
     * 通过Id获取医院学生
     *
     * @param id 医院学生Id
     * @return HospitalStudentResponseDTO
     */
    public HospitalStudentResponseDTO getByHospitalStudentId(Integer id) {
        return baseMapper.getByHospitalStudentId(id);
    }

    /**
     * 更新绑定家长手机号码
     *
     * @param studentId   学生ID
     * @param parentPhone 家长手机号码
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMpParentPhone(Integer studentId, String parentPhone) {
        List<HospitalStudent> hospitalStudents = baseMapper.getByStudentId(studentId);
        if (CollectionUtils.isEmpty(hospitalStudents)) {
            return;
        }
        hospitalStudents.forEach(hospitalStudent -> {
            String parentPhoneStr = hospitalStudent.getMpParentPhone();
            if (StringUtils.isBlank(parentPhoneStr)) {
                // 为空新增
                hospitalStudent.setMpParentPhone(parentPhone);
            } else {
                // 家长手机号码是否已经存在
                if (StringUtils.countMatches(parentPhoneStr, parentPhone) == 0) {
                    // 不存在拼接家长手机号码
                    hospitalStudent.setMpParentPhone(parentPhoneStr + "," + parentPhone);
                }
            }
        });
        updateBatchById(hospitalStudents);
    }
}