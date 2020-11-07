package com.gitee.sop.gatewaycommon.gateway.codec;

import com.gitee.sop.gatewaycommon.manager.EnvironmentKeys;
import org.springframework.core.codec.AbstractDataBufferDecoder;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.function.server.HandlerStrategies;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author tanghc
 */
public class MessageReaderFactory {

    public static final String METHOD_SET_MAX_IN_MEMORY_SIZE = "setMaxInMemorySize";
    public static final String METHOD_GET_DECODER = "getDecoder";
    public static final int DEFAULT_SIZE = 256 * 1024;

    public static List<HttpMessageReader<?>> build() {
        String maxInMemorySizeValueStr = EnvironmentKeys.MAX_IN_MEMORY_SIZE.getValue();
        int maxInMemorySizeValue = Integer.parseInt(maxInMemorySizeValueStr);
        List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();
        if (DEFAULT_SIZE == maxInMemorySizeValue) {
            return messageReaders;
        }
        // 设置POST缓存大小
        for (HttpMessageReader<?> httpMessageReader : messageReaders) {
            Method[] methods = ReflectionUtils.getDeclaredMethods(httpMessageReader.getClass());
            for (Method method : methods) {
                String methodName = method.getName();
                if (METHOD_SET_MAX_IN_MEMORY_SIZE.equals(methodName)) {
                    ReflectionUtils.invokeMethod(method, httpMessageReader, maxInMemorySizeValue);
                } else if (METHOD_GET_DECODER.equals(methodName)) {
                    Object decoder = ReflectionUtils.invokeMethod(method, httpMessageReader);
                    if (decoder instanceof AbstractDataBufferDecoder) {
                        AbstractDataBufferDecoder<?> bufferDecoder = (AbstractDataBufferDecoder<?>) decoder;
                        bufferDecoder.setMaxInMemorySize(maxInMemorySizeValue);
                    }
                }
            }
        }
        return messageReaders;
    }
}
