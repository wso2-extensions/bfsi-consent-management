/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.bfsi.consent.management.common.caching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementException;

import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * Abstract cache manager for Consent Management.
 *
 * @param <K>   Extended Cache Key
 * @param <V>   Cache Value
 */
public abstract class ConsentManagementBaseCache<K extends ConsentManagementBaseCacheKey, V> {

    private static final String BASE_CACHE_KEY = "CONSENT_MGT_BASE_CACHE";
    private final String cacheName;

    private static final Log log = LogFactory.getLog(ConsentManagementBaseCache.class);

    /**
     * On Demand Retriever for caching.
     */
    public interface OnDemandRetriever {

        Object get() throws ConsentManagementException;
    }

    /**
     * Initialize With unique cache name.
     *
     * @param cacheName   unique cache name.
     */
    public ConsentManagementBaseCache(String cacheName) {

        this.cacheName = cacheName;

        if (log.isDebugEnabled()) {
            log.debug(String.format("Base Cache initialized for %s", cacheName.replaceAll("[\r\n]", "")));
        }
    }

    /**
     * Get from cache or invoke ondemand retriever and store.
     *
     * @param key                  cache key.
     * @param onDemandRetriever    on demand retriever.
     * @return                   cached object.
     * @throws ConsentManagementException  if an error occurs while retrieving the object
     */
    public V getFromCacheOrRetrieve(K key, OnDemandRetriever onDemandRetriever) throws ConsentManagementException {

        Cache<K, V> cache = getBaseCache();

        if (cache.containsKey(key)) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Found cache entry `%s` in cache %s",
                        key.toString().replaceAll("[\r\n]", ""), cacheName.replaceAll("[\r\n]", "")));
            }
            return getFromCache(key);
        } else {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Cache Entry `%s` not available in cache %s",
                        key.toString().replaceAll("[\r\n]", ""), cacheName.replaceAll("[\r\n]", "")));
            }

            V value = (V) onDemandRetriever.get();

            if (log.isDebugEnabled()) {
                log.debug(String.format("On demand retrieved `%s` for %s",
                        key.toString().replaceAll("[\r\n]", ""), cacheName.replaceAll("[\r\n]", "")));
            }

            removeFromCache(key);
            addToCache(key, value);
            return value;
        }

    }

    /**
     * Get from cache.
     *
     * @param key cache key.
     * @return cached object.
     */
    public V getFromCache(K key) {

        Cache<K, V> cache = getBaseCache();

        if (cache.containsKey(key)) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Found cache entry `%s` in cache %s",
                        key.toString().replaceAll("[\r\n]", ""), cacheName.replaceAll("[\r\n]", "")));
            }
            return cache.get(key);
        } else {

            return null;
        }
    }

    /**
     * Add Object to cache.
     *
     * @param key    cache key.
     * @param value  cache value.
     */
    public void addToCache(K key, V value) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("`%s` added into cache %s", key.toString().replaceAll("[\r\n]", ""),
                    this.cacheName.replaceAll("[\r\n]", "")));
        }

        Cache<K, V> cache = getBaseCache();
        cache.put(key, value);
    }

    /**
     * Remove Object from Cache.
     *
     * @param key  cache key.
     */
    public void removeFromCache(K key) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("`%s` removed from cache %s", key.toString().replaceAll("[\r\n]", ""),
                    cacheName.replaceAll("[\r\n]", "")));
        }

        Cache<K, V> cache = getBaseCache();
        cache.remove(key);
    }

    /**
     * Get Cache for instance.
     *
     * @return
     */
    private Cache<K, V> getBaseCache() {

        CacheManager cacheManager = Caching.getCacheManager(BASE_CACHE_KEY);

        Iterable<Cache<?, ?>> availableCaches = cacheManager.getCaches();
        for (Cache cache : availableCaches) {
            if (cache.getName().equals(cache.getName().startsWith("$__local__$.") ? "$__local__$." +
                    cacheName : cacheName)) {
                return cacheManager.getCache(cacheName);
            }
        }

        CacheConfiguration.Duration accessExpiry = new CacheConfiguration.Duration(TimeUnit.MINUTES,
                getCacheAccessExpiryMinutes());

        CacheConfiguration.Duration modifiedExpiry = new CacheConfiguration.Duration(TimeUnit.MINUTES,
                getCacheModifiedExpiryMinutes());

        // Build Cache on BFSI base cache.
        CacheBuilder<K, V> cacheBuilder = cacheManager.createCacheBuilder(cacheName);

        return cacheBuilder.setExpiry(CacheConfiguration.ExpiryType.ACCESSED, accessExpiry)
                .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, modifiedExpiry)
                .build();
    }

    /**
     * Get Cache expiry time upon access in minutes.
     *
     * @return integer denoting number of minutes.
     */
    public abstract int getCacheAccessExpiryMinutes();

    /**
     * Get Cache expiry time upon modification in minutes.
     *
     * @return integer denoting number of minutes.
     */
    public abstract int getCacheModifiedExpiryMinutes();
}
