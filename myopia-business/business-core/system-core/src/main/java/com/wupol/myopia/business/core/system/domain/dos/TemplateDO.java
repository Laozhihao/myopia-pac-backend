package com.wupol.myopia.business.core.system.domain.dos;

import com.wupol.myopia.business.core.system.domain.model.Template;
import com.wupol.myopia.business.core.system.domain.model.TemplateDistrict;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2022/4/18
 **/
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class TemplateDO extends Template {

    /**
     * 使用的省份
     */
    private List<TemplateDistrict> districtInfo;

    /**
     * 把template转为templateDO
     *
     * @param template 模板
     * @return com.wupol.myopia.business.core.system.domain.dos.TemplateDO
     **/
    public static TemplateDO parseFromTemplate(Template template) {
        TemplateDO templateDO = new TemplateDO();
        if (Objects.isNull(template)) {
            return templateDO;
        }
        BeanUtils.copyProperties(template, templateDO);
        return templateDO;
    }
}
