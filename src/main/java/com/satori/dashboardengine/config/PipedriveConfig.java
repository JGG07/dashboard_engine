package com.satori.dashboardengine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PipedriveConfig {

    @Value("${pipedrive.api.url}")
    private String apiUrl;

    @Value("${pipedrive.api.token}")
    private String apiToken;

    public String getApiUrl() {
        return apiUrl;
    }

    public String getApiToken() {
        return apiToken;
    }
}
