package com.epsagon.vendored.lambdainternal;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class UserFault extends RuntimeException {
   private static final long serialVersionUID = 0L;
   public final String msg;
   public final String exception;
   public final String trace;

   public UserFault(String var1, String var2, String var3) {
      this.msg = var1;
      this.exception = var2;
      this.trace = var3;
   }

   public static UserFault makeUserFault(Throwable var0) {
      String var1 = var0.getLocalizedMessage() == null?var0.getClass().getName():var0.getLocalizedMessage();
      return new UserFault(var1, var0.getClass().getName(), trace(var0));
   }

   public static UserFault makeUserFault(String var0) {
      return new UserFault(var0, (String)null, (String)null);
   }

   public static String trace(Throwable var0) {
      filterStackTrace(var0);
      StringWriter var1 = new StringWriter();
      var0.printStackTrace(new PrintWriter(var1));
      return var1.toString();
   }

   public static Throwable filterStackTrace(Throwable var0) {
      StackTraceElement[] var1 = var0.getStackTrace();

      for(int var2 = 0; var2 < var1.length; ++var2) {
         if(var1[var2].getClassName().startsWith("com/epsagon/vendored/lambdainternal")) {
            StackTraceElement[] var3 = new StackTraceElement[var2];
            System.arraycopy(var1, 0, var3, 0, var2);
            var0.setStackTrace(var3);
            break;
         }
      }

      Throwable var4 = var0.getCause();
      if(var4 != null) {
         filterStackTrace(var4);
      }

      return var0;
   }
}
