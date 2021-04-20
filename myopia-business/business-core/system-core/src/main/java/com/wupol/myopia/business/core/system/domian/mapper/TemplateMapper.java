package com.wupol.myopia.business.core.system.domian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.Template;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板表Mapper
 *
 * @author Simple4H
 */
public interface TemplateMapper extends BaseMapper<Template> {

    List<Template> getByType(@Param("type") Integer type);

}