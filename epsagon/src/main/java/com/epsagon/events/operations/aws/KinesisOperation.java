package com.epsagon.events.operations.aws;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.epsagon.events.MetadataBuilder;
import com.epsagon.protocol.EventOuterClass;

/**
 * A builder for an event describing an Kinesis operation.
 */
public class KinesisOperation {
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

        EventOuterClass.Event.Builder builder = AWSSDKOperation.newBuilder(request, response, client, e);
        if (response != null) {
            MetadataBuilder metadataBuilder = new MetadataBuilder(builder.getResourceBuilder().getMetadataMap());
            AmazonWebServiceRequest awsReq = request.getOriginalRequest();
            switch (builder.getResourceBuilder().getOperation()) {
                case "PutRecord":
                    PutRecordRequest putRecordReq = (PutRecordRequest) awsReq;
                    PutRecordResult putRecordRes = (PutRecordResult) response.getAwsResponse();
                    builder.getResourceBuilder()
                            .setName(putRecordReq.getStreamName());
                    metadataBuilder
                            .putIfAllData("data", new String(putRecordReq.getData().array()))
                            .put("partition_key", putRecordReq.getPartitionKey())
                            .put("shard_id", putRecordRes.getShardId())
                            .put("sequence_number", putRecordRes.getSequenceNumber());
                    break;
            }
            builder.getResourceBuilder().putAllMetadata(metadataBuilder.build());

        }
        return builder;
    }
}