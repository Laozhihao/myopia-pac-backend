package com.wupol.myopia.business.api.management.constant;

import com.wupol.myopia.base.constant.HospitalServiceType;
import com.wupol.myopia.base.constant.OverviewConfigType;
import com.wupol.myopia.base.constant.PermissionTemplateType;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrgConfigTypeEnum;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Template org config map
 *
 * @author Simple4H
 */
@UtilityClass
public class TemplateConfigType {

    public static final Map<Integer, Integer> TEMPLATE_TO_ORG_CONFIG_TYPE = new HashMap<>();

    public static final Map<Integer, Integer> TEMPLATE_TO_HOSPITAL_CONFIG_TYPE = new HashMap<>();

    public static final Map<Integer, Integer> TEMPLATE_TO_OVERVIEW_CONFIG_TYPE = new HashMap<>();

    static {
        TEMPLATE_TO_ORG_CONFIG_TYPE.put(PermissionTemplateType.SCREENING_ORGANIZATION.getType(), ScreeningOrgConfigTypeEnum.CONFIG_TYPE_0.getType());
        TEMPLATE_TO_ORG_CONFIG_TYPE.put(PermissionTemplateType.SCREENING_ORG_SINGLE.getType(), ScreeningOrgConfigTypeEnum.CONFIG_TYPE_1.getType());
        TEMPLATE_TO_ORG_CONFIG_TYPE.put(PermissionTemplateType.SCREENING_ORG_VS666.getType(), ScreeningOrgConfigTypeEnum.CONFIG_TYPE_2.getType());
        TEMPLATE_TO_ORG_CONFIG_TYPE.put(PermissionTemplateType.SCREENING_ORG_SINGLE_AND_VS666.getType(), ScreeningOrgConfigTypeEnum.CONFIG_TYPE_3.getType());
    }

    static {
        TEMPLATE_TO_HOSPITAL_CONFIG_TYPE.put(PermissionTemplateType.HOSPITAL_ADMIN.getType(), HospitalServiceType.RESIDENT.getType());
        TEMPLATE_TO_HOSPITAL_CONFIG_TYPE.put(PermissionTemplateType.PRESCHOOL_ADMIN.getType(), HospitalServiceType.PRESCHOOL.getType());
        TEMPLATE_TO_HOSPITAL_CONFIG_TYPE.put(PermissionTemplateType.HOSPITAL_PRESCHOOL_ADMIN.getType(), HospitalServiceType.RESIDENT_PRESCHOOL.getType());
    }

    static {
        TEMPLATE_TO_OVERVIEW_CONFIG_TYPE.put(PermissionTemplateType.OVERVIEW_SCREENING_ORG.getType(), OverviewConfigType.SCREENING_ORG.getType());
        TEMPLATE_TO_OVERVIEW_CONFIG_TYPE.put(PermissionTemplateType.OVERVIEW_HOSPITAL.getType(), OverviewConfigType.HOSPITAL.getType());
        TEMPLATE_TO_OVERVIEW_CONFIG_TYPE.put(PermissionTemplateType.OVERVIEW_SCREENING_ORG_HOSPITAL.getType(), OverviewConfigType.SCREENING_ORG_HOSPITAL.getType());
    }

}
