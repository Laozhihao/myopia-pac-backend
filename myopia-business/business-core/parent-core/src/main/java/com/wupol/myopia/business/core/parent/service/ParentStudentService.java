package com.wupol.myopia.business.core.parent.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.parent.domain.mapper.ParentStudentMapper;
import com.wupol.myopia.business.core.parent.domain.model.ParentStudent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 家长端-家长查看学生信息
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class ParentStudentService extends BaseService<ParentStudentMapper, ParentStudent> {

    /**
     * 家长绑定学生
     *
     * @param studentId 学生ID
     * @param parentId  家长ID
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void parentBindStudent(Integer studentId, Integer parentId) {
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

    /**
     * 通过家长ID获取学生ID
     *
     * @param parentId 家长ID
     * @return 学生ID列表
     */
    public List<Integer> getStudentIdByParentId(Integer parentId) {
        return baseMapper.getByParentId(parentId);
    }
}