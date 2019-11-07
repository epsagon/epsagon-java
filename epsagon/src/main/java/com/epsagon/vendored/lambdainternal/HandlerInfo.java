package com.epsagon.vendored.lambdainternal;

public final class HandlerInfo {
   public final Class clazz;
   public final String methodName;

   public HandlerInfo(Class var1, String var2) {
      this.clazz = var1;
      this.methodName = var2;
   }

   public static HandlerInfo fromString(String var0, ClassLoader var1) throws ClassNotFoundException, NoClassDefFoundError, InvalidHandlerException {
      int var2 = var0.lastIndexOf("::");
      String var3;
      String var4;
      if(var2 < 0) {
         var3 = var0;
         var4 = null;
      } else {
         var3 = var0.substring(0, var2);
         var4 = var0.substring(var2 + 2);
      }

      if(!var3.isEmpty() && (var4 == null || !var4.isEmpty())) {
         return new HandlerInfo(Class.forName(var3, true, var1), var4);
      } else {
         throw new InvalidHandlerException();
      }
   }

   public static String className(String var0) {
      int var1 = var0.lastIndexOf("::");
      return var1 < 0?var0:var0.substring(0, var1);
   }

   public static class InvalidHandlerException extends RuntimeException {
      public static final long serialVersionUID = -1L;
   }
}
