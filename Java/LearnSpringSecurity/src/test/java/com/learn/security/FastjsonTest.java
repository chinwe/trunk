package com.learn.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextImpl;

import java.lang.reflect.Field;

/**
 * @author chinwe
 * 2022/8/18
 */
public class FastjsonTest {
    static ParserConfig config;

    @SneakyThrows
    @BeforeAll
    static void init() {
        config = ParserConfig.getGlobalInstance();
        config.setAutoTypeSupport(true);

        // https://github.com/LeadroyaL/fastjson-blacklist
        config.addAccept("org.springframework.security.web.savedrequest.DefaultSavedRequest");
        config.addAccept("org.springframework.security.core.context.SecurityContextImpl");
        config.addAccept("org.springframework.security.core.userdetails.User");
        config.addAccept("org.springframework.security.authentication.UsernamePasswordAuthenticationToken");
        config.addAccept("org.springframework.security.core.authority.SimpleGrantedAuthority");
        config.addAccept("org.springframework.security.web.authentication.WebAuthenticationDetails");
    }

    @Test
    public void SecurityContextImpl() throws Exception {
        String json = "{\"@type\":\"org.springframework.security.core.context.SecurityContextImpl\"}";
        JSON.parseObject(json, Object.class);

        JSON.parseObject(json, Object.class, config);
    }

    @Test
    public void SecurityContextImpl_x() throws Exception {
        String json = "{\"@type\":\"org.springframework.security.core.context.SecurityContextImpl\",\"authentication\":{\"@type\":\"org.springframework.security.authentication.UsernamePasswordAuthenticationToken\",\"authenticated\":true,\"authorities\":[{\"@type\":\"org.springframework.security.core.authority.SimpleGrantedAuthority\",\"authority\":\"ROLE_ADMIN\"}],\"details\":{\"@type\":\"org.springframework.security.web.authentication.WebAuthenticationDetails\",\"remoteAddress\":\"0:0:0:0:0:0:0:1\",\"sessionId\":\"35dbb2c4-971c-4624-bd89-2e002180a2ca\"},\"name\":\"admin\",\"principal\":{\"@type\":\"org.springframework.security.core.userdetails.User\",\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"$ref\":\"$.authentication.authorities[0]\"}],\"credentialsNonExpired\":true,\"enabled\":true,\"username\":\"admin\"}}}";
        SecurityContextImpl context = (SecurityContextImpl) JSON.parseObject(json, Object.class, config);
    }
}
