package com.wupol.myopia.business.aggregation.screening.domain.dto;

/**
 * @Classname CredentialTypeAndContent
 * @Description
 * @Date 2022/2/26 6:53 下午
 * @Author Jacob
 * @Version
 */

import com.wupol.myopia.base.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 证件相关实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CredentialTypeAndContent{
    /**
     * 证件类型
     */
    private CredentialType credentialType;
    /**
     * 证件内容
     */
    private String credentialContent;
    /**
     * id
     */
    private String idCard;
    /**
     * 护照
     */
    private String passport;


    public static CredentialTypeAndContent getInstance(String idCard, String passport) {
        if(com.wupol.framework.core.util.StringUtils.allHasLength(idCard,passport)) {
            throw new BusinessException("无法判断是什么类型的证件号, idCard = " + idCard + ", passport = " + passport ) ;
        }
        if (StringUtils.isNotBlank(idCard)) {
            return new CredentialTypeAndContent(CredentialType.ID_CARD,idCard,idCard,passport);

        }
        if (StringUtils.isNotBlank(passport)){
            return new CredentialTypeAndContent(CredentialType.PASSPORT,passport,idCard,passport);
        } else {
            return null;
        }
    }
}
