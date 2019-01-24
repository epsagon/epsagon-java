package com.epsagon.events.triggers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.*;

import com.epsagon.Trace;
import com.epsagon.protocol.EventOuterClass;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.io.IOUtils;

/**
 * A factory for AWS Lambda trigger events.
 */
public class TriggerFactory {
    private final static Map<Class<?>, TriggerNewBuilderInterface> TRIGGERS_BY_EVENT = new HashMap<>();
    private final static Map<String, TriggerNewBuilderInterfaceFromTree> TRIGGERS_BY_NAME = new HashMap<>();
    private final static ObjectMapper _objectMapper = new ObjectMapper()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .registerModule(new JodaModule());

    // TODO: change all the newBuilder to not be static, so we can
    // TODO: polymorphism with them instead of these hacks
    private static void addTriggerBuilder(
            String name,
            Class<?> clazz,
            TriggerNewBuilderInterface builder
    ) {
        TRIGGERS_BY_NAME.put(
                name,
                (JsonNode root) -> builder.newBuilder(
                        _objectMapper.treeToValue(root, clazz)
                )
        );
        TRIGGERS_BY_EVENT.put(
                clazz,
                builder
        );
    }

    static {
        addTriggerBuilder(
                "s3",
                S3Event.class,
                (event) -> S3Trigger.newBuilder((S3Event) event)
        );
        addTriggerBuilder(
                "sns",
                SNSEvent.class,
                (event) -> SNSTrigger.newBuilder((SNSEvent) event)
        );
        addTriggerBuilder(
                "sqs",
                SQSEvent.class,
                (event) -> SQSTrigger.newBuilder((SQSEvent) event)
        );
        addTriggerBuilder(
                "kinesis",
                KinesisEvent.class,
                (event) -> KinesisTrigger.newBuilder((KinesisEvent) event)
        );
        addTriggerBuilder(
                "api_gateway",
                APIGatewayProxyRequestEvent.class,
                (event) -> APIGatewayTrigger.newBuilder((APIGatewayProxyRequestEvent) event)
        );
    }

    /**
     * An interface used to dispatch trigger creation lambda expressions.
     */
    @FunctionalInterface
    interface TriggerNewBuilderInterface {
        EventOuterClass.Event.Builder newBuilder(Object event);
    }

    /**
     * An interface used to dispatch trigger creation lambda expressions.
     */
    @FunctionalInterface
    interface TriggerNewBuilderInterfaceFromTree {
        EventOuterClass.Event.Builder newBuilder(JsonNode root) throws IOException;
    }

    /**
     * Creates an appropriate trigger event builder.
     * @param event The event the Lambda was triggered with.
     * @param context The context the Lambda was triggered with.
     * @return An event builder initialized with the required fields.
     */
    public static EventOuterClass.Event.Builder newBuilder(
            Object event,
            Context context
    ) {
        if (event == null) {
            return JSONTrigger.newBuilder(null, context);
        }
        return TRIGGERS_BY_EVENT.getOrDefault(
                event.getClass(),
                (e) -> JSONTrigger.newBuilder(e, context)
        ).newBuilder(event);
    }

    /**
     * Creates an appropriate trigger event builder, from an event string.
     * @param event The event the Lambda was triggered with.
     * @param context The context the Lambda was triggered with.
     * @return An event builder initialized with the required fields.
     */
    public static EventOuterClass.Event.Builder newBuilder(
            String event,
            Context context
    ) {
        if (event == null) {
            return JSONTrigger.newBuilder(null, context);
        }

        JsonNode eventRoot;
        try {
            eventRoot = _objectMapper.readTree(event);
        } catch (IOException e) {
            return JSONTrigger.newBuilder(event, context);
        }

        String service = "json";

        if (eventRoot.hasNonNull("Records")) {
            JsonNode records = eventRoot.get("Records");
            if (records.isArray() && records.get(0) != null) {
                JsonNode first = records.get(0);
                String eventSourceId = (
                        first.hasNonNull("eventSource") ?
                                "eventSource" : "EventSource"
                );
                JsonNode eventSource = first.get(eventSourceId);
                if (eventSource != null && eventSource.isTextual()) {
                    String[] elements = eventSource.asText().split(":");
                    service = elements[elements.length - 1];
                }
            }
            //handle records
        } else if (eventRoot.hasNonNull("httpMethod")) {
            service = "api_gateway";

        } else if (eventRoot.hasNonNull("source")) {
            JsonNode source = eventRoot.get("httpMethod");
            if (source.isTextual()) {
                String[] elements = source.asText().split(":");
                service = elements[elements.length - 1];
            }
        }

        try {
            return TRIGGERS_BY_NAME.getOrDefault(
                    service,
                    (e) -> JSONTrigger.newBuilder(eventRoot, context)
            ).newBuilder(eventRoot);
        } catch (IOException e) {
            return JSONTrigger.newBuilder(event, context);
        }
    }
}
