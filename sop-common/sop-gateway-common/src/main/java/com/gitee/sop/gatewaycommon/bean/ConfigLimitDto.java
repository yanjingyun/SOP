package com.gitee.sop.gatewaycommon.bean;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tanghc
 */
@Setter
@Getter
public class ConfigLimitDto {

    public static final byte LIMIT_STATUS_OPEN = 1;
    public static final byte LIMIT_STATUS_CLOSE = 0;

    /**  数据库字段：id */
    private Long id;

    /** 路由id, 数据库字段：route_id */
    private String routeId;

    /**  数据库字段：app_key */
    private String appKey;

    /** 限流ip，多个用英文逗号隔开, 数据库字段：limit_ip */
    private String limitIp;

    /** 服务id, 数据库字段：service_id */
    private String serviceId;

    /** 限流策略，1：窗口策略，2：令牌桶策略, 数据库字段：limit_type */
    private Byte limitType;

    /** 每秒可处理请求数, 数据库字段：exec_count_per_second */
    private Integer execCountPerSecond;

    /** 限流过期时间，默认1秒，即每durationSeconds秒允许多少请求（当limit_type=1时有效）, 数据库字段：durationSeconds */
    private Integer durationSeconds;

    /** 返回的错误码, 数据库字段：limit_code */
    private String limitCode;

    /** 返回的错误信息, 数据库字段：limit_msg */
    private String limitMsg;

    /** 令牌桶容量, 数据库字段：token_bucket_count */
    private Integer tokenBucketCount;

    /** 限流开启状态，1:开启，0关闭, 数据库字段：limit_status */
    private Byte limitStatus;

    /** 顺序，值小的优先执行, 数据库字段：order_index */
    private Integer orderIndex;

    /**  数据库字段：gmt_create */
    private Date gmtCreate;

    /**  数据库字段：gmt_modified */
    private Date gmtModified;


    /**
     * 窗口计数器
     */
    private volatile LoadingCache<Long, AtomicLong> counter;

    /**
     * 获取持续时间，1秒内限制请求，则duration设置2
     *
     * @return 返回缓存保存的值。
     */
    public int fetchDuration() {
        Integer durationSeconds = this.durationSeconds;
        if (durationSeconds == null || durationSeconds < 1) {
            durationSeconds = 1;
        }
        // 1秒内限制请求，则duration设置2
        return durationSeconds + 1;
    }

    public LoadingCache<Long, AtomicLong> getCounter() {
        if (counter == null) {
            synchronized (this) {
                if (counter == null) {
                    int duration = fetchDuration();
                    counter = CacheBuilder.newBuilder()
                            .expireAfterWrite(duration, TimeUnit.SECONDS)
                            .build(new CacheLoader<Long, AtomicLong>() {
                                @Override
                                public AtomicLong load(Long seconds) throws Exception {
                                    return new AtomicLong(0);
                                }
                            });
                }
            }
        }
        return counter;
    }

    /**
     * 令牌桶
     */
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private volatile RateLimiter rateLimiter;

    public synchronized void initRateLimiter() {
        rateLimiter = RateLimiter.create(tokenBucketCount);
    }

    /**
     * 获取令牌桶
     * @return
     */
    public RateLimiter fetchRateLimiter() {
        if (rateLimiter == null) {
            synchronized (this) {
                if (rateLimiter == null) {
                    rateLimiter = RateLimiter.create(tokenBucketCount);
                }
            }
        }
        return rateLimiter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigLimitDto that = (ConfigLimitDto) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ConfigLimitDto{" +
                "id=" + id +
                ", routeId='" + routeId + '\'' +
                ", appKey='" + appKey + '\'' +
                ", limitIp='" + limitIp + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", limitType=" + limitType +
                ", execCountPerSecond=" + execCountPerSecond +
                ", durationSeconds=" + durationSeconds +
                ", limitCode='" + limitCode + '\'' +
                ", limitMsg='" + limitMsg + '\'' +
                ", tokenBucketCount=" + tokenBucketCount +
                ", limitStatus=" + limitStatus +
                ", orderIndex=" + orderIndex +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                '}';
    }
}
