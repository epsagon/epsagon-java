package com.epsagon.vendored.lambdainternal;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

import com.epsagon.vendored.lambdainternal.util.LambdaByteArrayOutputStream;
import com.epsagon.vendored.lambdainternal.util.UnsafeUtil;

public final class HttpHandlerLoader {
   private static LambdaRequestHandler wrapHttpHandler(HandlerInfo var0) {
      try {
         return wrapHttpHandler(var0.clazz.getMethod(var0.methodName, new Class[]{InputStream.class, OutputStream.class}));
      } catch (NoSuchMethodException var2) {
         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("No method named " + var0.methodName + " with appropriate method signature found on class " + var0.clazz.getName()));
      }
   }

   private static LambdaRequestHandler wrapHttpHandler(final Method var0) {
      return new LambdaRequestHandler() {
         public LambdaByteArrayOutputStream call(LambdaRuntime.InvokeRequest var1) throws Error, Exception {
            if(var1.sockfd < 0) {
               throw new UserFault("Socket cannot be negative -" + var1.sockfd, (String)null, (String)null);
            } else {
               Socket var2 = null;
               FileDescriptor var3 = UnsafeUtil.toFd(var1.sockfd);

               Object var4;
               try {
                  var2 = UnsafeUtil.toSocket(var3);

                  try {
                     var0.invoke((Object)null, new Object[]{var2.getInputStream(), var2.getOutputStream()});
                  } catch (Throwable var8) {
                     throw UserFault.makeUserFault(UserFault.filterStackTrace(var8));
                  }

                  var4 = null;
               } finally {
                  if(var2 != null) {
                     var2.close();
                  } else if(var3 != null) {
                     UnsafeUtil.closeFd(var3);
                  }

               }

               return (LambdaByteArrayOutputStream)var4;
            }
         }
      };
   }

   public static LambdaRequestHandler loadHttpHandler(HandlerInfo var0) {
      return (LambdaRequestHandler)(var0.methodName == null?new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("Invalid handler " + var0.clazz.getName() + ": class and method name should be separated by a double colon (::)")):wrapHttpHandler(var0));
   }
}
