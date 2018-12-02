package com.epsagon.events.operations.aws;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.handlers.HandlerContextKey;
import com.amazonaws.http.AmazonHttpClient;
import com.epsagon.protocol.EventOuterClass;

/**
 * Factory for AWS SDK operations.
 */
public class Factory {
    private final static Map<String, OperationBuilderInterface> OPERATIONS_BY_EVENT = (
            new HashMap<>()
    );

    static {
        OPERATIONS_BY_EVENT.put(
                "s3", S3Operation::newBuilder
        );
        OPERATIONS_BY_EVENT.put(
                "sqs", SQSOperation::newBuilder
        );
        OPERATIONS_BY_EVENT.put(
                "sns", SNSOperation::newBuilder
        );
        OPERATIONS_BY_EVENT.put(
                "kinesis", KinesisOperation::newBuilder
        );
        OPERATIONS_BY_EVENT.put(
                "ses", SESOperation::newBuilder
        );
        OPERATIONS_BY_EVENT.put(
                "dynamodb", DynamoDBOperation::newBuilder
        );
    }

    /**
     * An interface used for builders polymorphism.
     */
    @FunctionalInterface
    public interface OperationBuilderInterface {
        EventOuterClass.Event.Builder newBuilder(
                Request<?> request,
                Response<?> response,
                AmazonHttpClient client,
                Exception e
        );
    }

    /**
     * Creates a new Builder, with some fields pre-initialized, according to the request type
     *
     * @param request  The AWS Request object.
     * @param response The AWS Response object, if any. (may be null)
     * @param client   The Amazon Http Client.
     * @param error    An exception for the request, if any. (may be null)
     * @return A builder with pre-initialized fields.
     */
    public static EventOuterClass.Event.Builder newBuilder(
            Request<?> request,
            Response<?> response,
            AmazonHttpClient client,
            Exception error
    ) {
        return OPERATIONS_BY_EVENT.getOrDefault(
                request.getHandlerContext(HandlerContextKey.SERVICE_ID).toLowerCase(),
                (req, res, cli, e) -> null
        ).newBuilder(request, response, client, error);

    }

    /**
     * same as calling {@link Factory#newBuilder(Request, Response, AmazonHttpClient, Exception)} with
     * (request, response, null)
     *
     * @param request  The AWS Request object.
     * @param response The AWS Response object, if any. (may be null)
     * @return A builder with pre-initialized fields.
     */
    public static EventOuterClass.Event.Builder newBuilder(
            Request<?> request,
            Response<?> response,
            AmazonHttpClient client
    ) {
        return Factory.newBuilder(request, response, client, null);
    }

}
