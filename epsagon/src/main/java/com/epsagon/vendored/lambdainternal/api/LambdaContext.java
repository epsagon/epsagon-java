package com.epsagon.vendored.lambdainternal.api;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.LambdaRuntime;

public class LambdaContext implements Context {
   private int memoryLimit;
   private final String awsRequestId;
   private final String logGroupName;
   private final String logStreamName;
   private final String functionName;
   private final String functionVersion;
   private final String invokedFunctionArn;
   private final CognitoIdentity cognitoIdentity;
   private final ClientContext clientContext;
   private final LambdaLogger logger;

   public LambdaContext(int var1, String var2, String var3, String var4, String var5, CognitoIdentity var6, String var7, String var8, ClientContext var9) {
      this.memoryLimit = var1;
      this.awsRequestId = var2;
      this.logGroupName = var3;
      this.logStreamName = var4;
      this.functionName = var5;
      this.cognitoIdentity = var6;
      this.clientContext = var9;
      this.functionVersion = var7;
      this.invokedFunctionArn = var8;
      this.logger = LambdaRuntime.getLogger();
   }

   public int getMemoryLimitInMB() {
      return this.memoryLimit;
   }

   public String getAwsRequestId() {
      return this.awsRequestId;
   }

   public String getLogGroupName() {
      return this.logGroupName;
   }

   public String getLogStreamName() {
      return this.logStreamName;
   }

   public String getFunctionName() {
      return this.functionName;
   }

   public String getFunctionVersion() {
      return this.functionVersion;
   }

   public String getInvokedFunctionArn() {
      return this.invokedFunctionArn;
   }

   public CognitoIdentity getIdentity() {
      return this.cognitoIdentity;
   }

   public ClientContext getClientContext() {
      return this.clientContext;
   }

   public int getRemainingTimeInMillis() {
      return com.epsagon.vendored.lambdainternal.LambdaRuntime.getRemainingTime();
   }

   public LambdaLogger getLogger() {
      return this.logger;
   }
}
