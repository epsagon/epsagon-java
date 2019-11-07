package com.epsagon.vendored.lambdainternal;

public class LambdaRuntime {
   public static final int MEMORY_LIMIT = Integer.parseInt(getEnv("AWS_LAMBDA_FUNCTION_MEMORY_SIZE"));
   public static final String LOG_GROUP_NAME = getEnv("AWS_LAMBDA_LOG_GROUP_NAME");
   public static final String LOG_STREAM_NAME = getEnv("AWS_LAMBDA_LOG_STREAM_NAME");
   public static final String FUNCTION_NAME = getEnv("AWS_LAMBDA_FUNCTION_NAME");
   public static final String FUNCTION_VERSION = getEnv("AWS_LAMBDA_FUNCTION_VERSION");
   public static volatile boolean needsDebugLogs = false;

   public static String getEnv(String var0) {
      return System.getenv(var0);
   }

   public static native void initRuntime();

   public static native void reportRunning(String var0);

   public static native void reportDone(String var0, byte[] var1, int var2);

   public static native void reportFault(String var0, String var1, String var2, String var3);

   public static native void setenv(String var0, String var1, int var2);

   public static native void unsetenv(String var0);

   public static native WaitForStartResult waitForStart(int var0);

   public static native InvokeRequest waitForInvoke();

   public static native int getRemainingTime();

   public static native void sendContextLogs(String var0);

   public static synchronized native void streamLogsToSlicer(byte[] var0, int var1, int var2);

   public static class WaitForStartResult {
      public final String invokeid;
      public final String handler;
      public final String mode;
      public final AWSCredentials credentials;
      public final boolean suppressInit;

      public WaitForStartResult(String var1, String var2, String var3, String var4, String var5, String var6, boolean var7) {
         this.invokeid = var1;
         this.handler = var2;
         this.mode = var3;
         this.credentials = new AWSCredentials(var4, var5, var6);
         this.suppressInit = var7;
      }
   }

   public static class InvokeRequest {
      public final int sockfd;
      public final String invokeid;
      public final AWSCredentials credentials;
      public final String clientContext;
      public final String cognitoIdentityId;
      public final String cognitoPoolId;
      public final long eventBodyAddr;
      public final int eventBodyLen;
      public final boolean needsDebugLogs;
      public final String invokedFunctionArn;

      public InvokeRequest(int var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, long var9, int var11, boolean var12, String var13) {
         this.sockfd = var1;
         this.invokeid = var2;
         this.eventBodyAddr = var9;
         this.eventBodyLen = var11;
         this.clientContext = var6;
         this.cognitoIdentityId = var7;
         this.cognitoPoolId = var8;
         this.credentials = new AWSCredentials(var3, var4, var5);
         this.needsDebugLogs = var12;
         this.invokedFunctionArn = var13;
      }
   }

   public static class AWSCredentials {
      public final String key;
      public final String secret;
      public final String session;

      public AWSCredentials(String var1, String var2, String var3) {
         this.key = var1;
         this.secret = var2;
         this.session = var3;
      }
   }
}
