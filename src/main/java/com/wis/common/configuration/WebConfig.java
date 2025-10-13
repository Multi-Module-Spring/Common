package com.wis.common.configuration;

import com.wis.common.executation.interceptor.PagingInterceptor;
import com.wis.common.executation.interceptor.TraceInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private PagingInterceptor pagingInterceptor;

    @Autowired
    private TraceInterceptor traceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(pagingInterceptor);
        registry.addInterceptor(traceInterceptor);
    }
}

