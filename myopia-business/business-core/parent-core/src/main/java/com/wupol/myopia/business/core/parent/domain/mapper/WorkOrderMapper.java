package com.wupol.myopia.business.core.parent.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderDTO;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


/**
 * 工单Mapper接口
 *
 * @Author xjl
 * @Date 2022-03-04
 */
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {


    List<WorkOrder> findByCreateUserId(@Param("createUserId") Integer createUserId);

    /**
     * 查询工单分页结果
     * @param page
     * @param name 姓名
     * @param idCardOrPassport 身份证号和护照号
     * @param schoolIds 学校idList
     * @param startTime 筛查开始时间
     * @param endTime 结束时间
     * @return 工单分页结果
     */
    IPage<WorkOrderDTO> getByPage(@Param("page") Page<?> page, @Param("name") String name, @Param("idCardOrPassport") String idCardOrPassport,
                                  @Param("schoolIds") List<Integer> schoolIds, @Param("startTime") Date startTime, @Param("endTime") Date endTime,@Param("status") Integer status);
}
