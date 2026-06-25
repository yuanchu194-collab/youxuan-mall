package com.youxuan.gateway.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Gateway 鉴权配置，集中承载开发阶段允许匿名访问的接口白名单。
 */
@Component
@ConfigurationProperties(prefix = "youxuan.gateway.auth")
public class AuthProperties {

    private List<String> whiteList = new ArrayList<>();

    public List<String> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList == null ? new ArrayList<>() : whiteList;
    }
}
