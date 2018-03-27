package com.esure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

@Configuration
public class DynamoDbConfig
{
    private AmazonDynamoDB amazonDynamoDb = null;

    private AmazonDynamoDB awsDynamoDb()
    {
        if (amazonDynamoDb == null)
        {
            AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
            builder.setEndpointConfiguration(
                    new EndpointConfiguration("http://localhost:8000", Regions.EU_WEST_1.getName()));
            amazonDynamoDb = builder.build();
        }

        return amazonDynamoDb;
    }

    @Bean
    public DynamoDB dynamoDb()
    {
        return new DynamoDB(awsDynamoDb());
    }

    @Bean
    public DynamoDBMapper dbMapper()
    {
        return new DynamoDBMapper(awsDynamoDb());
    }

}
