package com.epsagon.events.operations.aws;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.epsagon.events.MetadataBuilder;
import com.epsagon.protocol.EventOuterClass;

/**
 * A builder for an event describing an SES operation.
 */
public class SESOperation {
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
                case "SendEmail":
                    SendEmailRequest sendEmailReq = (SendEmailRequest) awsReq;
                    SendEmailResult sendEmailRes = (SendEmailResult) response.getAwsResponse();
                    metadataBuilder
                            .putIfAllData("body", sendEmailReq.getMessage().getBody().toString())
                            .put("source", sendEmailReq.getSource())
                            .put("destination", sendEmailReq.getDestination().toString())
                            .put("subject", sendEmailReq.getMessage().getSubject().toString())
                            .put("message_id", sendEmailRes.getMessageId());
                    builder.getResourceBuilder()
                            .putAllMetadata(metadataBuilder.build());
            }
            builder.getResourceBuilder().putAllMetadata(metadataBuilder.build());

        }
        return builder;
    }
}