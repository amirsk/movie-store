package com.store.movie.service;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.protocol.RedisCommand;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DirContextDnsResolver;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Service
@Log4j2
public class RedisService {

    public void me() {
        StopWatch stopWatch = new StopWatch("ElastiCache");

        String endpoint = "movie-8whocy.serverless.use1.cache.amazonaws.com";
        int port = 6379;
        String username = "testuser";
        String password = "strongpass12345676";

//        DefaultClientResources clientResources = DefaultClientResources.builder() //
//                .dnsResolver(new DirContextDnsResolver()) // Does not cache DNS lookups
//                .build();
//
//        RedisClient redisClient = RedisClient.create(clientResources, "redis://"+username+":"+password+"@"+endpoint+":6379");
//        StatefulRedisConnection<String, String> connection = redisClient.connect();
//
//        System.out.println("Connected to Redis");

        RedisURI redisURI = RedisURI.builder()
                .withHost(endpoint)
                .withPort(port)
                .withAuthentication(username, password)
                .withSsl(true)
                .withVerifyPeer(false)
                .build();
        log.info("Created RedisURI");

        RedisClient redisClient = RedisClient.create(redisURI);
        log.info("Created RedisClient");

        StatefulRedisConnection<String, String> connection = redisClient.connect();
        log.info("Created StatefulRedisConnection");

        RedisCommands<String, String> syncCommands = connection.sync();

        String key = "myKey";
        String value = "myValue";

        stopWatch.start("Adding");
        syncCommands.set(key, value);
        stopWatch.stop();

        stopWatch.start("Retrieving");
        String retrievedValue = syncCommands.get(key);
        stopWatch.stop();

        log.info("Retrieved {}. StopWatch: {}", retrievedValue, stopWatch);

        connection.close();
        redisClient.shutdown();
    }

}
