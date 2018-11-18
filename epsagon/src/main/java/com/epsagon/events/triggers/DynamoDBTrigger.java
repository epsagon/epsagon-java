package com.epsagon.events.triggers;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.epsagon.events.MetadataBuilder;
import com.epsagon.protocol.EventOuterClass;

/**
 * A builder factory for a DynamoDB trigger.
 */
public class DynamoDBTrigger {
    /**
     * @param event The event the Lambda was triggered with.
     * @return a builder for a DynamoDB trigger.
     * TODO: Still in progress.
     */
    public static EventOuterClass.Event.Builder newBuilder(
            DynamodbEvent event
    ) {
        DynamodbEvent.DynamodbStreamRecord first = event.getRecords().get(0);
        EventOuterClass.Event.Builder builder = BaseTrigger.newBuilder()
                .setId(first.getEventID());
        MetadataBuilder metadataBuilder = new MetadataBuilder(builder.getResourceBuilder().getMetadataMap())
                .put("region", first.getAwsRegion())
                .put("sequence_number", first.getDynamodb().getSequenceNumber())
                .put("item_hash", "test");

        builder.getResourceBuilder()
                .setName(first.getEventSourceARN())
                .setOperation(first.getEventName())
                .setType("dynamodb")
                .putAllMetadata(metadataBuilder.build());
        return builder;
    }

}
