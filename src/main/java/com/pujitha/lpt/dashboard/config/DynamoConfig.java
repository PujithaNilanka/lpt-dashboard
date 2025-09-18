package com.pujitha.lpt.dashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.accessKeyId:}")
    private String accessKey;

    @Value("${aws.secretAccessKey:}")
    private String secretKey;

    @Value("${aws.dynamo.endpoint:}")
    private String dynamoEndpoint;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }
}
