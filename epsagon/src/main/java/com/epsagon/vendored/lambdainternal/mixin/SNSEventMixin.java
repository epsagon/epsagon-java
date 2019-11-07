package com.epsagon.vendored.lambdainternal.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface SNSEventMixin {
   public interface SNSRecordMixin {
      @JsonProperty("Sns")
      Object getSNS();
   }
}
