package com.epsagon.vendored.lambdainternal.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public final class ReflectUtil {
   public static Class loadClass(ClassLoader var0, String var1) {
      try {
         return Class.forName(var1, true, var0);
      } catch (LinkageError | ClassNotFoundException var3) {
         throw new ReflectException(var3);
      }
   }

   private static Object newInstance(Constructor var0, Object... var1) {
      try {
         return var0.newInstance(var1);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException var3) {
         throw new ReflectException(var3);
      }
   }

   public static Class getRawClass(Type var0) {
      if(var0 instanceof Class) {
         return (Class)var0;
      } else if(var0 instanceof ParameterizedType) {
         return getRawClass(((ParameterizedType)var0).getRawType());
      } else if(var0 instanceof GenericArrayType) {
         Class var1 = getRawClass(((GenericArrayType)var0).getGenericComponentType());
         return Array.newInstance(var1, 0).getClass();
      } else if(var0 instanceof TypeVariable) {
         throw new ReflectException("type variables not supported");
      } else {
         throw new ReflectException("unsupport type: " + var0.getClass().getName());
      }
   }

   public static Functions.R1 makeCaster(Type var0) {
      return makeCaster(getRawClass(var0));
   }

   private static Functions.R1 boxCaster(final Class var0) {
      return new Functions.R1() {
         public Object call(Object var1) {
            return var0.cast(var1);
         }
      };
   }

   public static Functions.R1 makeCaster(Class var0) {
      return Long.TYPE.equals(var0)?boxCaster(Long.class):(Double.TYPE.equals(var0)?boxCaster(Double.class):(Float.TYPE.equals(var0)?boxCaster(Float.class):(Integer.TYPE.equals(var0)?boxCaster(Integer.class):(Short.TYPE.equals(var0)?boxCaster(Short.class):(Character.TYPE.equals(var0)?boxCaster(Character.class):(Byte.TYPE.equals(var0)?boxCaster(Byte.class):(Boolean.TYPE.equals(var0)?boxCaster(Boolean.class):boxCaster(var0))))))));
   }

   private static Object invoke(Method var0, Object var1, Class var2, Object... var3) {
      Functions.R1 var4 = makeCaster(var2);

      try {
         Object var5 = var0.invoke(var1, var3);
         return var2.equals(Void.TYPE)?null:var4.call(var5);
      } catch (ExceptionInInitializerError | IllegalAccessException | InvocationTargetException var6) {
         throw new ReflectException(var6);
      }
   }

   private static Method lookupMethod(Class var0, String var1, Class... var2) {
      try {
         try {
            return var0.getDeclaredMethod(var1, var2);
         } catch (NoSuchMethodException var4) {
            return var0.getMethod(var1, var2);
         }
      } catch (SecurityException | NoSuchMethodException var5) {
         throw new ReflectException(var5);
      }
   }

   private static Method getDeclaredMethod(Class var0, String var1, boolean var2, boolean var3, Class var4, Class... var5) {
      Method var6 = lookupMethod(var0, var1, var5);
      if(!var4.equals(Void.TYPE) && !var4.isAssignableFrom(var6.getReturnType())) {
         throw new ReflectException("Class=" + var0.getName() + " method=" + var1 + " type " + var6.getReturnType().getName() + " not assignment-compatible with " + var4.getName());
      } else {
         int var7 = var6.getModifiers();
         if(Modifier.isStatic(var7) != var2) {
            throw new ReflectException("Class=" + var0.getName() + " method=" + var1 + " expected isStatic=" + var2);
         } else {
            if(var3) {
               var6.setAccessible(true);
            }

            return var6;
         }
      }
   }

   private static Constructor getDeclaredConstructor(Class var0, boolean var1, Class... var2) {
      Constructor var3;
      try {
         var3 = var0.getDeclaredConstructor(var2);
      } catch (SecurityException | NoSuchMethodException var5) {
         throw new ReflectException(var5);
      }

      if(var1) {
         var3.setAccessible(true);
      }

      return var3;
   }

   public static Functions.R1 loadInstanceR0(Class var0, String var1, boolean var2, final Class var3) {
      final Method var4 = getDeclaredMethod(var0, var1, false, var2, var3, new Class[0]);
      return new Functions.R1() {
         public Object call(Object var1) {
            return ReflectUtil.invoke(var4, var1, var3, new Object[0]);
         }
      };
   }

   public static Functions.R5 loadInstanceR4(Class var0, String var1, boolean var2, final Class var3, Class var4, Class var5, Class var6, Class var7) {
      final Method var8 = getDeclaredMethod(var0, var1, false, var2, var3, new Class[]{var4, var5, var6, var7});
      return new Functions.R5() {
         public Object call(Object var1, Object var2, Object var3x, Object var4, Object var5) {
            return ReflectUtil.invoke(var8, var1, var3, new Object[]{var2, var3x, var4, var5});
         }
      };
   }

   public static Functions.V2 loadInstanceV1(Class var0, String var1, boolean var2, Class var3) {
      final Method var4 = getDeclaredMethod(var0, var1, false, var2, Void.TYPE, new Class[]{var3});
      return new Functions.V2() {
         public void call(Object var1, Object var2) {
            ReflectUtil.invoke(var4, var1, Void.TYPE, new Object[]{var2});
         }
      };
   }

   public static Functions.R0 bindInstanceR0(final Object var0, String var1, boolean var2, final Class var3) {
      final Method var4 = getDeclaredMethod(var0.getClass(), var1, false, var2, var3, new Class[0]);
      return new Functions.R0() {
         public Object call() {
            return ReflectUtil.invoke(var4, var0, var3, new Object[0]);
         }
      };
   }

   public static Functions.R1 bindInstanceR1(final Object var0, String var1, boolean var2, final Class var3, Class var4) {
      final Method var5 = getDeclaredMethod(var0.getClass(), var1, false, var2, var3, new Class[]{var4});
      return new Functions.R1() {
         public Object call(Object var1) {
            return ReflectUtil.invoke(var5, var0, var3, new Object[]{var1});
         }
      };
   }

   public static Functions.V1 bindInstanceV1(final Object var0, String var1, boolean var2, Class var3) {
      final Method var4 = getDeclaredMethod(var0.getClass(), var1, false, var2, Void.TYPE, new Class[]{var3});
      return new Functions.V1() {
         public void call(Object var1) {
            ReflectUtil.invoke(var4, var0, Void.TYPE, new Object[]{var1});
         }
      };
   }

   public static Functions.V2 bindInstanceV2(final Object var0, String var1, boolean var2, Class var3, Class var4) {
      final Method var5 = getDeclaredMethod(var0.getClass(), var1, false, var2, Void.TYPE, new Class[]{var3, var4});
      return new Functions.V2() {
         public void call(Object var1, Object var2) {
            ReflectUtil.invoke(var5, var0, Void.TYPE, new Object[]{var1, var2});
         }
      };
   }

   public static Functions.R0 loadStaticR0(Class var0, String var1, boolean var2, final Class var3) {
      final Method var4 = getDeclaredMethod(var0, var1, true, var2, var3, new Class[0]);
      return new Functions.R0() {
         public Object call() {
            return ReflectUtil.invoke(var4, (Object)null, var3, new Object[0]);
         }
      };
   }

   public static Functions.R1 loadStaticR1(Class var0, String var1, boolean var2, final Class var3, Class var4) {
      final Method var5 = getDeclaredMethod(var0, var1, true, var2, var3, new Class[]{var4});
      return new Functions.R1() {
         public Object call(Object var1) {
            return ReflectUtil.invoke(var5, (Object)null, var3, new Object[]{var1});
         }
      };
   }

   public static Functions.V2 loadStaticV2(Class var0, String var1, boolean var2, Class var3, Class var4) {
      final Method var5 = getDeclaredMethod(var0, var1, true, var2, Void.TYPE, new Class[]{var3, var4});
      return new Functions.V2() {
         public void call(Object var1, Object var2) {
            ReflectUtil.invoke(var5, (Object)null, Void.TYPE, new Object[]{var1, var2});
         }
      };
   }

   public static Functions.R0 loadConstructor0(Class var0, boolean var1) {
      final Constructor var2 = getDeclaredConstructor(var0, var1, new Class[0]);
      return new Functions.R0() {
         public Object call() {
            return ReflectUtil.newInstance(var2, new Object[0]);
         }
      };
   }

   public static Functions.R1 loadConstructor1(Class var0, boolean var1, Class var2) {
      final Constructor var3 = getDeclaredConstructor(var0, var1, new Class[]{var2});
      return new Functions.R1() {
         public Object call(Object var1) {
            return ReflectUtil.newInstance(var3, new Object[]{var1});
         }
      };
   }

   public static Functions.R2 loadConstructor2(Class var0, boolean var1, Class var2, Class var3) {
      final Constructor var4 = getDeclaredConstructor(var0, var1, new Class[]{var2, var3});
      return new Functions.R2() {
         public Object call(Object var1, Object var2) {
            return ReflectUtil.newInstance(var4, new Object[]{var1, var2});
         }
      };
   }

   public static Object getStaticField(Class var0, String var1, Class var2) {
      Functions.R1 var3 = makeCaster(var2);

      try {
         return var3.call(var0.getField(var1).get((Object)null));
      } catch (SecurityException | IllegalAccessException | NoSuchFieldException var5) {
         throw new ReflectException(var5);
      }
   }

   public static void setStaticField(Class var0, String var1, boolean var2, Object var3) {
      try {
         Field var4 = var0.getDeclaredField(var1);
         if(var2) {
            var4.setAccessible(true);
         }

         var4.set((Object)null, var3);
      } catch (SecurityException | IllegalAccessException | NoSuchFieldException var5) {
         throw new ReflectException(var5);
      }
   }

   public static class ReflectException extends RuntimeException {
      private static final long serialVersionUID = 1L;

      public ReflectException() {
      }

      public ReflectException(String var1, Throwable var2, boolean var3, boolean var4) {
         super(var1, var2, var3, var4);
      }

      public ReflectException(String var1, Throwable var2) {
         super(var1, var2);
      }

      public ReflectException(String var1) {
         super(var1);
      }

      public ReflectException(Throwable var1) {
         super(var1);
      }
   }
}
