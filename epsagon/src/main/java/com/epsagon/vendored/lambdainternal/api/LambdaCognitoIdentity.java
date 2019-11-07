package com.epsagon.vendored.lambdainternal.api;

import com.amazonaws.services.lambda.runtime.CognitoIdentity;

public class LambdaCognitoIdentity implements CognitoIdentity {
   private final String identityId;
   private final String poolId;

   public LambdaCognitoIdentity(String var1, String var2) {
      this.identityId = var1;
      this.poolId = var2;
   }

   public String getIdentityId() {
      return this.identityId;
   }

   public String getIdentityPoolId() {
      return this.poolId;
   }
}
