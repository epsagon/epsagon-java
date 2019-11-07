package com.epsagon.vendored.lambdainternal.api;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.epsagon.vendored.lambdainternal.LambdaRuntime;

import java.util.Arrays;

public class LambdaContextLogger implements LambdaLogger {
   public void log(String var1) {
      LambdaRuntime.sendContextLogs(var1);
   }

   public void log(byte[] var1) {
      log(Arrays.toString(var1));
   }
}
