package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.base.domain.vo.FamilyInfoVO;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.dto.ReferralDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.ReferralRecordMapper;
import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


/**
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Service
public class ReferralRecordService extends BaseService<ReferralRecordMapper, ReferralRecord> {

    /**
     * 获取转诊单详情
     * @param id
     * @return
     */
    public ReferralDTO getDetails(Integer id) {
        ReferralDTO details = baseMapper.getDetails(id);
        FamilyInfoVO familyInfo = details.getFamilyInfo();
        if (Objects.nonNull(familyInfo) && CollectionUtils.isNotEmpty(familyInfo.getMember())) {
            List<FamilyInfoVO.MemberInfo> members = familyInfo.getMember();
            // 获取家长信息，member中第一个为父亲数据，第二个为母亲数据，优先取父亲数据
            for (FamilyInfoVO.MemberInfo member : members) {
                if (StringUtils.isBlank(details.getParentName())) {
                    details.setParentName(member.getName());
                }
                if (StringUtils.isBlank(details.getParentPhone())) {
                    details.setParentPhone(member.getPhone());
                }
                if (StringUtils.isNoneBlank(details.getParentName(), details.getParentPhone())) {
                    break;
                }
            }
        }
        return details;
    }

}
