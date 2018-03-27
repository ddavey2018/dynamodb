package com.esure;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "AbstractCustomerDynamoEntity")
public abstract class AbstractCustomerDynamoEntity
{
    @DynamoDBHashKey(attributeName = "customerId")
    protected String customerId;

    protected AbstractCustomerDynamoEntity(String customerId)
    {
        this.customerId = customerId;
    }

    public String getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(String customerId)
    {
        this.customerId = customerId;
    }

}
