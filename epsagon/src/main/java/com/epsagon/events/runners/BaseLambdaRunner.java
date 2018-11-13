package com.epsagon.events.runners;

import com.amazonaws.services.lambda.runtime.Context;
import com.epsagon.ColdStart;
import com.epsagon.Region;
import com.epsagon.protocol.EventOuterClass;

/**
 * A builder for an event describing a generic Lambda runner.
 */
public class BaseLambdaRunner {
    /**
     * Creates a new Builder, with some fields pre-initialized.
     * @param context The AWS Context object the Lambda was triggered with.
     * @return A builder with pre-initialized fields.
     */
    public static EventOuterClass.Event.Builder newBuilder(Context context) {
        EventOuterClass.Event.Builder builder = Runner.newBuilder();
        builder.setId(context.getAwsRequestId());
        builder.getResourceBuilder()
                .setOperation("invoke")
                .setName(context.getFunctionName())
                .putMetadata("log_stream_name", context.getLogStreamName())
                .putMetadata("log_group_name", context.getLogGroupName())
                .putMetadata("function_version", context.getFunctionVersion())
                .putMetadata("memory", String.valueOf(context.getMemoryLimitInMB()))
                .putMetadata("cold_start", String.valueOf(ColdStart.getInstance().readAndSwitch()))
                .putMetadata("region", Region.getRegion())
                .putMetadata("aws_account", context.getInvokedFunctionArn().split(":")[4]);
        return builder;
    }
}
