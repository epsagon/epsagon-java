package com.epsagon.vendored.lambdainternal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import com.epsagon.vendored.lambdainternal.mixin.DateTimeModule;
import com.epsagon.vendored.lambdainternal.util.LambdaByteArrayOutputStream;
import com.epsagon.vendored.lambdainternal.util.ReflectUtil;
import com.epsagon.vendored.lambdainternal.mixin.S3EventNotificationMixin;
import com.epsagon.vendored.lambdainternal.util.Functions;

public class AwsJackson {
   private static final String S3_EVENT_NOTIFICATION = "com.amazonaws.services.s3.event.S3EventNotification";
   private static final String RESPONSE_ELEMENTS_ENTITY = "com.amazonaws.services.s3.event.S3EventNotification$ResponseElementsEntity";
   private static final Functions.R5 defineClass = getDefineClass();
   private static final Functions.V2 resolveClass = ReflectUtil.loadInstanceV1(ClassLoader.class, "resolveClass", true, Class.class);
   private static final Map awsJacksonCache = new HashMap();
   private final Object mapper;
   private final Functions.R1 writeValueAsString;

   private static Functions.R5 getDefineClass() {
      return ReflectUtil.loadInstanceR4(ClassLoader.class, "defineClass", true, Class.class, String.class, byte[].class, Integer.TYPE, Integer.TYPE);
   }

   public static AwsJackson getCached(ClassLoader var0) {
      Map var1 = awsJacksonCache;
      synchronized(awsJacksonCache) {
         AwsJackson var2 = (AwsJackson)awsJacksonCache.get(var0);
         if(var2 == null) {
            var2 = new AwsJackson(var0);
            awsJacksonCache.put(var0, var2);
         }

         return var2;
      }
   }

   public AwsJackson(ClassLoader var1) {
      Class var2 = ReflectUtil.loadClass(var1, "com.amazonaws.util.json.Jackson");
      Object var3 = ReflectUtil.loadStaticR0(var2, "getObjectMapper", false, Object.class).call();
      this.mapper = ReflectUtil.bindInstanceR0(var3, "copy", false, var3.getClass()).call();
      configureMapper(this.mapper);
      this.writeValueAsString = ReflectUtil.bindInstanceR1(this.mapper, "writeValueAsString", false, String.class, Object.class);
      Functions.V2 var4 = ReflectUtil.bindInstanceV2(this.mapper, "addMixInAnnotations", false, Class.class, Class.class);
      Class var5 = ReflectUtil.loadClass(var1, "com.amazonaws.services.s3.event.S3EventNotification");
      Class var6 = ReflectUtil.loadClass(var1, "com.amazonaws.services.s3.event.S3EventNotification$ResponseElementsEntity");
      Class var7 = copyClass(S3EventNotificationMixin.class, var1);
      Class var8 = copyClass(S3EventNotificationMixin.ResponseElementsEntityMixin.class, var1);
      addModules(this.mapper);
      var4.call(var5, var7);
      var4.call(var6, var8);
   }

   private static Class copyClass(Class var0, ClassLoader var1) {
      try {
         return var1.loadClass(var0.getName());
      } catch (ClassNotFoundException var5) {
         LambdaByteArrayOutputStream var2 = getBytes(var0);

         try {
            Class var3 = (Class)defineClass.call(var1, var0.getName(), var2.getRawBuf(), Integer.valueOf(0), Integer.valueOf(var2.getValidByteCount()));
            resolveClass.call(var1, var3);
            return var3;
         } catch (SecurityException | ClassFormatError var4) {
            throw new ReflectUtil.ReflectException(var4);
         }
      }
   }

   private static LambdaByteArrayOutputStream getBytes(Class var0) {
      String var1 = var0.getName().replace('.', '/') + ".class";

      try {
         InputStream var2 = var0.getClassLoader().getResourceAsStream(var1);
         Throwable var3 = null;

         LambdaByteArrayOutputStream var6;
         try {
            int var4 = Math.max(1024, var2.available());
            LambdaByteArrayOutputStream var5 = new LambdaByteArrayOutputStream(var4);
            var5.readAll(var2);
            var6 = var5;
         } catch (Throwable var16) {
            var3 = var16;
            throw var16;
         } finally {
            if(var2 != null) {
               if(var3 != null) {
                  try {
                     var2.close();
                  } catch (Throwable var15) {
                     var3.addSuppressed(var15);
                  }
               } else {
                  var2.close();
               }
            }

         }

         return var6;
      } catch (IOException var18) {
         throw new UncheckedIOException(var18);
      }
   }

   private static void addModules(Object var0) {
      ClassLoader var1 = var0.getClass().getClassLoader();
      copyClass(DateTimeModule.Serializer.class, var1);
      Object var2 = ReflectUtil.loadConstructor0(copyClass(DateTimeModule.class, var1), false).call();
      Class var3 = ReflectUtil.loadClass(var1, "com.fasterxml.jackson.databind.Module");
      Functions.V1 var4 = ReflectUtil.bindInstanceV1(var0, "registerModule", false, var3);
      var4.call(var2);
   }

   private static void configureMapper(Object var0) {
      ClassLoader var1 = var0.getClass().getClassLoader();
      Class var2 = ReflectUtil.loadClass(var1, "com.fasterxml.jackson.annotation.JsonInclude$Include");
      Functions.R1 var3 = ReflectUtil.loadStaticR1(var2, "valueOf", false, var2, String.class);
      Object var4 = var3.call("NON_NULL");
      Functions.V1 var5 = ReflectUtil.bindInstanceV1(var0, "setSerializationInclusion", false, var2);
      var5.call(var4);
   }

   public String toJsonString(Object var1) {
      return (String)this.writeValueAsString.call(var1);
   }
}
