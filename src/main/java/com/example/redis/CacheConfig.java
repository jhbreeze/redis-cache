package com.example.redis;

import static org.springframework.data.redis.serializer.RedisSerializationContext.*;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableCaching // 캐시를 관리하는 CacheManger의 구현체가 Bean으로 등록되어야 함.
public class CacheConfig {

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
		RedisCacheConfiguration configuration = RedisCacheConfiguration
			.defaultCacheConfig()
			.disableCachingNullValues()
			.entryTtl(Duration.ofSeconds(10)) // 캐싱 전략 : 10초만 유지(lazyLoading)
			.computePrefixWith(CacheKeyPrefix.simple())
			.serializeValuesWith(SerializationPair.fromSerializer(RedisSerializer.java()));

		return RedisCacheManager
			.builder(redisConnectionFactory)
			.cacheDefaults(configuration)
			.build();

	}
}
