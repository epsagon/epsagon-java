package com.epsagon.vendored.lambdainternal;

import com.epsagon.vendored.lambdainternal.util.LambdaByteArrayOutputStream;

public interface LambdaRequestHandler {
   LambdaByteArrayOutputStream call(LambdaRuntime.InvokeRequest var1) throws Error, Exception;

   public static class UserFaultHandler implements LambdaRequestHandler {
      private final UserFault fault;

      public UserFaultHandler(UserFault var1) {
         this.fault = var1;
      }

      public LambdaByteArrayOutputStream call(LambdaRuntime.InvokeRequest var1) {
         throw this.fault;
      }
   }
}
