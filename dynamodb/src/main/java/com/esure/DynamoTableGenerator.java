package com.esure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;

@Component
public class DynamoTableGenerator
{
    @Autowired
    private DynamoDB dynamoDb;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void generateTables(String... basePackages)
    {
        Set<Class<?>> dynamoDbTableClasses;
        for (String strBasePackage : basePackages)
        {
            dynamoDbTableClasses = getDynamoTables(strBasePackage);
            createDbTables(dynamoDbTableClasses);
        }
    }

    private Set<Class<?>> getDynamoTables(String strBasePackage)
    {
        Reflections reflections = new Reflections(strBasePackage);
        return reflections.getTypesAnnotatedWith(DynamoDBTable.class);
    }

    private void createDbTables(Set<Class<?>> dynamoDbTableClasses)
    {
        for (Class<?> cls : dynamoDbTableClasses)
        {
            if (cls != AbstractCustomerDynamoEntity.class && !tableExists(cls))
            {
                createTable(cls);
            }
        }
    }

    private Field[] getFields(Class<?> cls)
    {
        Field[] fields = cls.getDeclaredFields();
        if (cls.getSuperclass() != Object.class)
        {
            Field[] superClassFields = getFields(cls.getSuperclass());
            int combinedLength = superClassFields.length + fields.length;
            Field[] returnFields = new Field[combinedLength];
            for (int i = 0; i < superClassFields.length; i++)
            {
                returnFields[i] = superClassFields[i];
            }
            for (int i = 0; i < fields.length; i++)
            {
                returnFields[superClassFields.length + i] = fields[i];
            }
            return returnFields;
        }
        else
        {
            return fields;
        }
    }

    private Table createTable(Class<?> cls)
    {
        String tableName = getTableName(cls);

        List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
        List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
        Field[] fields = getFields(cls);
        for (Field field : fields)
        {
            if (!field.isAnnotationPresent(DynamoDBIgnore.class))
            {
                addFieldDefinition(field, keySchema, attributeDefinitions);
            }
            else
            {
                continue;
            }
        }

        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                .withKeySchema(keySchema).withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput(5L, 6L));
        Table table = dynamoDb.createTable(createTableRequest);
        try
        {
            table.waitForActive();
            logger.info(String.format("Table [%s] is now active", tableName));
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return table;
    }

    private void addFieldDefinition(Field field, List<KeySchemaElement> keySchema,
            List<AttributeDefinition> attributeDefinitions)
    {
        String attrName = getAttributeName(field);
        DynamoDBHashKey hashKey = field.getAnnotation(DynamoDBHashKey.class);
        if (hashKey != null)
        {
            keySchema.add(new KeySchemaElement().withAttributeName(attrName).withKeyType(KeyType.HASH));
            attributeDefinitions.add(new AttributeDefinition().withAttributeName(attrName)
                    .withAttributeType(field.getType().getSimpleName().substring(0, 1)));
        }
        DynamoDBRangeKey rangeKey = field.getAnnotation(DynamoDBRangeKey.class);
        if (rangeKey != null)
        {
            keySchema.add(new KeySchemaElement().withAttributeName(attrName).withKeyType(KeyType.RANGE));
            attributeDefinitions.add(new AttributeDefinition().withAttributeName(attrName)
                    .withAttributeType(field.getType().getSimpleName().substring(0, 1)));
        }
    }

    private String getAttributeName(Field field)
    {
        Annotation[] annotations = field.getAnnotations();
        String attributeName = null;
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType() == DynamoDBHashKey.class)
            {
                attributeName = ((DynamoDBHashKey) annotation).attributeName();
                break;
            }
            else if (annotation.annotationType() == DynamoDBAttribute.class)
            {
                attributeName = ((DynamoDBAttribute) annotation).attributeName();
                break;
            }
            else
            {
                continue;
            }
        }
        if (attributeName == null)
        {
            attributeName = field.getName();
        }
        return attributeName;
    }

    private boolean tableExists(Class<?> cls)
    {
        String tableName = getTableName(cls);

        Table table = dynamoDb.getTable(tableName);
        try
        {
            TableDescription desc = table.describe();
            return desc.getTableStatus().equals(TableStatus.ACTIVE.name());
        }
        catch (ResourceNotFoundException e)
        {
            return false;
        }
    }

    private String getTableName(Class<?> cls)
    {
        DynamoDBTable tableAnnotation = cls.getAnnotation(DynamoDBTable.class);
        String tableName = tableAnnotation.tableName();
        return tableName != null ? tableName : cls.getSimpleName();
    }
}
