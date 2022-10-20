package com.wupol.myopia.business.sdk.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2022/7/11 12:09
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AccountDTO {

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

}
