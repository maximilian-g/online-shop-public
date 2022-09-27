package com.online.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaginationConfig {

    @Value("${spring.data.web.pageable.default-page-size}")
    private Integer pageSizeProp;

    public Integer getPageSizeProp() {
        return pageSizeProp;
    }
}
