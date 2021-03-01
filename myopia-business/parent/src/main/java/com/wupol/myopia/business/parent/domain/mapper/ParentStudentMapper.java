package com.wupol.myopia.business.parent.domain.mapper;

import com.wupol.myopia.business.parent.domain.model.ParentStudent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.parent.domain.vo.ParentStudentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 家长学生关系表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-02-26
 */
public interface ParentStudentMapper extends BaseMapper<ParentStudent> {

    List<ParentStudentVO> countParentStudent(@Param("parentId") Integer parentId);

}
