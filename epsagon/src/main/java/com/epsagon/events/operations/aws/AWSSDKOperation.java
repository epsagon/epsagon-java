package com.epsagon.events.operations.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.handlers.HandlerContextKey;
import com.amazonaws.http.AmazonHttpClient;
import com.epsagon.TimeHelper;
import com.epsagon.events.EventBuildHelper;
import com.epsagon.protocol.EventOuterClass;

import java.util.Optional;

/**
 * A Builder for an AWS SDK Event.
 */
public class AWSSDKOperation {
    /**
     * Creates a new Builder, with some fields pre-initialized.
     * @param request The AWS Request object.
     * @param response The AWS Response object, if any. (may be null)
     * @param client The Amazon Http Client.
     * @param e An exception for the request, if any. (may be null)
     * @return A builder with pre-initialized fields.
     */
    public static EventOuterClass.Event.Builder newBuilder(
            Request<?> request,
            Response<?> response,
            AmazonHttpClient client,
            Exception e
    ) {
        EventOuterClass.Event.Builder builder = EventOuterClass.Event.newBuilder()
                .setOrigin("aws-sdk")
                .setStartTime(TimeHelper.fromMillis(
                        request.getAWSRequestMetrics().getTimingInfo().getStartEpochTimeMilliIfKnown()
                ));

        EventBuildHelper.setDuration(
                builder,
                Optional.ofNullable(
                    request.getAWSRequestMetrics().getTimingInfo().getEndEpochTimeMilliIfKnown()
                ).orElse(TimeHelper.getCurrentMillis())
        );

        builder.getResourceBuilder()
                .setOperation(request.getHandlerContext(HandlerContextKey.OPERATION_NAME))
                .setType(request.getHandlerContext(HandlerContextKey.SERVICE_ID).toLowerCase())
                .putMetadata("Region", request.getHandlerContext(HandlerContextKey.SIGNING_REGION));

        if (response != null) {
            builder.setId(client.getResponseMetadataForRequest(request.getOriginalRequest()).getRequestId())
                    .getResourceBuilder()
                    .putMetadata("Status Code", String.valueOf(response.getHttpResponse().getStatusCode()));
                    // TODO Add retry attempts
        }

        if (e != null) {
            EventBuildHelper.setException(builder, e);
            if (e instanceof AmazonServiceException) {
                AmazonServiceException amazonErr = (AmazonServiceException) e;
                builder.setId(amazonErr.getRequestId()).getResourceBuilder()
                        .putMetadata("Status Code", String.valueOf(amazonErr.getStatusCode()));
                        // TODO Add retry attempts
            }
        }
        return builder;
    }
}
