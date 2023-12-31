package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.dos.ScreeningOrgCountDO;
import com.wupol.myopia.business.core.hospital.domain.dto.CooperationHospitalDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.CooperationHospitalRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.OrgCooperationHospitalMapper;
import com.wupol.myopia.business.core.hospital.domain.model.OrgCooperationHospital;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 筛查机构合作医院Service
 *
 * @author Simple4H
 */
@Service
public class OrgCooperationHospitalService extends BaseService<OrgCooperationHospitalMapper, OrgCooperationHospital> {

    /**
     * 新增合作医院
     *
     * @param requestDTO 请求入参
     * @return 是否新增成功
     */
    @Transactional
    public boolean saveCooperationHospital(CooperationHospitalRequestDTO requestDTO) {

        Integer screeningOrgId = requestDTO.getScreeningOrgId();
        List<Integer> hospitalIds = requestDTO.getHospitalIds();
        if (Objects.isNull(screeningOrgId) || CollectionUtils.isEmpty(hospitalIds)) {
            throw new BusinessException("数据不能为空");
        }
        // 检查是否重复
        if (baseMapper.countByOrgIdAndHospitalIds(screeningOrgId, hospitalIds) > 0) {
            throw new BusinessException("存在重复医院，请确认");
        }
        return baseMapper.batchSaveOrgCooperationHospital(screeningOrgId, hospitalIds) > 0;
    }

    /**
     * 删除合作医院
     *
     * @param id Id
     * @return 是否删除成功
     */
    @Transactional
    public boolean deletedCooperationHospital(Integer id) {
        return baseMapper.deleteById(id) > 0;
    }


    /**
     * 置顶医院
     *
     * @param id 合作医院Id
     * @return 是否置顶成功
     */
    @Transactional
    public boolean topCooperationHospital(Integer id) {
        OrgCooperationHospital orgCooperationHospital = baseMapper.selectById(id);
        if (Objects.isNull(orgCooperationHospital)) {
            throw new BusinessException("合作医院数据异常");
        }
        // 将其他的置顶医院取消
        baseMapper.updateByScreeningOrgId(orgCooperationHospital.getScreeningOrgId());
        // 将目标医院更新
        orgCooperationHospital.setIsTop(1);
        return baseMapper.updateById(orgCooperationHospital) > 0;
    }

    /**
     * 筛查机构获取合作医院列表
     *
     * @param pageRequest    分页入参
     * @param screeningOrgId 筛查机构Id
     * @return IPage<CooperationHospitalDTO>
     */
    public IPage<CooperationHospitalDTO> getCooperationHospitalListByPage(PageRequest pageRequest, Integer screeningOrgId) {
        return baseMapper.getByScreeningOrgId(pageRequest.toPage(), screeningOrgId);
    }

    /**
     * 筛查机构获取合作医院列表
     *
     * @param screeningOrgId 筛查机构Id
     * @return List<OrgCooperationHospital>
     */
    public List<OrgCooperationHospital> getCooperationHospitalList(Integer screeningOrgId) {
        return baseMapper.getListByScreeningOrgId(screeningOrgId);
    }

    /**
     * 通过筛查机构统计合作医院
     *
     * @param screeningOrgId 筛查机构Id
     * @return 总数
     */
    public Integer countCooperationHospital(Integer screeningOrgId) {
        return baseMapper.countByScreeningOrgId(screeningOrgId);
    }

    /**
     * 通过筛查机构统计合作医院
     *
     * @param screeningOrgIds 筛查机构Id集合
     * @return 总数
     */
    public Map<Integer, Integer> countByScreeningOrgIdList(List<Integer> screeningOrgIds) {
        return baseMapper.countByScreeningOrgIdList(screeningOrgIds).stream().collect(Collectors.toMap(ScreeningOrgCountDO::getScreeningOrgId, ScreeningOrgCountDO::getCount));
    }

    /**
     * 获取推荐医院
     *
     * @param screeningOrgId 筛查机构Id
     * @return 医院Id
     */
    public Integer getSuggestHospital(Integer screeningOrgId) {
        return baseMapper.getSuggestHospital(screeningOrgId);
    }

    /**
     * 通过医院Id删除数据
     *
     * @param hospitalId 医院Id
     */
    public void deletedHospital(Integer hospitalId) {
        baseMapper.deletedByHospitalId(hospitalId);
    }
}
