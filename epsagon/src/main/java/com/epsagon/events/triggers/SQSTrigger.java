package com.epsagon.events.triggers;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.epsagon.events.MetadataBuilder;
import com.epsagon.protocol.EventOuterClass;

import java.util.Map;

/**
 * A builder factory for a SQS trigger.
 */
public class SQSTrigger {
    /**
     * @param event The event the Lambda was triggered with.
     * @return a builder for a SQS trigger.
     */
    public static EventOuterClass.Event.Builder newBuilder(
            SQSEvent event
    ) {
        SQSEvent.SQSMessage first = event.getRecords().get(0);
        EventOuterClass.Event.Builder builder = BaseTrigger.newBuilder()
                .setId(first.getMessageId());
        Map<String, String> messageAtributes = first.getAttributes();
        MetadataBuilder metadataBuilder = new MetadataBuilder(builder.getResourceBuilder().getMetadataMap())
                .put("MD5 Of Message Body", first.getMd5OfBody())
                .put("Sender ID", messageAtributes.get("SenderId"))
                .put("Approximate Receive Count", messageAtributes.get("ApproximateReceiveCount"))
                .put("Sent Timestamp", messageAtributes.get("SentTimestamp"))
                .put("Approximate First Receive Timestamp", messageAtributes.get("ApproximateFirstReceiveTimestamp"))
                .putIfAllData("Message Body", first.getBody());

        builder.getResourceBuilder()
                .setName(first.getEventSourceArn().split(":")[5])
                .setOperation("ReceiveMessage")
                .setType("sqs")
                .putAllMetadata(metadataBuilder.build());
        return builder;
    }

}
