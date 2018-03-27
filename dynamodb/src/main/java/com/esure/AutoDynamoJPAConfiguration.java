package com.esure;

import java.util.Iterator;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;

@Configuration
@PropertySource("classpath:application.properties")
public class AutoDynamoJPAConfiguration
{
    @Resource
    private Environment env;

    @Autowired
    private DynamoTableGenerator tableGenerator;

    @Autowired
    private DynamoDB dynamoDb;

    public AutoDynamoJPAConfiguration()
    {
    }

    @Bean
    public Object generateTables()
    {
        TableCollection<ListTablesResult> tables = dynamoDb.listTables();
        Iterator<Table> it = tables.iterator();
        Table table;
        while (it.hasNext())
        {
            table = it.next();
            System.out.println(table.getTableName());
        }

        return null;
    }

}
