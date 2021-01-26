package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.model.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息表Mapper接口
 *
 * @author Simple4H
 */
public interface NoticeMapper extends BaseMapper<Notice> {

    IPage<Notice> getByUserId(@Param("page") Page<?> page, @Param("userId") Integer userId);

    Integer batchUpdateStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);

    List<Notice> unreadCount(@Param("status") Integer status, @Param("userId") Integer userId);
}