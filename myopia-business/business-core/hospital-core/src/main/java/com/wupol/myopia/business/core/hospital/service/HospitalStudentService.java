package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.constant.StudentTypeEnum;
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

import java.util.*;
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

    /**
     * 通过学生Id获取患者
     *
     * @param studentId 学生
     * @return 患者
     */
    public List<HospitalStudent> getByStudentId(Integer studentId) {
        return baseMapper.getByStudentId(studentId);
    }

    /**
     * 通过医院id跟学生id获取医院学生信息
     * @param hospitalId
     * @param studentId
     * @return
     */
    public HospitalStudentResponseDTO getByHospitalIdAndStudentId(Integer hospitalId, Integer studentId) {
        HospitalStudentResponseDTO hospitalStudent = baseMapper.getByHospitalIdAndStudentId(hospitalId, studentId);
        hospitalStudent.setBirthdayInfo(DateUtil.getAgeInfo(hospitalStudent.getBirthday(), new Date()));
        return hospitalStudent;
    }


    /**
     * 获取学生类型
     *
     * @param clientIdStr 登录用户
     * @param studentType 学生类型
     * @return Integer
     */
    public Integer getStudentType(String clientIdStr, Integer studentType) {
        if (StringUtils.isBlank(clientIdStr)) {
            return null;
        }
        log.info("clientIdStr:{}", clientIdStr);
        Integer clientId = Integer.valueOf(clientIdStr);
        if (Objects.isNull(studentType)) {
            // 医院端
            if (SystemCode.HOSPITAL_CLIENT.getCode().equals(clientId)) {
                return StudentTypeEnum.HOSPITAL_TYPE.getType();
            }
            // 0到6岁
            if (SystemCode.PRESCHOOL_CLIENT.getCode().equals(clientId)) {
                return StudentTypeEnum.PRESCHOOL_TYPE.getType();
            }
        } else {
            // 学生类型是医院端，当前登录用户为0到6岁，则更新
            if (StudentTypeEnum.HOSPITAL_TYPE.getType().equals(studentType) && SystemCode.PRESCHOOL_CLIENT.getCode().equals(clientId)) {
                return StudentTypeEnum.HOSPITAL_AND_PRESCHOOL.getType();
            }

            // 学生类型是0到6岁，当前登录用户为医院端，则更新
            if (StudentTypeEnum.PRESCHOOL_TYPE.getType().equals(studentType) && SystemCode.HOSPITAL_CLIENT.getCode().equals(clientId)) {
                return StudentTypeEnum.HOSPITAL_AND_PRESCHOOL.getType();
            }
        }
        return null;
    }

    /**
     * 获取0-6岁患者
     *
     * @return List<HospitalStudent>
     */
    public List<HospitalStudent> getByStudentType(Date startDate) {
        return baseMapper.getPreschoolByStudentType(startDate);
    }

    /**
     * 通过学生Id获取学生
     *
     * @param studentIds 学生Ids
     * @return List<HospitalStudent>
     */
    public List<HospitalStudent> getByStudentIds(List<Integer> studentIds) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return new ArrayList<>();
        }
        return baseMapper.getByStudentIds(studentIds);
    }
}