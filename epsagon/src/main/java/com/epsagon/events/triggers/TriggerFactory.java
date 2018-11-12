package com.epsagon.events.triggers;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.epsagon.protocol.EventOuterClass;

/**
 * A factory for AWS Lambda trigger events.
 */
public class TriggerFactory {
    private final static Map<Class<?>, TriggerNewBuilderInterface> TRIGGERS_BY_EVENT = new HashMap<>();
    static {
        TRIGGERS_BY_EVENT.put(
                S3Event.class, (event) -> S3Trigger.newBuilder((S3Event) event)
        );
        TRIGGERS_BY_EVENT.put(
                SNSEvent.class, (event) -> SNSTrigger.newBuilder((SNSEvent) event)
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
     * Creates an appropriate trigger event builder.
     * @param event The event the Lambda was triggered with.
     * @param context The context the Lambda was triggered with.
     * @return An event builder initialized with the required fields.
     */
    public static EventOuterClass.Event.Builder newBuilder(
        Object event,
        Context context
    ) {
        return TRIGGERS_BY_EVENT.getOrDefault(
            event.getClass(),
            (e) -> JSONTrigger.newBuilder(e, context)
        ).newBuilder(event);
    }
}
