package com.example.userservice.Config;

import com.hazelcast.config.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

        @Bean
        public Config userCacheConfig() {
            return new Config()
                    .setInstanceName("hazel-instance")
                    .addMapConfig(new MapConfig()
                            .setName("user-cache")
                            .setTimeToLiveSeconds(1800000) //30 minutes
                            .setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU).setMaxSizePolicy(MaxSizePolicy.FREE_HEAP_SIZE))
                    );
        }
}
