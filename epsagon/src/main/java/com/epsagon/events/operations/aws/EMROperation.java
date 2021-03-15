package com.epsagon.events.operations.aws;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.services.elasticmapreduce.model.*;
import com.epsagon.events.MetadataBuilder;
import com.epsagon.protocol.EventOuterClass;

import java.util.List;

/**
 * A builder for an event describing an EMR operation.
 */
public class EMROperation {
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
        MetadataBuilder metadataBuilder = new MetadataBuilder(builder.getResourceBuilder().getMetadataMap());
        if (request != null) {
            AmazonWebServiceRequest awsReq = request.getOriginalRequest();
            switch (builder.getResourceBuilder().getOperation()) {
                case "AddJobFlowSteps":
                    AddJobFlowStepsRequest addJobFlowStepsRequest = (AddJobFlowStepsRequest) awsReq;
                    builder.getResourceBuilder()
                            .setName("")
                            .putMetadata("Steps", addJobFlowStepsRequest.getSteps().toString())
                            .putMetadata("Job Flow ID", addJobFlowStepsRequest.getJobFlowId());
                    break;
                case "TerminateJobFlows":
                    TerminateJobFlowsRequest terminateJobFlowsRequest = (TerminateJobFlowsRequest) awsReq;
                    builder.getResourceBuilder()
                            .setName("")
                            .putMetadata("Job Flow IDs", terminateJobFlowsRequest.getJobFlowIds().toString());
                    break;
                case "ListClusters":
                    ListClustersRequest listClustersRequest = (ListClustersRequest) awsReq;
                    builder.getResourceBuilder()
                            .setName("")
                            .putMetadata("Cluster States", listClustersRequest.getClusterStates().toString());
                    break;
                case "RunJobFlow":
                    RunJobFlowRequest runJobFlowRequest = (RunJobFlowRequest) awsReq;
                    builder.getResourceBuilder()
                            .setName("")
                            .putMetadata("Request", runJobFlowRequest.toString());
                    break;
            }
        }
        if (response != null) {
            AmazonWebServiceRequest awsReq = request.getOriginalRequest();
            switch (builder.getResourceBuilder().getOperation()) {
                case "AddJobFlowSteps":
                    AddJobFlowStepsResult addJobFlowStepsResult = (AddJobFlowStepsResult) response.getAwsResponse();
                    builder.getResourceBuilder()
                            .putMetadata("Step Ids", addJobFlowStepsResult.getStepIds().toString());
                    break;
                case "ListClusters":
                    ListClustersResult listClustersResult = (ListClustersResult) response.getAwsResponse();
                    builder.getResourceBuilder()
                            .putMetadata("Clusters", listClustersResult.getClusters().toString());
                    break;
                case "RunJobFlow":
                    RunJobFlowResult runJobFlowResult = (RunJobFlowResult) response.getAwsResponse();
                    builder.getResourceBuilder()
                            .putMetadata("Job Flow ID", runJobFlowResult.getJobFlowId());
                    break;

            }
            builder.getResourceBuilder().putAllMetadata(metadataBuilder.build());
        }
        return builder;
    }
}