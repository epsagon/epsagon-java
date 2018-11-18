package com.epsagon.events.triggers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.epsagon.events.MetadataBuilder;
import com.epsagon.protocol.EventOuterClass;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A builder factory for a APIGateway trigger.
 */
public class APIGatewayTrigger {

    private static ObjectMapper _objectMapper = new ObjectMapper();

    /**
     * @param event The event the Lambda was triggered with.
     * @return a builder for a APIGateway trigger.
     */
    public static EventOuterClass.Event.Builder newBuilder(
            APIGatewayProxyRequestEvent event
    ) {
        APIGatewayProxyRequestEvent.ProxyRequestContext context = event.getRequestContext();
        EventOuterClass.Event.Builder builder = BaseTrigger.newBuilder()
                .setId(context.getRequestId());
        String queryParams, pathParams;
        try {
            queryParams = _objectMapper.writeValueAsString(event.getQueryStringParameters());
            pathParams = _objectMapper.writeValueAsString(event.getPathParameters());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            queryParams = "Unknown";
            pathParams = "Unknown";
        }
        MetadataBuilder metadataBuilder = new MetadataBuilder(builder.getResourceBuilder().getMetadataMap())
                .put("stage", context.getStage())
                .put("query_string_parameters", queryParams)
                .put("path_parameters", pathParams);

        builder.getResourceBuilder()
                .setName(event.getResource())
                .setOperation(event.getHttpMethod())
                .setType("api_gateway")
                .putAllMetadata(metadataBuilder.build());
        return builder;
    }

}
