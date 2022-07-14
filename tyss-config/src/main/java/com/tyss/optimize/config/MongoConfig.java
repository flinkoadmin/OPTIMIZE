package com.tyss.optimize.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Autowired
    private Environment env;

    @Bean(name = "mongoClient")
    public MongoClient mongoClient() {
        return MongoClients.create(env.getProperty("MONGO_CONNECTION_URL"));
    }
    @Bean(name = "mongoAuthTemplate")
    public  MongoTemplate mongoAuthTemplate() {
        return new MongoTemplate(mongoClient(), env.getProperty("MONGO_AUTH_DB"));
    }
}