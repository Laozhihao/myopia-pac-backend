package com.wupol.myopia.business.core.hospital.util;

import com.wupol.myopia.base.domain.vo.FamilyInfoVO;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.hospital.domain.interfaces.HasParentInfoInterface;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2022/1/10 18:00
 */
@UtilityClass
public class HospitalUtil {

    /**
     * 通过获取家长信息
     * @param familyInfo
     * @return
     */
    public static TwoTuple<String, String> getParentNameAndPhone(FamilyInfoVO familyInfo) {
        if (Objects.nonNull(familyInfo) && CollectionUtils.isNotEmpty(familyInfo.getMember())) {
            String name = null;
            String phone = null;
            List<FamilyInfoVO.MemberInfo> members = familyInfo.getMember();
            // 获取家长信息，member中第一个为父亲数据，第二个为母亲数据，优先取父亲数据
            for (FamilyInfoVO.MemberInfo member : members) {
                if (StringUtils.isBlank(name)) {
                    name = member.getName();
                }
                if (StringUtils.isBlank(phone)) {
                    phone = member.getPhone();
                }
                if (StringUtils.isNoneBlank(name, phone)) {
                    break;
                }
            }
            return TwoTuple.of(name, phone);
        }
        return null;
    }

    /**
     * 设置学生家长信息
     * @param parentInfo
     */
    public static void setParentInfo(HasParentInfoInterface parentInfo) {
        if (Objects.isNull(parentInfo)) return;
        TwoTuple<String, String> parentNameAndPhone = HospitalUtil.getParentNameAndPhone(parentInfo.getFamilyInfo());
        if (Objects.nonNull(parentNameAndPhone)) {
            parentInfo.setParentName(parentNameAndPhone.getFirst());
            parentInfo.setParentPhone(parentNameAndPhone.getSecond());
        }
    }

    /**
     * 获取map中id对应名称
     * @param ids
     * @param idAndNameMap
     * @param separator  分割符
     * @return
     */
    public static String getName(Set<Integer> ids, Map<Integer, String> idAndNameMap, String separator) {
        String collect = ids.stream().map(idAndNameMap::get).filter(StringUtils::isNotBlank).collect(Collectors.joining(separator));
        return StringUtils.isBlank(collect) ? null : collect;
    }

}
