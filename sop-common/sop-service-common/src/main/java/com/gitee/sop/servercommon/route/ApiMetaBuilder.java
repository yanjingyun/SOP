package com.gitee.sop.servercommon.route;

import com.gitee.sop.servercommon.annotation.Open;
import com.gitee.sop.servercommon.bean.ServiceApiInfo;
import com.gitee.sop.servercommon.bean.ServiceConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tanghc
 */
public class ApiMetaBuilder {

    /**
     * 接口名规则：允许字母、数字、点、下划线
     */
    private static final String REGEX_API_NAME = "^[a-zA-Z0-9\\.\\_\\-]+$";

    public ServiceApiInfo getServiceApiInfo(String serviceId, RequestMappingHandlerMapping requestMappingHandlerMapping) {
        if (serviceId == null) {
            throw new IllegalArgumentException("请在application.properties中指定spring.application.name属性");
        }
        List<ServiceApiInfo.ApiMeta> apis = this.buildApiMetaList(requestMappingHandlerMapping);
        // 排序
        apis.sort(Comparator.comparing(ServiceApiInfo.ApiMeta::fetchNameVersion));

        ServiceApiInfo serviceApiInfo = new ServiceApiInfo();
        serviceApiInfo.setServiceId(serviceId);
        serviceApiInfo.setApis(apis);

        return serviceApiInfo;
    }

    protected List<ServiceApiInfo.ApiMeta> buildApiMetaList(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        Set<RequestMappingInfo> requestMappingInfos = handlerMethods.keySet();
        List<String> store = new ArrayList<>();
        List<ServiceApiInfo.ApiMeta> apis = new ArrayList<>(requestMappingInfos.size());

        for (Map.Entry<RequestMappingInfo, HandlerMethod> handlerMethodEntry : handlerMethods.entrySet()) {
            ServiceApiInfo.ApiMeta apiMeta = this.buildApiMeta(handlerMethodEntry);
            if (apiMeta == null) {
                continue;
            }
            String key = apiMeta.fetchNameVersion();
            if (store.contains(key)) {
                throw new IllegalArgumentException("重复申明接口，请检查path和version，path:" + apiMeta.getPath() + ", version:" + apiMeta.getVersion());
            } else {
                store.add(key);
            }
            apis.add(apiMeta);
        }
        return apis;
    }

    protected ServiceApiInfo.ApiMeta buildApiMeta(Map.Entry<RequestMappingInfo, HandlerMethod> handlerMethodEntry) {
        RequestMappingInfo requestMappingInfo = handlerMethodEntry.getKey();
        Set<String> patterns = requestMappingInfo.getPatternsCondition().getPatterns();
        Open open = handlerMethodEntry.getValue().getMethodAnnotation(Open.class);
        ServiceApiInfo.ApiMeta apiMeta = null;
        if (open != null) {
            String name = open.value();
            String version = open.version();
            if ("".equals(version)) {
                version = ServiceConfig.getInstance().getDefaultVersion();
            }
            // 方法完整的path，如: /goods/listGoods,/users/user/get
            String path = patterns.iterator().next();
            this.checkApiName(name);
            apiMeta = new ServiceApiInfo.ApiMeta(name, path, version);
            apiMeta.setIgnoreValidate(BooleanUtils.toInteger(open.ignoreValidate()));
            apiMeta.setMergeResult(BooleanUtils.toInteger(open.mergeResult()));
            apiMeta.setPermission(BooleanUtils.toInteger(open.permission()));
            apiMeta.setNeedToken(BooleanUtils.toInteger(open.needToken()));
        }
        return apiMeta;
    }


    protected void checkApiName(String name) {
        if (!name.matches(REGEX_API_NAME)) {
            throw new IllegalArgumentException("接口名称只允许【字母、数字、点(.)、下划线(_)、减号(-)】，错误接口：" + name);
        }
    }
}
