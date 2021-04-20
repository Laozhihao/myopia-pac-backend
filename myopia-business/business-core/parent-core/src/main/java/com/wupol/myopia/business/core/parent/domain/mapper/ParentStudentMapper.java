package com.wupol.myopia.business.core.parent.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.parent.domain.dto.ParentStudentDTO;
import com.wupol.myopia.business.core.parent.domain.model.ParentStudent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 家长学生关系表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-02-26
 */
public interface ParentStudentMapper extends BaseMapper<ParentStudent> {

    List<ParentStudentDTO> countParentStudent(@Param("parentId") Integer parentId);

    ParentStudent getByParentIdAndStudentId(@Param("parentId") Integer parentId, @Param("studentId") Integer studentId);

}