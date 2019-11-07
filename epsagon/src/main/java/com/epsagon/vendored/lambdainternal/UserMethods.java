package com.epsagon.vendored.lambdainternal;

public final class UserMethods {
   public final Runnable initHandler;
   public final LambdaRequestHandler requestHandler;

   public UserMethods(Runnable var1, LambdaRequestHandler var2) {
      this.initHandler = var1;
      this.requestHandler = var2;
   }
}
