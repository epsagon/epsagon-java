package com.epsagon.vendored.lambdainternal.api;

import com.amazonaws.services.lambda.runtime.Client;
import com.amazonaws.services.lambda.runtime.ClientContext;
import java.util.Map;

public class LambdaClientContext implements ClientContext {
   private LambdaClientContextClient client;
   private Map custom;
   private Map env;

   public Client getClient() {
      return this.client;
   }

   public Map getCustom() {
      return this.custom;
   }

   public Map getEnvironment() {
      return this.env;
   }
}
