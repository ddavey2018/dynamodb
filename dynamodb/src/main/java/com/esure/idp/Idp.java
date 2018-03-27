package com.esure.idp;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.esure.AbstractCustomerDynamoEntity;

@DynamoDBTable(tableName = "Idp")
public class Idp extends AbstractCustomerDynamoEntity
{

    public Idp()
    {
        super(null);
    }

    public Idp(String customerId, String entityId)
    {
        super(customerId);
        this.entityId = entityId;
    }

    @DynamoDBRangeKey
    private String entityId;

    public String getEntityId()
    {
        return entityId;
    }

    public void setEntityId(String entityId)
    {
        this.entityId = entityId;
    }

    @Override
    public String toString()
    {
        return "Idp [entityId=" + entityId + ", customerId=" + customerId + "]";
    }

}
