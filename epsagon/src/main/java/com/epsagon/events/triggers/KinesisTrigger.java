package com.epsagon.events.triggers;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.epsagon.events.MetadataBuilder;
import com.epsagon.protocol.EventOuterClass;

/**
 * A builder factory for a Kinesis trigger.
 */
public class KinesisTrigger {
    /**
     * @param event The event the Lambda was triggered with.
     * @return a builder for a Kinesis trigger.
     */
    public static EventOuterClass.Event.Builder newBuilder(
            KinesisEvent event
    ) {
        KinesisEvent.KinesisEventRecord first = event.getRecords().get(0);
        EventOuterClass.Event.Builder builder = BaseTrigger.newBuilder()
                .setId(first.getEventID());
        KinesisEvent.Record kinesis = first.getKinesis();
        MetadataBuilder metadataBuilder = new MetadataBuilder(builder.getResourceBuilder().getMetadataMap())
                .put("region", first.getAwsRegion())
                .put("invoke_identity", first.getInvokeIdentityArn())
                .put("sequence_number", kinesis.getSequenceNumber())
                .put("partition_key", kinesis.getPartitionKey());

        String[] sourceArn = first.getEventSourceARN().split("/");
        builder.getResourceBuilder()
                .setName(sourceArn[sourceArn.length - 1])
                .setOperation(first.getEventName().replace("aws:kinesis:", ""))
                .setType("kinesis")
                .putAllMetadata(metadataBuilder.build());
        return builder;
    }

}
