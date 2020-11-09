package com.gitee.sop.gateway.manager;

import com.alibaba.fastjson.JSON;
import com.gitee.fastmybatis.core.query.Query;
import com.gitee.sop.gateway.entity.ConfigServiceRoute;
import com.gitee.sop.gateway.mapper.ConfigServiceRouteMapper;
import com.gitee.sop.gatewaycommon.bean.BeanInitializer;
import com.gitee.sop.gatewaycommon.bean.InstanceDefinition;
import com.gitee.sop.gatewaycommon.bean.ServiceBeanInitializer;
import com.gitee.sop.gatewaycommon.bean.ServiceRouteInfo;
import com.gitee.sop.gatewaycommon.route.RoutesProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tanghc
 */
@Slf4j
@Component
public class DbRoutesProcessor implements RoutesProcessor {

    @Autowired
    private ConfigServiceRouteMapper configServiceRouteMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void removeAllRoutes(String serviceId) {
        // 删除serviceId下所有的路由
        Query delServiceQuery = new Query().eq("service_id", serviceId);
        configServiceRouteMapper.deleteByQuery(delServiceQuery);
    }

    @Override
    public synchronized void saveRoutes(ServiceRouteInfo serviceRouteInfo, InstanceDefinition instance) {
        log.info("保存路由信息到数据库，instance: {}", instance);
        String serviceId = serviceRouteInfo.getServiceId();
        List<ConfigServiceRoute> configServiceRoutes = serviceRouteInfo
                .getRouteDefinitionList()
                .parallelStream()
                .map(routeDefinition -> {
                    ConfigServiceRoute configServiceRoute = new ConfigServiceRoute();
                    configServiceRoute.setId(routeDefinition.getId());
                    configServiceRoute.setName(routeDefinition.getName());
                    configServiceRoute.setNameCn(routeDefinition.getNameCn());
                    configServiceRoute.setVersion(routeDefinition.getVersion());
                    configServiceRoute.setUri(routeDefinition.getUri());
                    configServiceRoute.setPath(routeDefinition.getPath());
                    configServiceRoute.setFilters(JSON.toJSONString(routeDefinition.getFilters()));
                    configServiceRoute.setPredicates(JSON.toJSONString(routeDefinition.getPredicates()));
                    configServiceRoute.setIgnoreValidate((byte) routeDefinition.getIgnoreValidate());
                    configServiceRoute.setMergeResult((byte) routeDefinition.getMergeResult());
                    configServiceRoute.setStatus((byte) routeDefinition.getStatus());
                    configServiceRoute.setPermission((byte) routeDefinition.getPermission());
                    configServiceRoute.setOrderIndex(routeDefinition.getOrder());
                    configServiceRoute.setNeedToken((byte)routeDefinition.getNeedToken());
                    configServiceRoute.setServiceId(serviceId);
                    return configServiceRoute;
                })
                .collect(Collectors.toList());

        // 删除serviceId下所有的路由
        this.removeAllRoutes(serviceId);

        if (CollectionUtils.isNotEmpty(configServiceRoutes)) {
            // 批量保存
            configServiceRouteMapper.saveBatch(configServiceRoutes);
            // 后续处理操作
            this.initServiceBeanInitializer(serviceId);
        }
    }

    private void initServiceBeanInitializer(String serviceId) {
        Map<String, ServiceBeanInitializer> serviceBeanInitializerMap = applicationContext.getBeansOfType(ServiceBeanInitializer.class);
        serviceBeanInitializerMap.values().forEach(serviceBeanInitializer -> serviceBeanInitializer.load(serviceId));
    }
}
