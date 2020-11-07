package com.gitee.sop.bridge.route;

import com.gitee.sop.gatewaycommon.bean.InstanceDefinition;
import com.gitee.sop.gatewaycommon.bean.SopConstants;
import com.gitee.sop.gatewaycommon.route.BaseRegistryListener;
import com.gitee.sop.gatewaycommon.route.RegistryEvent;
import com.gitee.sop.gatewaycommon.route.ServiceHolder;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.context.ApplicationEvent;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 加载服务路由，eureka实现
 *
 * @author tanghc
 */
public class EurekaRegistryListener extends BaseRegistryListener {

    private Set<ServiceHolder> cacheServices = new HashSet<>();

    @Autowired(required = false)
    private List<RegistryEvent> registryEventList;

    @Override
    public void onEvent(ApplicationEvent applicationEvent) {
        Object source = applicationEvent.getSource();
        CloudEurekaClient cloudEurekaClient = (CloudEurekaClient) source;
        Applications applications = cloudEurekaClient.getApplications();
        List<Application> registeredApplications = applications.getRegisteredApplications();

        List<ServiceHolder> serviceList = registeredApplications
                .stream()
                .filter(application -> CollectionUtils.isNotEmpty(application.getInstances()))
                .map(Application::getInstances)
                .map(instanceInfos -> {
                    // 找到最新的一个
                    Optional<InstanceInfo> instanceInfoOptional = instanceInfos
                            .stream()
                            .max(Comparator.comparing(InstanceInfo::getLastUpdatedTimestamp));
                    return instanceInfoOptional.map(instanceInfo -> {
                        String startupTime = instanceInfo.getMetadata().get(SopConstants.METADATA_KEY_TIME_STARTUP);
                        if (startupTime == null) {
                            return null;
                        }
                        return new ServiceHolder(instanceInfo.getAppName(), Long.parseLong(startupTime));
                    }).orElse(null);
                })
                .filter(Objects::nonNull)
                .filter(this::canOperator)
                .collect(Collectors.toList());

        final Set<ServiceHolder> currentServices = new HashSet<>(serviceList);
        // 删除现有的，剩下的就是新服务
        currentServices.removeAll(cacheServices);
        // 如果有新的服务注册进来
        if (currentServices.size() > 0) {
            List<Application> newApplications = registeredApplications.stream()
                    .filter(application ->
                            containsService(currentServices, application.getName()))
                    .collect(Collectors.toList());

            this.doRegister(newApplications);
        }

        Set<String> removedServiceIdList = getRemovedServiceId(serviceList);
        // 如果有服务下线
        this.doRemove(removedServiceIdList);

        cacheServices = new HashSet<>(serviceList);
    }

    /**
     * 获取已经下线的serviceId
     *
     * @param serviceList 最新的serviceId集合
     * @return 返回已下线的serviceId
     */
    private Set<String> getRemovedServiceId(List<ServiceHolder> serviceList) {
        Set<String> cache = cacheServices.stream()
                .map(ServiceHolder::getServiceId)
                .collect(Collectors.toSet());

        Set<String> newList = serviceList.stream()
                .map(ServiceHolder::getServiceId)
                .collect(Collectors.toSet());

        cache.removeAll(newList);
        return cache;
    }

    private static boolean containsService(Set<ServiceHolder> currentServices, String serviceId) {
        for (ServiceHolder currentService : currentServices) {
            if (currentService.getServiceId().equalsIgnoreCase(serviceId)) {
                return true;
            }
        }
        return false;
    }

    private void doRegister(List<Application> registeredApplications) {
        registeredApplications.forEach(application -> {
            List<InstanceInfo> instances = application.getInstances();
            if (CollectionUtils.isNotEmpty(instances)) {
                instances.sort(Comparator.comparing(InstanceInfo::getLastUpdatedTimestamp).reversed());
                InstanceInfo instanceInfo = instances.get(0);
                InstanceDefinition instanceDefinition = new InstanceDefinition();
                instanceDefinition.setInstanceId(instanceInfo.getInstanceId());
                instanceDefinition.setServiceId(instanceInfo.getAppName());
                instanceDefinition.setIp(instanceInfo.getIPAddr());
                instanceDefinition.setPort(instanceInfo.getPort());
                instanceDefinition.setMetadata(instanceInfo.getMetadata());
                pullRoutes(instanceDefinition);
                if (registryEventList != null) {
                    registryEventList.forEach(registryEvent -> registryEvent.onRegistry(instanceDefinition));
                }
            }
        });
    }

    private void doRemove(Set<String> deletedServices) {
        if (deletedServices == null) {
            return;
        }
        deletedServices.forEach(serviceId -> {
            this.removeRoutes(serviceId);
            if (registryEventList != null) {
                registryEventList.forEach(registryEvent -> registryEvent.onRemove(serviceId));
            }
        });
    }

}
