package com.wupol.myopia.oauth.service;

import lombok.SneakyThrows;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;

import javax.sql.DataSource;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
public class JdbcClientDetailsServiceImpl extends JdbcClientDetailsService {

    public JdbcClientDetailsServiceImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    @SneakyThrows
    public ClientDetails loadClientByClientId(String clientId)  {
        return super.loadClientByClientId(clientId);
    }
}
