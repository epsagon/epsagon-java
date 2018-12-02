package com.epsagon.events.operations.aws;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.services.sqs.model.*;
import com.epsagon.events.MetadataBuilder;
import com.epsagon.protocol.EventOuterClass;

import java.util.List;

/**
 * A builder for an event describing an SQS operation.
 */
public class SQSOperation {
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
            String[] queueURL;
            AmazonWebServiceRequest awsReq = request.getOriginalRequest();
            switch (builder.getResourceBuilder().getOperation()) {
                case "SendMessage":
                    SendMessageRequest sendMessageReq = (SendMessageRequest) awsReq;
                    SendMessageResult sendMessageRes = (SendMessageResult) response.getAwsResponse();
                    queueURL = sendMessageReq.getQueueUrl().split("/");
                    builder.getResourceBuilder()
                            .setName(queueURL[queueURL.length - 1]);
                    metadataBuilder
                            .putIfAllData("Message Body", sendMessageReq.getMessageBody())
                            .put("Message ID", sendMessageRes.getMessageId())
                            .put("MD5 Of Message Body", sendMessageRes.getMD5OfMessageBody());
                    break;
                case "ReceiveMessage":
                    ReceiveMessageRequest receiveMessageReq = (ReceiveMessageRequest) awsReq;
                    ReceiveMessageResult receiveMessageRes = (ReceiveMessageResult) response.getAwsResponse();
                    queueURL = receiveMessageReq.getQueueUrl().split("/");
                    builder.getResourceBuilder()
                            .setName(queueURL[queueURL.length - 1]);
                    List<Message> messages = receiveMessageRes.getMessages();
                    metadataBuilder
                            .putIfAllData("Message Body", messages.get(0).getBody())
                            .put("Number Of Messages", Integer.toString(messages.size()))
                            .put("Message ID", messages.get(0).getMessageId())
                            .put("MD5 Of Message Body", messages.get(0).getMD5OfBody());
                    break;
            }
            builder.getResourceBuilder().putAllMetadata(metadataBuilder.build());

        }
        return builder;
    }
}