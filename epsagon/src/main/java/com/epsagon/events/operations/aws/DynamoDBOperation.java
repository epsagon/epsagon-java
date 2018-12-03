package com.epsagon.events.operations.aws;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.epsagon.Trace;
import com.epsagon.events.MetadataBuilder;
import com.epsagon.protocol.EventOuterClass;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder for an event describing an dynamodb operation.
 */
public class DynamoDBOperation {
    /**
     * Creates a new Builder, with some fields pre-initialized.
     *
     * @param request  The AWS Request object.
     * @param response The AWS Response object, if any. (may be null)
     * @param e        An exception for the request, if any. (may be null)
     * @return A builder with pre-initialized fields.
     */
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static EventOuterClass.Event.Builder newBuilder(
            Request<?> request,
            Response<?> response,
            AmazonHttpClient client,
            Exception e
    ) {

        EventOuterClass.Event.Builder builder = AWSSDKOperation.newBuilder(request, response, client, e);
        if (response != null) {
            MetadataBuilder metadataBuilder = new MetadataBuilder(builder.getResourceBuilder().getMetadataMap());
            AmazonWebServiceRequest awsReq = request.getOriginalRequest();
            switch (builder.getResourceBuilder().getOperation()) {
                case "PutItem":
                    PutItemRequest putItemdReq = (PutItemRequest) awsReq;
                    builder.getResourceBuilder()
                            .setName(putItemdReq.getTableName());
                    try {
                        String item = objectMapper.writeValueAsString(putItemdReq.getItem());
                        metadataBuilder.put("item_hash", getMD5Hash(item));
                        metadataBuilder.putIfAllData("Item", item);
                    } catch (JsonProcessingException err) {
                        Trace.getInstance().addException(err);
                    }
                    break;
                case "UpdateItem":
                    HashMap<String, String> updateParams = new HashMap<>();
                    String updateParamsString = "";
                    UpdateItemRequest updateItemReq = (UpdateItemRequest) awsReq;
                    try {
                        updateParams.put("Key", objectMapper.writeValueAsString(updateItemReq.getKey()));
                        updateParams.put("Expression Attribute Values",
                                objectMapper.writeValueAsString(updateItemReq.getExpressionAttributeValues()));
                        updateParams.put("Update Expression", updateItemReq.getUpdateExpression());
                        updateParamsString = objectMapper.writeValueAsString(updateParams);
                    } catch (JsonProcessingException err) {
                        Trace.getInstance().addException(err);
                    }
                    builder.getResourceBuilder().setName(updateItemReq.getTableName());
                    metadataBuilder.put("Update Parameters", updateParamsString);
                    break;
                case "GetItem":
                    GetItemRequest getItemReq = (GetItemRequest) awsReq;
                    GetItemResult getItemRes = (GetItemResult) response.getAwsResponse();
                    builder.getResourceBuilder()
                            .setName(getItemReq.getTableName());
                    try {
                        metadataBuilder.put("Key", objectMapper.writeValueAsString(getItemReq.getKey()))
                                .putIfAllData("Item", objectMapper.writeValueAsString(getItemRes.getItem()));
                    } catch (JsonProcessingException err) {
                        Trace.getInstance().addException(err);
                    }
                    break;
                case "DeleteItem":
                    DeleteItemRequest delItemReq = (DeleteItemRequest) awsReq;
                    builder.getResourceBuilder()
                            .setName(delItemReq.getTableName());
                    try {
                        metadataBuilder.put("Key", objectMapper.writeValueAsString(delItemReq.getKey()));
                    } catch (JsonProcessingException err) {
                        Trace.getInstance().addException(err);
                    }
                    break;
                case "BatchWriteItem":
                    BatchWriteItemRequest batchWriteReq = (BatchWriteItemRequest) awsReq;
                    Map<String, List<WriteRequest>> requestItems = batchWriteReq.getRequestItems();
                    String tableName = requestItems.keySet().toArray()[0].toString();
                    List<Map> items = new ArrayList<>();
                    builder.getResourceBuilder()
                            .setName(tableName);
                    List<WriteRequest> writeReqs = requestItems.get(tableName);
                    writeReqs.forEach(item -> {
                                if (item.getDeleteRequest() == null) {
                                    items.add(item.getPutRequest().getItem());
                                } else {
                                    items.add(item.getDeleteRequest().getKey());

                                }
                            }

                    );
                    try {
                        metadataBuilder.putIfAllData("Items",
                                objectMapper.writeValueAsString(objectMapper.writeValueAsString(items)));
                    } catch (JsonProcessingException err) {
                        Trace.getInstance().addException(err);
                    }
            }
            builder.getResourceBuilder().putAllMetadata(metadataBuilder.build());
        }
        return builder;
    }

    private static String getMD5Hash(String stringToHash) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(stringToHash.getBytes(StandardCharsets.UTF_8));

            for (byte b : hashInBytes) {
                stringBuilder.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException err) {
        }
        return stringBuilder.toString();
    }
}