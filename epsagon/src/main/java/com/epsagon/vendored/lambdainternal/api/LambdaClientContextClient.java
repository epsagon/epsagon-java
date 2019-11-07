package com.epsagon.vendored.lambdainternal.api;

import com.amazonaws.services.lambda.runtime.Client;
import com.google.gson.annotations.SerializedName;

public class LambdaClientContextClient implements Client {
   @SerializedName("installation_id")
   private String installationId;
   @SerializedName("app_title")
   private String appTitle;
   @SerializedName("app_version_name")
   private String appVersionName;
   @SerializedName("app_version_code")
   private String appVersionCode;
   @SerializedName("app_package_name")
   private String appPackageName;

   public String getInstallationId() {
      return this.installationId;
   }

   public String getAppTitle() {
      return this.appTitle;
   }

   public String getAppVersionName() {
      return this.appVersionName;
   }

   public String getAppVersionCode() {
      return this.appVersionCode;
   }

   public String getAppPackageName() {
      return this.appPackageName;
   }
}
