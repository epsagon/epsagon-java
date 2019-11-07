package com.epsagon.vendored.lambdainternal;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Map;

import com.epsagon.vendored.lambdainternal.util.LambdaByteArrayOutputStream;
import com.epsagon.vendored.lambdainternal.util.LambdaOutputStream;
import com.epsagon.vendored.lambdainternal.util.ReflectUtil;
import com.epsagon.vendored.lambdainternal.api.LambdaContextLogger;

public class AWSLambda {
   private static final Runnable doNothing = new Runnable() {
      public void run() {
      }
   };
   public static URLClassLoader customerClassLoader;

   private static UserMethods findUserMethods(String var0, String var1, boolean var2, ClassLoader var3) {
      return var2?findUserMethodsDelayed(var0, var1, var3):findUserMethodsImmediate(var0, var1, var3);
   }

   private static UserMethods findUserMethodsDelayed(final String var0, final String var1, final ClassLoader var2) {
      Runnable var3 = doNothing;
      final LambdaRequestHandler[] var4 = new LambdaRequestHandler[1];
      LambdaRequestHandler var5 = new LambdaRequestHandler() {
         public LambdaByteArrayOutputStream call(LambdaRuntime.InvokeRequest var1x) throws Error, Exception {
            LambdaRequestHandler var2x = var4[0];
            if(var2x == null) {
               UserMethods var3 = AWSLambda.findUserMethodsImmediate(var0, var1, var2);
               var4[0] = var3.requestHandler;
               var3.initHandler.run();
               return var3.requestHandler.call(var1x);
            } else {
               return var2x.call(var1x);
            }
         }
      };
      return new UserMethods(var3, var5);
   }

   private static final LambdaRequestHandler initErrorHandler(Throwable var0, String var1) {
      return new LambdaRequestHandler.UserFaultHandler(new UserFault("Error loading class " + var1 + (var0.getMessage() == null?"":": " + var0.getMessage()), var0.getClass().toString(), UserFault.trace(var0)));
   }

   private static final LambdaRequestHandler classNotFound(Throwable var0, String var1) {
      return new LambdaRequestHandler.UserFaultHandler(new UserFault("Class not found: " + var1, var0.getClass().toString(), UserFault.trace(var0)));
   }

   private static UserMethods findUserMethodsImmediate(String var0, String var1, ClassLoader var2) {
      HandlerInfo var3;
      try {
         var3 = HandlerInfo.fromString(var0, var2);
      } catch (HandlerInfo.InvalidHandlerException var8) {
         return new UserMethods(doNothing, new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("Invalid handler: `" + var0 + "\'")));
      } catch (ClassNotFoundException var9) {
         return new UserMethods(doNothing, classNotFound(var9, HandlerInfo.className(var0)));
      } catch (NoClassDefFoundError | ExceptionInInitializerError var10) {
         return new UserMethods(doNothing, initErrorHandler(var10, HandlerInfo.className(var0)));
      } catch (Throwable var11) {
         return new UserMethods(doNothing, new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(var11)));
      }

      LambdaRequestHandler var4;
      if(var1.equals("event")) {
         var4 = EventHandlerLoader.loadEventHandler(var3);
      } else {
         if(!var1.equals("http")) {
            throw new RuntimeException("invalid mode specified: " + var1);
         }

         var4 = HttpHandlerLoader.loadHttpHandler(var3);
      }

      Runnable var5 = doNothing;

      try {
         var5 = wrapInitCall(var3.clazz.getMethod("init", new Class[0]));
      } catch (NoClassDefFoundError | NoSuchMethodException var7) {
         ;
      }

      return new UserMethods(var5, var4);
   }

   private static void addIfNotNull(Map var0, String var1, String var2) {
      if(var2 != null && !var2.isEmpty()) {
         var0.put(var1, var2);
         LambdaRuntime.setenv(var1, var2, 1);
      }

   }

   private static void setCredsEnv(LambdaRuntime.AWSCredentials var0, String var1) {
      try {
         Map var2 = System.getenv();
         Field var3 = var2.getClass().getDeclaredField("m");
         var3.setAccessible(true);
         Object var4 = var3.get(var2);
         Map var5 = (Map)var4;
         addIfNotNull(var5, "AWS_ACCESS_KEY_ID", var0.key);
         addIfNotNull(var5, "AWS_ACCESS_KEY", var0.key);
         addIfNotNull(var5, "AWS_SECRET_ACCESS_KEY", var0.secret);
         addIfNotNull(var5, "AWS_SECRET_KEY", var0.secret);
         addIfNotNull(var5, "AWS_SESSION_TOKEN", var0.session);
         var3.setAccessible(false);
      } catch (Exception var6) {
         throw new RuntimeException(var6);
      }
   }

   private static Runnable wrapInitCall(final Method var0) {
      return new Runnable() {
         public void run() {
            try {
               var0.invoke((Object)null, new Object[0]);
            } catch (Throwable var2) {
               throw UserFault.makeUserFault(var2);
            }
         }
      };
   }

   public static void setupRuntimeLogger() throws IllegalAccessException, NoSuchFieldException {
      ReflectUtil.setStaticField(com.amazonaws.services.lambda.runtime.LambdaRuntime.class, "logger", true, new LambdaContextLogger());
   }

   public static void startRuntime(ClassLoader var0) throws Throwable {
      try {
         System.loadLibrary("awslambda");
         LambdaRuntime.initRuntime();
      } catch (Throwable var28) {
         System.err.printf("Failed to load awslambda shared library: Library=%s Exception=%s message=%s%n", new Object[]{System.mapLibraryName("awslambda"), var28.getClass().getName(), var28.getMessage()});
         var28.printStackTrace(System.err);
         System.exit(-1);
      }

      System.setOut(new PrintStream(new LambdaOutputStream(System.out), false, "UTF-8"));
      System.setErr(new PrintStream(new LambdaOutputStream(System.err), false, "UTF-8"));
      String var1 = LambdaRTEntry.getEnvOrExit("AWS_REGION");
      String var2 = LambdaRTEntry.getEnvOrExit("LAMBDA_CONTROL_SOCKET");
      int var3 = Integer.parseInt(var2);
      LambdaRuntime.WaitForStartResult var4 = LambdaRuntime.waitForStart(var3);
      setCredsEnv(var4.credentials, var1);
      LambdaRuntime.reportRunning(var4.invokeid);
      String var5 = LambdaRTEntry.getEnvOrExit("LAMBDA_TASK_ROOT");
      customerClassLoader = LambdaRTEntry.makeClassLoader(var5, var0, true);
      setupRuntimeLogger();
      Thread.currentThread().setContextClassLoader(customerClassLoader);
      UserMethods var6 = findUserMethods(var4.handler, var4.mode, var4.suppressInit, customerClassLoader);

      try {
         var6.initHandler.run();
      } catch (UserFault var26) {
         LambdaRuntime.reportFault(var4.invokeid, var26.msg, var26.exception, var26.trace);
      } finally {
         LambdaRuntime.reportDone(var4.invokeid, (byte[])null, 0);
      }

      while(true) {
         LambdaRuntime.InvokeRequest var7 = LambdaRuntime.waitForInvoke();
         LambdaRuntime.needsDebugLogs = var7.needsDebugLogs;
         setCredsEnv(var7.credentials, (String)null);
         LambdaByteArrayOutputStream var8 = null;

         try {
            var8 = var6.requestHandler.call(var7);
         } catch (UserFault var24) {
            LambdaRuntime.reportFault(var7.invokeid, var24.msg, var24.exception, var24.trace);
         } catch (Exception | Error var25) {
            UserFault var10 = UserFault.makeUserFault((Throwable)var25);
            LambdaRuntime.reportFault(var7.invokeid, var10.msg, var10.exception, var10.trace);
         } finally {
            if(var8 == null) {
               LambdaRuntime.reportDone(var7.invokeid, (byte[])null, 0);
            } else {
               LambdaRuntime.reportDone(var7.invokeid, var8.getRawBuf(), var8.getValidByteCount());
            }

            LambdaRuntime.needsDebugLogs = false;
         }
      }
   }

   static {
      try {
         startRuntime(ClassLoader.getSystemClassLoader());
      } catch (Throwable var1) {
         throw new Error(var1);
      }
   }
}
