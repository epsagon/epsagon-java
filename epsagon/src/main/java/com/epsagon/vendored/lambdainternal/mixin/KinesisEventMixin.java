package com.epsagon.vendored.lambdainternal.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public interface KinesisEventMixin {
   @JsonProperty("Records")
   List getRecords();
}
