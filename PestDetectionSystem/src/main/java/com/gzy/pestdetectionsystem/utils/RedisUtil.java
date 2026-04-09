package com.gzy.pestdetectionsystem.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public final class RedisUtil {

    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);
    private static final String NULL_VALUE = "__REDIS_NULL__";

    private final ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider;
    private final ObjectMapper objectMapper;

    private RedisTemplate<String, Object> redisTemplate() {
        return redisTemplateProvider.getIfAvailable();
    }

    public boolean isAvailable() {
        return redisTemplate() != null;
    }

    private boolean isConnectionFailure(Exception e) {
        return e instanceof RedisConnectionFailureException || e instanceof DataAccessResourceFailureException;
    }

    private void logFailure(String action, String key, Exception e) {
        if (isConnectionFailure(e)) {
            log.warn("redis服务不在线");
            return;
        }
        log.warn("Redis operation failed during action={}, key={}, fallback enabled: {}",
                action, key, e.getMessage());
    }

    private boolean execute(String action, String key, Consumer<RedisTemplate<String, Object>> consumer) {
        RedisTemplate<String, Object> redisTemplate = redisTemplate();
        if (redisTemplate == null) {
            log.debug("Redis is not configured, skip action={}, key={}", action, key);
            return false;
        }
        try {
            consumer.accept(redisTemplate);
            return true;
        } catch (Exception e) {
            logFailure(action, key, e);
            return false;
        }
    }

    private <T> T execute(String action, String key, Supplier<T> supplier, T fallbackValue) {
        RedisTemplate<String, Object> redisTemplate = redisTemplate();
        if (redisTemplate == null) {
            log.debug("Redis is not configured, skip action={}, key={}", action, key);
            return fallbackValue;
        }
        try {
            return supplier.get();
        } catch (Exception e) {
            logFailure(action, key, e);
            return fallbackValue;
        }
    }

    private boolean isNullPlaceholder(Object value) {
        return NULL_VALUE.equals(value);
    }

    private long resolveExpireSeconds(long baseSeconds, long randomExtraSeconds) {
        if (baseSeconds <= 0) {
            return 0;
        }
        if (randomExtraSeconds <= 0) {
            return baseSeconds;
        }
        return baseSeconds + ThreadLocalRandom.current().nextLong(randomExtraSeconds + 1);
    }

    public boolean expire(String key, long time) {
        if (time <= 0) {
            return false;
        }
        return execute("expire", key, redisTemplate -> redisTemplate.expire(key, time, TimeUnit.SECONDS));
    }

    public long getExpire(String key) {
        return execute("getExpire", key, () -> {
            Long expire = redisTemplate().getExpire(key, TimeUnit.SECONDS);
            return expire == null ? 0L : expire;
        }, 0L);
    }

    public boolean hasKey(String key) {
        return execute("hasKey", key, () -> Boolean.TRUE.equals(redisTemplate().hasKey(key)), false);
    }

    public void del(String... key) {
        if (key == null || key.length == 0) {
            return;
        }
        if (key.length == 1) {
            execute("delete", key[0], redisTemplate -> redisTemplate.delete(key[0]));
            return;
        }
        execute("batchDelete", Arrays.toString(key), redisTemplate -> redisTemplate.delete(Arrays.asList(key)));
    }

    public Object get(String key) {
        if (key == null) {
            return null;
        }
        Object value = execute("get", key, () -> redisTemplate().opsForValue().get(key), null);
        return isNullPlaceholder(value) ? null : value;
    }

    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        try {
            return objectMapper.convertValue(value, clazz);
        } catch (IllegalArgumentException e) {
            log.warn("Redis value convert failed, key={}, targetType={}", key, clazz.getName(), e);
            return null;
        }
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(value, typeReference);
        } catch (IllegalArgumentException e) {
            log.warn("Redis value convert failed, key={}, targetType={}", key, typeReference.getType(), e);
            return null;
        }
    }

    public boolean set(String key, Object value) {
        return execute("set", key, redisTemplate -> redisTemplate.opsForValue().set(key, value));
    }

    public boolean set(String key, Object value, long time) {
        if (time > 0) {
            return execute("setWithExpire", key,
                    redisTemplate -> redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS));
        }
        return set(key, value);
    }

    public boolean setWithRandomTtl(String key, Object value, long time, long randomExtraSeconds) {
        long expireSeconds = resolveExpireSeconds(time, randomExtraSeconds);
        if (expireSeconds <= 0) {
            return set(key, value);
        }
        return execute("setWithRandomTtl", key,
                redisTemplate -> redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS));
    }

    public boolean setNullValue(String key, long time) {
        if (time <= 0) {
            return false;
        }
        return execute("setNullValue", key,
                redisTemplate -> redisTemplate.opsForValue().set(key, NULL_VALUE, time, TimeUnit.SECONDS));
    }

    public boolean setNullValueWithRandomTtl(String key, long time, long randomExtraSeconds) {
        long expireSeconds = resolveExpireSeconds(time, randomExtraSeconds);
        if (expireSeconds <= 0) {
            return false;
        }
        return execute("setNullValueWithRandomTtl", key,
                redisTemplate -> redisTemplate.opsForValue().set(key, NULL_VALUE, expireSeconds, TimeUnit.SECONDS));
    }

    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于 0");
        }
        return execute("increment", key, () -> {
            Long result = redisTemplate().opsForValue().increment(key, delta);
            return result == null ? 0L : result;
        }, 0L);
    }

    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于 0");
        }
        return execute("decrement", key, () -> {
            Long result = redisTemplate().opsForValue().increment(key, -delta);
            return result == null ? 0L : result;
        }, 0L);
    }

    public Object hget(String key, String item) {
        return execute("hashGet", key, () -> redisTemplate().opsForHash().get(key, item), null);
    }

    public Map<Object, Object> hmget(String key) {
        return execute("hashEntries", key, () -> redisTemplate().opsForHash().entries(key), Collections.emptyMap());
    }

    public boolean hmset(String key, Map<String, Object> map) {
        return execute("hashPutAll", key, redisTemplate -> redisTemplate.opsForHash().putAll(key, map));
    }

    public boolean hset(String key, String item, Object value) {
        return execute("hashPut", key, redisTemplate -> redisTemplate.opsForHash().put(key, item, value));
    }

    public double hincr(String key, String item, double by) {
        return execute("hashIncrement", key, () -> {
            Double result = redisTemplate().opsForHash().increment(key, item, by);
            return result == null ? 0D : result;
        }, 0D);
    }

    public Set<Object> sGet(String key) {
        return execute("setMembers", key, () -> redisTemplate().opsForSet().members(key), Collections.emptySet());
    }

    public long sSet(String key, Object... values) {
        return execute("setAdd", key, () -> {
            Long result = redisTemplate().opsForSet().add(key, values);
            return result == null ? 0L : result;
        }, 0L);
    }

    public List<Object> lGet(String key, long start, long end) {
        return execute("listRange", key, () -> redisTemplate().opsForList().range(key, start, end), Collections.emptyList());
    }

    public boolean lSet(String key, Object value) {
        return execute("listPush", key, redisTemplate -> redisTemplate.opsForList().rightPush(key, value));
    }
}
