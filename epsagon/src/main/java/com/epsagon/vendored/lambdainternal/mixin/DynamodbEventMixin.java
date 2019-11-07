package com.epsagon.vendored.lambdainternal.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DynamodbEventMixin {
   String L = "L";
   String M = "M";
   String BS = "BS";
   String NS = "NS";
   String SS = "SS";
   String BOOL = "BOOL";
   String NULL = "NULL";
   String B = "B";
   String N = "N";
   String S = "S";
   String OLD_IMAGE = "OldImage";
   String NEW_IMAGE = "NewImage";
   String STREAM_VIEW_TYPE = "StreamViewType";
   String SEQUENCE_NUMBER = "SequenceNumber";
   String SIZE_BYTES = "SizeBytes";
   String KEYS = "Keys";
   String AWS_REGION = "awsRegion";
   String DYNAMODB = "dynamodb";
   String EVENT_ID = "eventID";
   String EVENT_NAME = "eventName";
   String EVENT_SOURCE = "eventSource";
   String EVENT_VERSION = "eventVersion";
   String EVENT_SOURCE_ARN = "eventSourceARN";
   String APPROXIMATE_CREATION_DATE_TIME = "ApproximateCreationDateTime";

   @JsonProperty("Records")
   List getRecords();

   public interface AttributeValueMixIn {
      @JsonProperty("S")
      String getS();

      @JsonProperty("S")
      void setS(String var1);

      @JsonProperty("N")
      String getN();

      @JsonProperty("N")
      void setN(String var1);

      @JsonProperty("B")
      ByteBuffer getB();

      @JsonProperty("B")
      void setB(ByteBuffer var1);

      @JsonProperty("NULL")
      Boolean isNULL();

      @JsonProperty("NULL")
      void setNULL(Boolean var1);

      @JsonProperty("BOOL")
      Boolean getBOOL();

      @JsonProperty("BOOL")
      void setBOOL(Boolean var1);

      @JsonProperty("SS")
      List getSS();

      @JsonProperty("SS")
      void setSS(List var1);

      @JsonProperty("NS")
      List getNS();

      @JsonProperty("NS")
      void setNS(List var1);

      @JsonProperty("BS")
      List getBS();

      @JsonProperty("BS")
      void setBS(List var1);

      @JsonProperty("M")
      Map getM();

      @JsonProperty("M")
      void setM(Map var1);

      @JsonProperty("L")
      List getL();

      @JsonProperty("L")
      void setL(List var1);
   }

   public interface StreamRecordMixin {
      @JsonProperty("Keys")
      Map getKeys();

      @JsonProperty("Keys")
      void setKeys(Map var1);

      @JsonProperty("SizeBytes")
      Long getSizeBytes();

      @JsonProperty("SizeBytes")
      void setSizeBytes(Long var1);

      @JsonProperty("SequenceNumber")
      String getSequenceNumber();

      @JsonProperty("SequenceNumber")
      void setSequenceNumber(String var1);

      @JsonProperty("StreamViewType")
      String getStreamViewType();

      @JsonProperty("StreamViewType")
      void setStreamViewType(String var1);

      @JsonProperty("NewImage")
      Map getNewImage();

      @JsonProperty("NewImage")
      void setNewImage(Map var1);

      @JsonProperty("OldImage")
      Map getOldImage();

      @JsonProperty("OldImage")
      void setOldImage(Map var1);

      @JsonProperty("ApproximateCreationDateTime")
      Date getApproximateCreationDateTime();

      @JsonProperty("ApproximateCreationDateTime")
      void setApproximateCreationDateTime(Date var1);
   }

   public interface RecordMixin {
      @JsonProperty("awsRegion")
      String getAwsRegion();

      @JsonProperty("awsRegion")
      void setAwsRegion(String var1);

      @JsonProperty("dynamodb")
      Object getDynamodb();

      @JsonProperty("dynamodb")
      void setDynamodb(Object var1);

      @JsonProperty("eventID")
      String getEventID();

      @JsonProperty("eventID")
      void setEventID(String var1);

      @JsonProperty("eventName")
      String getEventName();

      @JsonProperty("eventName")
      void setEventName(String var1);

      @JsonProperty("eventSource")
      String getEventSource();

      @JsonProperty("eventSource")
      void setEventSource(String var1);

      @JsonProperty("eventVersion")
      String getEventVersion();

      @JsonProperty("eventVersion")
      void setEventVersion(String var1);

      @JsonProperty("eventSourceARN")
      String getEventSourceArn();

      @JsonProperty("eventSourceARN")
      void setEventSourceArn(String var1);
   }
}
