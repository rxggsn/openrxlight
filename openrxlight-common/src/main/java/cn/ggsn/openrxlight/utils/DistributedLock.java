package cn.ggsn.openrxlight.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import cn.ggsn.openrxlight.lang.Lists2;
import io.quarkus.runtime.Startup;
import io.vertx.redis.client.RedisAPI;
import jakarta.inject.Singleton;

@Singleton
@RequiredArgsConstructor
@Slf4j
@Startup
public class DistributedLock {
	private final RedisAPI redisAPI;

	/**
	 * Acquire a distributed lock using Redis SETNX pattern.
	 * Uses Lua script to ensure atomic SETNX + EXPIRE operation.
	 *
	 * @param key      the lock key
	 * @param uuid     unique identifier for the lock owner
	 * @param msToLock lock expiration time in milliseconds
	 * @return true if lock acquired successfully, false otherwise
	 */
	public Boolean lock(String key, String uuid, Integer msToLock) {
		String lockScript = "if redis.call('SETNX', KEYS[1], ARGV[1]) == 1 then\n" +
				"    redis.call('PEXPIRE', KEYS[1], ARGV[2])\n" +
				"    return 1\n" +
				"else\n" +
				"    return 0\n" +
				"end";

		return this.redisAPI.eval(Lists2.of(lockScript, "1", key, uuid, String.valueOf(msToLock)))
				.map(response -> {
					return response.toInteger() == 1;
				}).onFailure(ex -> {
					log.error("Failed to acquire lock", ex);
					throw new RuntimeException(ex);
				})
				.result();
	}

	/**
	 * Release a distributed lock.
	 * Only releases the lock if the current owner matches the provided uuid.
	 * Uses Lua script to ensure atomic GET + DELETE operation.
	 *
	 * @param key  the lock key
	 * @param uuid unique identifier for the lock owner
	 */
	public void unlock(String key, String uuid) {
		String unlockScript = "if redis.call('GET', KEYS[1]) == ARGV[1] then\n" +
				"    return redis.call('DEL', KEYS[1])\n" +
				"else\n" +
				"    return 0\n" +
				"end";

		this.redisAPI.eval(Lists2.of(unlockScript, "1", key, uuid))
				.onFailure(ex -> {
					log.error("Failed to release lock", ex);
				}).result();
	}
}
