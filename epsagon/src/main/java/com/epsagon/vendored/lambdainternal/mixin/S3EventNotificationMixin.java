package com.epsagon.vendored.lambdainternal.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public interface S3EventNotificationMixin {
   @JsonProperty("Records")
   List getRecords();

   public interface ResponseElementsEntityMixin {
      @JsonProperty("x-amz-id-2")
      String getxAmzId2();

      @JsonProperty("x-amz-request-id")
      String getxAmzRequestId();
   }
}
