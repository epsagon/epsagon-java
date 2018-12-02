package com.epsagon.events.operations.aws;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.services.s3.model.*;
import com.epsagon.Trace;
import com.epsagon.events.MetadataBuilder;
import com.epsagon.protocol.EventOuterClass;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;

abstract class S3ObjectSummerySerializationMixIn {
    @JsonIgnore
    String bucketName;
    @JsonIgnore
    String storageClass;
    @JsonIgnore
    Owner owner;
    @JsonIgnore
    Date lastModified;

}

/**
 * A builder for an event describing an S3 operation
 */
public class S3Operation {
    /**
     * Creates a new Builder, with some fields pre-initialized.
     *
     * @param request  The AWS Request object.
     * @param response The AWS Response object, if any. (may be null)
     * @param e        An exception for the request, if any. (may be null)
     * @return A builder with pre-initialized fields.
     */
    public static EventOuterClass.Event.Builder newBuilder(
            Request<?> request,
            Response<?> response,
            AmazonHttpClient client,
            Exception e
    ) {
        ObjectMetadata objectMetadata;
        EventOuterClass.Event.Builder builder = AWSSDKOperation.newBuilder(request, response, client, e);
        if (response != null) {
            AmazonWebServiceRequest awsReq = request.getOriginalRequest();
            switch (builder.getResourceBuilder().getOperation()) {
                case "PutObject":
                    objectMetadata = (ObjectMetadata) response.getAwsResponse();
                    PutObjectRequest putObjReq = (PutObjectRequest) awsReq;
                    builder.getResourceBuilder()
                            .setName(putObjReq.getBucketName())
                            .putMetadata("key", putObjReq.getKey());
                    builder.getResourceBuilder()
                            .putMetadata("etag", objectMetadata.getETag());
                    break;
                case "HeadObject":
                    objectMetadata = (ObjectMetadata) response.getAwsResponse();
                    GetObjectMetadataRequest getMetadataReq = (GetObjectMetadataRequest) awsReq;
                    builder.getResourceBuilder()
                            .setName(getMetadataReq.getBucketName())
                            .putMetadata("key", getMetadataReq.getKey())
                            .putMetadata("etag", objectMetadata.getETag())
                            .putMetadata("file_size", Long.toString(objectMetadata.getContentLength()))
                            .putMetadata("last_modified", Long.toString(objectMetadata.getLastModified().getTime()));
                    break;
                case "GetObject":
                    S3Object s3Object = (S3Object) response.getAwsResponse();
                    objectMetadata = s3Object.getObjectMetadata();
                    GetObjectRequest getObjectReq = (GetObjectRequest) awsReq;
                    builder.getResourceBuilder()
                            .setName(getObjectReq.getBucketName())
                            .putMetadata("key", getObjectReq.getKey())
                            .putMetadata("etag", objectMetadata.getETag())
                            .putMetadata("file_size", Long.toString(objectMetadata.getContentLength()))
                            .putMetadata("last_modified", Long.toString(objectMetadata.getLastModified().getTime()));
                    break;

                case "ListObjects":
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.addMixIn(S3ObjectSummary.class, S3ObjectSummerySerializationMixIn.class);
                    ListObjectsRequest listObjectsReq = (ListObjectsRequest) awsReq;
                    ObjectListing listObjectsResponse = (ObjectListing) response.getAwsResponse();
                    List<S3ObjectSummary> objects = listObjectsResponse.getObjectSummaries();
                    MetadataBuilder metadataBuilder = new MetadataBuilder(builder.getResourceBuilder().getMetadataMap());

                    try {
                        metadataBuilder.putIfAllData("files", objectMapper.writeValueAsString(objects));
                        builder.getResourceBuilder()
                                .setName(listObjectsReq.getBucketName())
                                .putAllMetadata(metadataBuilder.build());
                    } catch (JsonProcessingException err) {
                        Trace.getInstance().addException(err);
                    }
                    break;
            }
        }
        return builder;
    }
}