package com.wupol.myopia.business.core.parent.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.parent.domain.dto.CountParentStudentResponseDTO;
import com.wupol.myopia.business.core.parent.domain.dto.ParentStudentDTO;
import com.wupol.myopia.business.core.parent.domain.mapper.ParentStudentMapper;
import com.wupol.myopia.business.core.parent.domain.model.ParentStudent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 家长端-家长查看学生信息
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class ParentStudentService extends BaseService<ParentStudentMapper, ParentStudent> {

    /**
     * 孩子统计、孩子列表
     *
     * @param parentId 家长ID
     * @return CountParentStudentResponseDTO 家长端-统计家长绑定学生
     */
    public CountParentStudentResponseDTO countParentStudent(Integer parentId) {
        CountParentStudentResponseDTO responseDTO = new CountParentStudentResponseDTO();
        List<ParentStudentDTO> parentStudentDTOS = baseMapper.countParentStudent(parentId);
        responseDTO.setTotal(parentStudentDTOS.size());
        responseDTO.setItem(parentStudentDTOS);
        return responseDTO;
    }

    /**
     * 家长绑定学生
     *
     * @param studentId 学生ID
     * @param parentId  家长ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void parentBindStudent(Integer studentId, Integer parentId) {
        ParentStudent parentStudent = new ParentStudent();
        if (null == parentId || null == studentId) {
            throw new BusinessException("数据异常");
        }
        ParentStudent checkResult = baseMapper.getByParentIdAndStudentId(parentId, studentId);
        if (null != checkResult) {
            return;
        }
        parentStudent.setParentId(parentId);
        parentStudent.setStudentId(studentId);

        baseMapper.insert(parentStudent);
    }
}