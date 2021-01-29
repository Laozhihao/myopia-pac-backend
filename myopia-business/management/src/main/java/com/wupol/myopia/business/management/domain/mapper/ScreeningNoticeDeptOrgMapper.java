package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningNoticeDeptOrg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningNoticeVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.PropertySource;

/**
 * 筛查通知通知到的部门或者机构表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningNoticeDeptOrgMapper extends BaseMapper<ScreeningNoticeDeptOrg> {

    IPage<ScreeningNoticeVo> selectPageByQuery(@Param("page")IPage<ScreeningNotice> page, @Param("param")ScreeningNoticeQuery query);

    Integer updateStatusByNoticeIdAndAcceptOrgId(@Param("screeningNoticeId") Integer noticeId, @Param("acceptOrgId") Integer govDeptId, @Param("operatorId") Integer userId, @Param("operationStatus") Integer operationStatus);
}
