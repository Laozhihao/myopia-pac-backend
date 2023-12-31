package com.wupol.myopia.business.core.system.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.system.domain.model.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 消息表Mapper接口
 *
 * @author Simple4H
 */
public interface NoticeMapper extends BaseMapper<Notice> {

    IPage<Notice> getByNoticeUserId(@Param("page") Page<?> page, @Param("userId") Integer userId);

    Integer batchUpdateStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);

    List<Notice> unreadCount(@Param("status") Integer status, @Param("userId") Integer userId);

    Integer updateNotice(@Param("noticeUserId") Integer noticeUserId, @Param("linkId") Integer linkId);

    void batchCreateNotice(@Param("createUserId") Integer createUserId, @Param("linkId") Integer linkId,
                                    @Param("toUserIds") List<Integer> toUserIds, @Param("type") Byte type,
                                    @Param("title") String title, @Param("content") String content,
                                    @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}