package com.epsagon.events.operations.aws;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.epsagon.events.MetadataBuilder;
import com.epsagon.protocol.EventOuterClass;

/**
 * A builder for an event describing an SNS operation.
 */
public class SNSOperation {
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
                case "CreateTopic":
                    CreateTopicRequest createTopicReq = (CreateTopicRequest) awsReq;
                    builder.getResourceBuilder()
                            .setName(createTopicReq.getName());

                    break;
                case "Publish":
                    PublishRequest publishReq = (PublishRequest) awsReq;
                    PublishResult publishRes = (PublishResult) response.getAwsResponse();
                    String[] topicArn = publishReq.getTopicArn().split(":");
                    metadataBuilder
                            .putIfAllData("Notification Message", publishReq.getMessage())
                            .put("message_id", publishRes.getMessageId());
                    builder.getResourceBuilder()
                            .setName(topicArn[topicArn.length - 1])
                            .putAllMetadata(metadataBuilder.build());
            }
            builder.getResourceBuilder().putAllMetadata(metadataBuilder.build());

        }
        return builder;
    }
}