package com.epsagon.vendored.lambdainternal.util;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketImpl;
import sun.misc.Unsafe;

public final class UnsafeUtil {
   public static final Unsafe TheUnsafe;

   public static RuntimeException throwException(Throwable var0) {
      TheUnsafe.throwException(var0);
      throw new Error("should never get here");
   }

   public static FileDescriptor toFd(int var0) throws RuntimeException {
      try {
         Class var1 = FileDescriptor.class;
         Constructor var2 = var1.getDeclaredConstructor(new Class[]{Integer.TYPE});
         var2.setAccessible(true);
         return (FileDescriptor)var2.newInstance(new Object[]{new Integer(var0)});
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }

   public static void closeFd(FileDescriptor var0) throws IOException {
      (new FileOutputStream(var0)).close();
      if(var0.valid()) {
         System.err.println("File descriptor is still valid!");
      }

   }

   public static Socket toSocket(FileDescriptor var0) throws RuntimeException {
      try {
         Class var1 = Class.forName("java.net.PlainSocketImpl");
         Constructor var2 = var1.getDeclaredConstructor(new Class[]{FileDescriptor.class});
         var2.setAccessible(true);
         SocketImpl var3 = (SocketImpl)var2.newInstance(new Object[]{var0});
         var1 = Socket.class;
         var2 = var1.getDeclaredConstructor(new Class[]{SocketImpl.class});
         var2.setAccessible(true);
         Socket var4 = (Socket)var2.newInstance(new Object[]{var3});
         Field var5 = var1.getDeclaredField("created");
         var5.setAccessible(true);
         var5.setBoolean(var4, true);
         Field var6 = var1.getDeclaredField("connected");
         var6.setAccessible(true);
         var6.setBoolean(var4, true);
         Field var7 = var1.getDeclaredField("bound");
         var7.setAccessible(true);
         var7.setBoolean(var4, true);
         return var4;
      } catch (Exception var8) {
         throw new RuntimeException(var8);
      }
   }

   static {
      try {
         Field var0 = Unsafe.class.getDeclaredField("theUnsafe");
         var0.setAccessible(true);
         TheUnsafe = (Unsafe)var0.get((Object)null);
      } catch (Exception var1) {
         throw new Error("failed to load Unsafe", var1);
      }
   }
}
