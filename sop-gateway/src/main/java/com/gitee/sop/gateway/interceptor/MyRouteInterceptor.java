package com.gitee.sop.gateway.interceptor;

import com.gitee.sop.gatewaycommon.interceptor.RouteInterceptor;
import com.gitee.sop.gatewaycommon.interceptor.RouteInterceptorContext;
import com.gitee.sop.gatewaycommon.param.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

/**
 * 演示拦截器
 *
 * @author tanghc
 */
@Slf4j
@Component
public class MyRouteInterceptor implements RouteInterceptor {

    @Override
    public void preRoute(RouteInterceptorContext context) {
        ApiParam apiParam = context.getApiParam();
        log.info("请求接口:{}, ip:{}", apiParam.fetchNameVersion(), apiParam.fetchIp());
    }

    @Override
    public void afterRoute(RouteInterceptorContext context) {
        ServiceInstance serviceInstance = context.getServiceInstance();
        log.info("请求成功，serviceId:{}({}:{})，微服务返回结果：{}",
                serviceInstance.getServiceId(),
                serviceInstance.getHost(),serviceInstance.getPort(),
                context.getServiceResult());
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
