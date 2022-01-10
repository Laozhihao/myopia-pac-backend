package com.wupol.myopia.business.core.hospital.domain.interfaces;

import com.wupol.myopia.base.domain.vo.FamilyInfoVO;

/**
 * @Author wulizhou
 * @Date 2022/1/10 18:11
 */
public interface HasParentInfoInterface {

    String getParentName();

    void setParentName(String parentName);

    String getParentPhone();

    void setParentPhone(String parentPhone);

    FamilyInfoVO getFamilyInfo();

    void setFamilyInfo(FamilyInfoVO familyInfo);

}
