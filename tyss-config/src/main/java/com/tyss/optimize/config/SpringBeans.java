package com.tyss.optimize.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringBeans {

    @Autowired
    private static ApplicationContext applicationContext;


    SpringBeans(@Autowired ApplicationContext ctx)
    {
        SpringBeans.applicationContext = ctx;
    }

    public static ReqHeader getReqHeaderBean()
    {
        ReqHeader reqHeader = applicationContext.getBean(ReqHeader.class);
        return reqHeader;
    }
}
