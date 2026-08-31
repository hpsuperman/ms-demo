package com.example.ms.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 常用操作封装。对象存取走 redisTemplate（JSON 序列化），hash 简单场景走 stringRedisTemplate。
 * 各服务启动类配了 scanBasePackages="com.example.ms"，本类被自动扫描注册。
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

  private final RedisTemplate<String, Object> redisTemplate;
  private final StringRedisTemplate stringRedisTemplate;

  // ---------- String / 对象 ----------

  public void set(String key, Object value) {
    redisTemplate.opsForValue().set(key, value);
  }

  public void set(String key, Object value, long timeout, TimeUnit unit) {
    redisTemplate.opsForValue().set(key, value, timeout, unit);
  }

  public Object get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  public <T> T get(String key, Class<T> clazz) {
    Object value = get(key);
    return value == null ? null : clazz.cast(value);
  }

  public boolean delete(String key) {
    return Boolean.TRUE.equals(redisTemplate.delete(key));
  }

  public long delete(Collection<String> keys) {
    Long n = redisTemplate.delete(keys);
    return n == null ? 0 : n;
  }

  public boolean hasKey(String key) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  public boolean expire(String key, long timeout, TimeUnit unit) {
    return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
  }

  public long getExpire(String key) {
    Long ttl = redisTemplate.getExpire(key);
    return ttl == null ? -1 : ttl;
  }

  public long increment(String key) {
    Long n = redisTemplate.opsForValue().increment(key);
    return n == null ? 0 : n;
  }

  public long increment(String key, long delta) {
    Long n = redisTemplate.opsForValue().increment(key, delta);
    return n == null ? 0 : n;
  }

  public boolean tryLock(String key, long timeout, TimeUnit unit) {
    return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, 1, timeout, unit));
  }

  public void unlock(String key) {
    redisTemplate.delete(key);
  }

  // ---------- Hash ----------

  public void hSet(String key, String field, String value) {
    stringRedisTemplate.opsForHash().put(key, field, value);
  }

  public String hGet(String key, String field) {
    Object value = stringRedisTemplate.opsForHash().get(key, field);
    return value == null ? null : value.toString();
  }

  public long hDel(String key, Object... fields) {
    Long n = stringRedisTemplate.opsForHash().delete(key, fields);
    return n == null ? 0 : n;
  }

  public boolean hHasKey(String key, String field) {
    return Boolean.TRUE.equals(stringRedisTemplate.opsForHash().hasKey(key, field));
  }

  public Map<Object, Object> hGetAll(String key) {
    return stringRedisTemplate.opsForHash().entries(key);
  }
}
