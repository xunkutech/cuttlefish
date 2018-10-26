package com.xunkutech.base;

import com.xunkutech.base.app.BaseAppConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import redis.embedded.RedisServer;

/**
 * Override the Executor at the Application Level
 * <p>
 * Created by jason on 9/12/15.
 */
@SpringBootApplication(scanBasePackageClasses = {BaseAppConfiguration.class, Application4Test.class})
@EnableScheduling
@EnableAsync
public class Application4Test {

    @Bean
    public JedisConnectionFactory connectionFactory(final RedisServer redisServer) {
        final JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName("localhost");
        jedisConnectionFactory.setPort(redisServer.ports().get(0));
        jedisConnectionFactory.setDatabase(0);
        return jedisConnectionFactory;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RedisServer embeddedRedisServer() {
        return RedisServer.builder().port(16379).build();
    }

    /**
     * To prevent the customer converter register twice issue when using @AutoConfigureMockMvc,
     * we have to init MockMvc manually here.
     * <p>
     * See [https://stackoverflow.com/questions/40742221/hibernate-5-0-11-attributeconverter-class-registered-mulitple-times]
     *
     * @param webApplicationContext
     * @return
     */
    @Bean
    public MockMvc createMockMvc(WebApplicationContext webApplicationContext) {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application4Test.class, args);
    }

}

