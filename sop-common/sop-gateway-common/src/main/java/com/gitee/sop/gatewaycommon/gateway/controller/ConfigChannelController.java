package com.gitee.sop.gatewaycommon.gateway.controller;

import com.alibaba.fastjson.JSON;
import com.gitee.sop.gatewaycommon.bean.GatewayPushDTO;
import com.gitee.sop.gatewaycommon.bean.NacosConfigs;
import com.gitee.sop.gatewaycommon.bean.SpringContext;
import com.gitee.sop.gatewaycommon.gateway.ServerWebExchangeUtil;
import com.gitee.sop.gatewaycommon.manager.ChannelMsgProcessor;
import com.gitee.sop.gatewaycommon.manager.EnvGrayManager;
import com.gitee.sop.gatewaycommon.manager.IPBlacklistManager;
import com.gitee.sop.gatewaycommon.manager.IsvRoutePermissionManager;
import com.gitee.sop.gatewaycommon.manager.LimitConfigManager;
import com.gitee.sop.gatewaycommon.manager.RouteConfigManager;
import com.gitee.sop.gatewaycommon.secret.IsvManager;
import com.gitee.sop.gatewaycommon.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tanghc
 */
@Slf4j
@RestController
public class ConfigChannelController {

    private static Map<String, Class<? extends ChannelMsgProcessor>> processorMap = new HashMap<>(16);

    static {
        processorMap.put(NacosConfigs.GROUP_CHANNEL + NacosConfigs.DATA_ID_GRAY, EnvGrayManager.class);
        processorMap.put(NacosConfigs.GROUP_CHANNEL + NacosConfigs.DATA_ID_IP_BLACKLIST, IPBlacklistManager.class);
        processorMap.put(NacosConfigs.GROUP_CHANNEL + NacosConfigs.DATA_ID_ISV, IsvManager.class);
        processorMap.put(NacosConfigs.GROUP_CHANNEL + NacosConfigs.DATA_ID_ROUTE_PERMISSION, IsvRoutePermissionManager.class);
        processorMap.put(NacosConfigs.GROUP_CHANNEL + NacosConfigs.DATA_ID_LIMIT_CONFIG, LimitConfigManager.class);
        processorMap.put(NacosConfigs.GROUP_CHANNEL + NacosConfigs.DATA_ID_ROUTE_CONFIG, RouteConfigManager.class);
    }

    @Value("${sop.secret}")
    private String secret;

    @PostMapping("/sop/configChannelMsg")
    public Mono<String> configChannel(ServerWebExchange exchange) {
        ServerRequest serverRequest = ServerWebExchangeUtil.createReadBodyRequest(exchange);
        // 读取请求体中的内容
        return serverRequest.bodyToMono(String.class)
                .flatMap(requestJson -> {
                    String sign = exchange.getRequest().getHeaders().getFirst("sign");
                    try {
                        // 签名验证
                        RequestUtil.checkResponseBody(requestJson, sign, secret);
                    } catch (Exception e) {
                        log.error("configChannelMsg错误", e);
                        return Mono.just(e.getMessage());
                    }
                    GatewayPushDTO gatewayPushDTO = JSON.parseObject(requestJson, GatewayPushDTO.class);
                    ChannelMsgProcessor channelMsgProcessor = getChannelMsgProcessor(gatewayPushDTO);
                    channelMsgProcessor.process(gatewayPushDTO.getChannelMsg());
                    return Mono.just("ok");
                });
    }

    private ChannelMsgProcessor getChannelMsgProcessor(GatewayPushDTO gatewayPushDTO) {
        String key = gatewayPushDTO.getGroupId() + gatewayPushDTO.getDataId();
        Class<? extends ChannelMsgProcessor> aClass = processorMap.get(key);
        return SpringContext.getBean(aClass);
    }

}
