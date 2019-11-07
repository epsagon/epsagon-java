package com.epsagon.vendored.lambdainternal;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaRuntimeInternal;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.epsagon.vendored.lambdainternal.util.LambdaByteArrayOutputStream;
import com.epsagon.vendored.lambdainternal.util.NativeMemoryAsInputStream;
import com.epsagon.vendored.lambdainternal.util.ReflectUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.PascalCaseStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.epsagon.vendored.lambdainternal.api.LambdaClientContext;
import com.epsagon.vendored.lambdainternal.api.LambdaCognitoIdentity;
import com.epsagon.vendored.lambdainternal.api.LambdaContext;
import com.epsagon.vendored.lambdainternal.mixin.AwsDateModule;
import com.epsagon.vendored.lambdainternal.mixin.DateTimeModule;
import com.epsagon.vendored.lambdainternal.mixin.DynamodbEventMixin;
import com.epsagon.vendored.lambdainternal.mixin.KinesisEventMixin;
import com.epsagon.vendored.lambdainternal.mixin.SNSEventMixin;
import com.epsagon.vendored.lambdainternal.util.Functions;
import com.epsagon.vendored.lambdainternal.util.UnsafeUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public final class EventHandlerLoader {
   private static final byte[] _JsonNull = new byte[]{(byte)110, (byte)117, (byte)108, (byte)108};
   private static Functions.V2 mdcPutMethod = null;
   private static boolean checkedMDC = false;
   private static final EnumMap typeCache = new EnumMap(Platform.class);
   private static volatile PojoSerializerFactory.PojoSerializer contextSerializer;
   private static final Comparator methodPriority = new Comparator<Method>() {
      public int compare(Method var1, Method var2) {
         return Integer.compare(EventHandlerLoader.methodPriority(var1), EventHandlerLoader.methodPriority(var2));
      }
   };

   private static String convertStreamToString(InputStream var0) {
      Scanner var1 = (new Scanner(var0)).useDelimiter("\\A");
      return var1.hasNext()?var1.next():"";
   }

   public static PojoSerializerFactory.PojoSerializer getSerializer(Type type) {
      return getSerializer(Platform.UNKNOWN, type);
   }

   private static PojoSerializerFactory.PojoSerializer getSerializer(Platform var0, Type var1) {
      if(var1 instanceof Class) {
         Class var2 = (Class)var1;
         if(var2.getName().equals("com.amazonaws.services.s3.event.S3EventNotification")) {
            return new S3Serializer(var2);
         }

         if(var2.getName().equals("com.amazonaws.services.lambda.runtime.events.S3Event")) {
            return new LambdaS3Serializer(var2);
         }

         if(var2.getName().equals("com.amazonaws.services.lambda.runtime.events.KinesisEvent")) {
            SimpleModule var8 = new SimpleModule();
            var8.addDeserializer(Date.class, new AwsDateModule.Deserializer());
            var8.addSerializer(Date.class, new AwsDateModule.Serializer());
            JacksonFactory var9 = JacksonFactory.getInstance().withMixin(var2, KinesisEventMixin.class);
            var9.getMapper().registerModule(var8);
            return var9.getSerializer(var2);
         }

         Class var3;
         Class var4;
         SimpleModule var6;
         JacksonFactory var7;
         if(var2.getName().equals("com.amazonaws.services.lambda.runtime.events.SNSEvent")) {
            var3 = ReflectUtil.loadClass(var2.getClassLoader(), var2.getName() + "$SNSRecord");
            var4 = ReflectUtil.loadClass(var2.getClassLoader(), "org.joda.time.DateTime");
            CrossLoaderDateTimeHandler var10 = new CrossLoaderDateTimeHandler(var4);
            var6 = new SimpleModule();
            var6.addDeserializer(var4, var10.deserializer);
            var6.addSerializer(var4, var10.serializer);
            var7 = JacksonFactory.getInstance().withNamingStrategy(var2, new PascalCaseStrategy()).withMixin(var3, SNSEventMixin.SNSRecordMixin.class);
            var7.getMapper().registerModule(var6);
            return var7.getSerializer(var2);
         }

         if(var2.getName().equals("com.amazonaws.services.lambda.runtime.events.DynamodbEvent")) {
            var3 = ReflectUtil.loadClass(var2.getClassLoader(), "com.amazonaws.services.dynamodbv2.model.Record");
            var4 = ReflectUtil.loadClass(var2.getClassLoader(), "com.amazonaws.services.dynamodbv2.model.StreamRecord");
            Class var5 = ReflectUtil.loadClass(var2.getClassLoader(), "com.amazonaws.services.dynamodbv2.model.AttributeValue");
            var6 = new SimpleModule();
            var6.addDeserializer(Date.class, new AwsDateModule.Deserializer());
            var6.addSerializer(Date.class, new AwsDateModule.Serializer());
            var7 = JacksonFactory.getInstance().withMixin(var2, DynamodbEventMixin.class).withMixin(var3, DynamodbEventMixin.RecordMixin.class).withMixin(var4, DynamodbEventMixin.StreamRecordMixin.class).withMixin(var5, DynamodbEventMixin.AttributeValueMixIn.class);
            var7.getMapper().registerModule(var6);
            return var7.getSerializer(var2);
         }
      }

      return var0 == Platform.ANDROID?GsonFactory.getInstance().getSerializer(var1):JacksonFactory.getInstance().getSerializer(var1);
   }

   private static PojoSerializerFactory.PojoSerializer getSerializerCached(Platform var0, Type var1) {
      Object var2 = (Map)typeCache.get(var0);
      if(var2 == null) {
         var2 = new HashMap();
         typeCache.put(var0, var2);
      }

      PojoSerializerFactory.PojoSerializer var3 = (PojoSerializerFactory.PojoSerializer)((Map)var2).get(var1);
      if(var3 == null) {
         var3 = getSerializer(var0, var1);
         ((Map)var2).put(var1, var3);
      }

      return var3;
   }

   private static PojoSerializerFactory.PojoSerializer getContextSerializer() {
      if(contextSerializer == null) {
         contextSerializer = GsonFactory.getInstance().getSerializer(LambdaClientContext.class);
      }

      return contextSerializer;
   }

   private static Platform getPlatform(Context var0) {
      ClientContext var1 = var0.getClientContext();
      if(var1 == null) {
         return Platform.UNKNOWN;
      } else {
         Map var2 = var1.getEnvironment();
         if(var2 == null) {
            return Platform.UNKNOWN;
         } else {
            String var3 = (String)var2.get("platform");
            return var3 == null? Platform.UNKNOWN:("Android".equalsIgnoreCase(var3)? Platform.ANDROID:("iPhoneOS".equalsIgnoreCase(var3)? Platform.IOS: Platform.UNKNOWN));
         }
      }
   }

   private static boolean isVoid(Type var0) {
      return Void.TYPE.equals(var0) || var0 instanceof Class && Void.class.isAssignableFrom((Class)var0);
   }

   public static Constructor getConstructor(Class var0) throws Exception {
      try {
         Constructor var1 = var0.getConstructor(new Class[0]);
         return var1;
      } catch (NoSuchMethodException var3) {
         if(var0.getEnclosingClass() != null && !Modifier.isStatic(var0.getModifiers())) {
            throw new Exception("Class " + var0.getName() + " cannot be instantiated because it is a non-static inner class");
         } else {
            throw new Exception("Class " + var0.getName() + " has no public zero-argument constructor", var3);
         }
      }
   }

   public static Object newInstance(Constructor var0) {
      try {
         return var0.newInstance(new Object[0]);
      } catch (UserFault var2) {
         throw var2;
      } catch (InstantiationException | InvocationTargetException var3) {
         throw UnsafeUtil.throwException((Throwable)(var3.getCause() == null?var3:var3.getCause()));
      } catch (IllegalAccessException var4) {
         throw UnsafeUtil.throwException(var4);
      }
   }

   public static Type[] findInterfaceParameters(Class var0, Class var1) {
      LinkedList var2 = new LinkedList();
      var2.addFirst(new ClassContext(var0, (Type[])null));

      while(!var2.isEmpty()) {
         ClassContext var3 = (ClassContext)var2.removeLast();
         Type[] var4 = var3.clazz.getGenericInterfaces();
         Type[] var5 = var4;
         int var6 = var4.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Type var8 = var5[var7];
            if(var8 instanceof ParameterizedType) {
               ParameterizedType var9 = (ParameterizedType)var8;
               Type var10 = var9.getRawType();
               if(!(var10 instanceof Class)) {
                  System.err.println("raw type is not a class: " + var10);
               } else {
                  Class var11 = (Class)var10;
                  if(var1.isAssignableFrom(var11)) {
                     return (new ClassContext(var9, var3)).actualTypeArguments;
                  }

                  var2.addFirst(new ClassContext(var9, var3));
               }
            } else if(var8 instanceof Class) {
               var2.addFirst(new ClassContext((Class)var8, var3));
            } else {
               System.err.println("Unexpected type class " + var8.getClass().getName());
            }
         }

         Type var12 = var3.clazz.getGenericSuperclass();
         if(var12 instanceof ParameterizedType) {
            var2.addFirst(new ClassContext((ParameterizedType)var12, var3));
         } else if(var12 != null) {
            var2.addFirst(new ClassContext((Class)var12, var3));
         }
      }

      return null;
   }

   public static LambdaRequestHandler wrapRequestHandlerClass(Class var0) {
      Type[] var1 = findInterfaceParameters(var0, RequestHandler.class);
      if(var1 == null) {
         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("Class " + var0.getName() + " does not implement RequestHandler with concrete type parameters"));
      } else if(var1.length != 2) {
         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("Invalid class signature for RequestHandler. Expected two generic types, got " + var1.length));
      } else {
         Type[] var2 = var1;
         int var3 = var1.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Type var5 = var2[var4];
            if(var5 instanceof TypeVariable) {
               Type[] var6 = ((TypeVariable)var5).getBounds();
               boolean var7 = false;
               if(var6 != null) {
                  Type[] var8 = var6;
                  int var9 = var6.length;

                  for(int var10 = 0; var10 < var9; ++var10) {
                     Type var11 = var8[var10];
                     if(!Object.class.equals(var11)) {
                        var7 = true;
                        break;
                     }
                  }
               }

               if(!var7) {
                  return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("Class " + var0.getName() + " does not implement RequestHandler with concrete type parameters: parameter " + var5 + " has no upper bound."));
               }
            }
         }

         Type var13 = var1[0];
         Type var14 = var1[1];

         try {
            Constructor var15 = getConstructor(var0);
            return wrapPojoHandler((RequestHandler)newInstance(var15), var13, var14);
         } catch (Throwable var12) {
            return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(var12));
         }
      }
   }

   public static LambdaRequestHandler wrapRequestStreamHandlerClass(Class var0) {
      try {
         Constructor var1 = getConstructor(var0);
         return wrapRequestStreamHandler((RequestStreamHandler)newInstance(var1));
      } catch (Throwable var3) {
         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(var3));
      }
   }

   public static LambdaRequestHandler loadStreamingRequestHandler(Class var0) {
      return (LambdaRequestHandler)(RequestStreamHandler.class.isAssignableFrom(var0)?wrapRequestStreamHandlerClass(var0.asSubclass(RequestStreamHandler.class)):(RequestHandler.class.isAssignableFrom(var0)?wrapRequestHandlerClass(var0.asSubclass(RequestHandler.class)):new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("Class does not implement an appropriate handler interface: " + var0.getName()))));
   }

   public static LambdaRequestHandler loadEventHandler(HandlerInfo var0) {
      return var0.methodName == null?loadStreamingRequestHandler(var0.clazz):loadEventPojoHandler(var0);
   }

   private static Optional getOneLengthHandler(Class var0, Method var1, Type var2, Type var3) {
      return InputStream.class.equals(var2)?Optional.of(StreamMethodRequestHandler.makeRequestHandler(var0, var1, true, false, false)):(OutputStream.class.equals(var2)?Optional.of(StreamMethodRequestHandler.makeRequestHandler(var0, var1, false, true, false)):(Context.class.equals(var2)?Optional.of(PojoMethodRequestHandler.makeRequestHandler(var0, var1, (Type)null, var3, true)):Optional.of(PojoMethodRequestHandler.makeRequestHandler(var0, var1, var2, var3, false))));
   }

   private static Optional getTwoLengthHandler(Class var0, Method var1, Type var2, Type var3, Type var4) {
      if(OutputStream.class.equals(var2)) {
         if(Context.class.equals(var3)) {
            return Optional.of(StreamMethodRequestHandler.makeRequestHandler(var0, var1, false, true, true));
         } else {
            System.err.println("Ignoring two-argument overload because first argument type is OutputStream and second argument type is not Context");
            return Optional.empty();
         }
      } else if(Context.class.equals(var2)) {
         System.err.println("Ignoring two-argument overload because first argument type is Context");
         return Optional.empty();
      } else if(InputStream.class.equals(var2)) {
         if(OutputStream.class.equals(var3)) {
            return Optional.of(StreamMethodRequestHandler.makeRequestHandler(var0, var1, true, true, false));
         } else if(Context.class.equals(var3)) {
            return Optional.of(StreamMethodRequestHandler.makeRequestHandler(var0, var1, true, false, true));
         } else {
            System.err.println("Ignoring two-argument overload because second parameter type, " + ReflectUtil.getRawClass(var3).getName() + ", is not OutputStream.");
            return Optional.empty();
         }
      } else if(Context.class.equals(var3)) {
         return Optional.of(PojoMethodRequestHandler.makeRequestHandler(var0, var1, var2, var4, true));
      } else {
         System.err.println("Ignoring two-argument overload because second parameter type is not Context");
         return Optional.empty();
      }
   }

   private static Optional getThreeLengthHandler(Class var0, Method var1, Type var2, Type var3, Type var4, Type var5) {
      if(InputStream.class.equals(var2) && OutputStream.class.equals(var3) && Context.class.equals(var4)) {
         return Optional.of(StreamMethodRequestHandler.makeRequestHandler(var0, var1, true, true, true));
      } else {
         System.err.println("Ignoring three-argument overload because argument signature is not (InputStream, OutputStream, Context");
         return Optional.empty();
      }
   }

   private static Optional getHandlerFromOverload(Class var0, Method var1) {
      Type var2 = var1.getGenericReturnType();
      Type[] var3 = var1.getGenericParameterTypes();
      if(var3.length == 0) {
         return Optional.of(PojoMethodRequestHandler.makeRequestHandler(var0, var1, (Type)null, var2, false));
      } else if(var3.length == 1) {
         return getOneLengthHandler(var0, var1, var3[0], var2);
      } else if(var3.length == 2) {
         return getTwoLengthHandler(var0, var1, var3[0], var3[1], var2);
      } else if(var3.length == 3) {
         return getThreeLengthHandler(var0, var1, var3[0], var3[1], var3[2], var2);
      } else {
         System.err.println("Ignoring an overload of method " + var1.getName() + " because it has too many parameters: Expected at most 3, got " + var3.length);
         return Optional.empty();
      }
   }

   private static final int methodPriority(Method var0) {
      Class[] var1 = var0.getParameterTypes();
      int var2 = var1.length;
      if(var2 > 0 && Context.class.equals(var1[var2 - 1])) {
         ++var2;
      }

      if(!var0.isBridge()) {
         var2 += 1000;
      }

      return -var2;
   }

   private static LambdaRequestHandler loadEventPojoHandler(HandlerInfo var0) {
      Method[] var1;
      try {
         var1 = var0.clazz.getMethods();
      } catch (NoClassDefFoundError var7) {
         return new LambdaRequestHandler.UserFaultHandler(new UserFault("Error loading method " + var0.methodName + " on class " + var0.clazz.getName(), var7.getClass().toString(), UserFault.trace(var7)));
      }

      if(var1.length == 0) {
         String var8 = "Class " + var0.getClass().getName() + " has no public method named " + var0.methodName;
         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(var8));
      } else {
         int var2 = 0;

         int var3;
         for(var3 = 0; var3 < var1.length; ++var3) {
            Method var4 = var1[var3];
            var1[var3 - var2] = var4;
            if(!var4.getName().equals(var0.methodName)) {
               ++var2;
            }
         }

         var3 = var1.length - var2;
         Arrays.sort(var1, 0, var3, methodPriority);

         for(int var9 = 0; var9 < var3; ++var9) {
            Method var5 = var1[var9];
            Optional var6 = getHandlerFromOverload(var0.clazz, var5);
            if(var6.isPresent()) {
               return (LambdaRequestHandler)var6.get();
            }
         }

         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("No public method named " + var0.methodName + " with appropriate method signature found on class " + var0.clazz));
      }
   }

   public static LambdaRequestHandler wrapPojoHandler(RequestHandler var0, Type var1, Type var2) {
      return wrapRequestStreamHandler((RequestStreamHandler)(new PojoHandlerAsStreamHandler(var0, Optional.ofNullable(var1), isVoid(var2)?Optional.empty():Optional.of(var2))));
   }

   public static String exceptionToString(Throwable var0) {
      StringWriter var1 = new StringWriter(65536);
      PrintWriter var2 = new PrintWriter(var1);
      Throwable var3 = null;

      try {
         var0.printStackTrace(var2);
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if(var2 != null) {
            if(var3 != null) {
               try {
                  var2.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               var2.close();
            }
         }

      }

      StringBuffer var14 = var1.getBuffer();
      if(var14.length() > 262144) {
         var14.delete(262144, var14.length());
         var14.append(" Truncated by Lambda");
      }

      return var14.toString();
   }

   public static LambdaRequestHandler wrapRequestStreamHandler(Constructor var0) {
      return wrapRequestStreamHandler((RequestStreamHandler)newInstance(var0));
   }

   public static LambdaRequestHandler wrapRequestStreamHandler(final RequestStreamHandler var0) {
      return new LambdaRequestHandler() {
         private final LambdaByteArrayOutputStream output = new LambdaByteArrayOutputStream(1024);

         public LambdaByteArrayOutputStream call(LambdaRuntime.InvokeRequest var1) throws Error, Exception {
            if(var1.sockfd >= 0) {
               throw new UserFault("Invalid args - eventbody = " + var1.eventBodyAddr + " socket =" + var1.sockfd, (String)null, (String)null);
            } else {
               this.output.reset();
               LambdaCognitoIdentity var2 = new LambdaCognitoIdentity(var1.cognitoIdentityId, var1.cognitoPoolId);
               LambdaClientContext var3 = null;
               if(var1.clientContext != null && var1.clientContext.length() > 0) {
                  try {
                     var3 = (LambdaClientContext)EventHandlerLoader.getContextSerializer().fromJson(var1.clientContext);
                  } catch (Throwable var16) {
                     UserFault.filterStackTrace(var16);
                     UserFault var5 = UserFault.makeUserFault("Error parsing Client Context as JSON");
                     LambdaRuntime.reportFault(var1.invokeid, var5.msg, var5.exception, var5.trace);
                     Failure var6 = new Failure(var16);
                     GsonFactory.getInstance().getSerializer(Failure.class).toJson(var6, this.output);
                     return this.output;
                  }
               }

               LambdaContext var4 = new LambdaContext(LambdaRuntime.MEMORY_LIMIT, var1.invokeid, LambdaRuntime.LOG_GROUP_NAME, LambdaRuntime.LOG_STREAM_NAME, LambdaRuntime.FUNCTION_NAME, var2, LambdaRuntime.FUNCTION_VERSION, var1.invokedFunctionArn, var3);
               if(!EventHandlerLoader.checkedMDC && LambdaRuntimeInternal.getUseLog4jAppender()) {
                  try {
                     Class var17 = ReflectUtil.loadClass(AWSLambda.customerClassLoader, "org.apache.log4j.MDC");
                     EventHandlerLoader.mdcPutMethod = ReflectUtil.loadStaticV2(var17, "put", false, String.class, Object.class);
                  } catch (Exception var14) {
                     System.err.println("Unable to load the log4j\'s MDC class, customer cannot see RequestId from MDC");
                  } finally {
                     EventHandlerLoader.checkedMDC = true;
                  }
               }

               if(EventHandlerLoader.mdcPutMethod != null) {
                  EventHandlerLoader.mdcPutMethod.call("AWSRequestId", var1.invokeid);
               }

               NativeMemoryAsInputStream var18 = new NativeMemoryAsInputStream(var1.eventBodyAddr, var1.eventBodyAddr + (long)var1.eventBodyLen);

               try {
                  var0.handleRequest(var18, this.output, var4);
               } catch (Throwable var13) {
                  UserFault.filterStackTrace(var13);
                  UserFault var7 = UserFault.makeUserFault(var13);
                  LambdaRuntime.reportFault(var1.invokeid, var7.msg, var7.exception, var7.trace);
                  this.output.reset();
                  Failure var8 = new Failure(var13);
                  GsonFactory.getInstance().getSerializer(Failure.class).toJson(var8, this.output);
               }

               return this.output;
            }
         }
      };
   }

   private static final class ClassContext {
      public final Class clazz;
      public final Type[] actualTypeArguments;
      private TypeVariable[] typeParameters;

      public ClassContext(Class var1, Type[] var2) {
         this.clazz = var1;
         this.actualTypeArguments = var2;
      }

      public ClassContext(Class var1, ClassContext var2) {
         this.typeParameters = var1.getTypeParameters();
         if(this.typeParameters.length != 0 && var2.actualTypeArguments != null) {
            Type[] var3 = new Type[this.typeParameters.length];

            for(int var4 = 0; var4 < var3.length; ++var4) {
               var3[var4] = var2.resolveTypeVariable(this.typeParameters[var4]);
            }

            this.clazz = var1;
            this.actualTypeArguments = var3;
         } else {
            this.clazz = var1;
            this.actualTypeArguments = null;
         }

      }

      public ClassContext(ParameterizedType var1, ClassContext var2) {
         Type[] var3 = var1.getActualTypeArguments();

         for(int var4 = 0; var4 < var3.length; ++var4) {
            Type var5 = var3[var4];
            if(var5 instanceof TypeVariable) {
               var3[var4] = var2.resolveTypeVariable((TypeVariable)var5);
            }
         }

         Type var6 = var1.getRawType();
         if(var6 instanceof Class) {
            this.clazz = (Class)var6;
         } else {
            if(!(var6 instanceof TypeVariable)) {
               throw new RuntimeException("Type " + var6 + " is of unexpected type " + var6.getClass());
            }

            this.clazz = (Class)((TypeVariable)var6).getGenericDeclaration();
         }

         this.actualTypeArguments = var3;
      }

      public Type resolveTypeVariable(TypeVariable var1) {
         TypeVariable[] var2 = this.getTypeParameters();

         for(int var3 = 0; var3 < var2.length; ++var3) {
            if(var1.getName().equals(var2[var3].getName())) {
               return (Type)(this.actualTypeArguments == null?var2[var3]:this.actualTypeArguments[var3]);
            }
         }

         return var1;
      }

      private TypeVariable[] getTypeParameters() {
         if(this.typeParameters == null) {
            this.typeParameters = this.clazz.getTypeParameters();
         }

         return this.typeParameters;
      }
   }

   private static final class StreamMethodRequestHandler implements RequestStreamHandler {
      public final Method m;
      public final Object instance;
      public final boolean needsInput;
      public final boolean needsOutput;
      public final boolean needsContext;
      public final int argSize;

      public StreamMethodRequestHandler(Method var1, Object var2, boolean var3, boolean var4, boolean var5) {
         this.m = var1;
         this.instance = var2;
         this.needsInput = var3;
         this.needsOutput = var4;
         this.needsContext = var5;
         this.argSize = (var3?1:0) + (var4?1:0) + (var5?1:0);
      }

      public static StreamMethodRequestHandler fromMethod(Class var0, Method var1, boolean var2, boolean var3, boolean var4) throws Exception {
         if(!EventHandlerLoader.isVoid(var1.getReturnType())) {
            System.err.println("Will ignore return type " + var1.getReturnType() + " on byte stream handler");
         }

         Object var5 = Modifier.isStatic(var1.getModifiers())?null:EventHandlerLoader.newInstance(EventHandlerLoader.getConstructor(var0));
         return new StreamMethodRequestHandler(var1, var5, var2, var3, var4);
      }

      public static LambdaRequestHandler makeRequestHandler(Class var0, Method var1, boolean var2, boolean var3, boolean var4) {
         try {
            return EventHandlerLoader.wrapRequestStreamHandler((RequestStreamHandler)fromMethod(var0, var1, var2, var3, var4));
         } catch (Throwable var6) {
            return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(var6));
         }
      }

      public void handleRequest(InputStream var1, OutputStream var2, Context var3) throws IOException {
         Object[] var4 = new Object[this.argSize];
         int var5 = 0;
         if(this.needsInput) {
            var4[var5++] = var1;
         } else {
            var1.close();
         }

         if(this.needsOutput) {
            var4[var5++] = var2;
         }

         if(this.needsContext) {
            var4[var5++] = var3;
         }

         try {
            this.m.invoke(this.instance, var4);
            if(!this.needsOutput) {
               var2.write(EventHandlerLoader._JsonNull);
            }

         } catch (InvocationTargetException var7) {
            if(var7.getCause() != null) {
               throw UnsafeUtil.throwException(UserFault.filterStackTrace(var7.getCause()));
            } else {
               throw UnsafeUtil.throwException(UserFault.filterStackTrace(var7));
            }
         } catch (Throwable var8) {
            throw UnsafeUtil.throwException(UserFault.filterStackTrace(var8));
         }
      }
   }

   private static final class PojoMethodRequestHandler implements RequestHandler {
      public final Method m;
      public final Type pType;
      public final Object instance;
      public final boolean needsContext;
      public final int argSize;

      public PojoMethodRequestHandler(Method var1, Type var2, Type var3, Object var4, boolean var5) {
         this.m = var1;
         this.pType = var2;
         this.instance = var4;
         this.needsContext = var5;
         this.argSize = (var5?1:0) + (var2 != null?1:0);
      }

      public static PojoMethodRequestHandler fromMethod(Class var0, Method var1, Type var2, Type var3, boolean var4) throws Exception {
         Object var5;
         if(Modifier.isStatic(var1.getModifiers())) {
            var5 = null;
         } else {
            var5 = EventHandlerLoader.newInstance(EventHandlerLoader.getConstructor(var0));
         }

         return new PojoMethodRequestHandler(var1, var2, var3, var5, var4);
      }

      public static LambdaRequestHandler makeRequestHandler(Class var0, Method var1, Type var2, Type var3, boolean var4) {
         try {
            return EventHandlerLoader.wrapPojoHandler(fromMethod(var0, var1, var2, var3, var4), var2, var3);
         } catch (Throwable var6) {
            return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(var6));
         }
      }

      public Object handleRequest(Object var1, Context var2) {
         Object[] var3 = new Object[this.argSize];
         int var4 = 0;
         if(this.pType != null) {
            var3[var4++] = var1;
         }

         if(this.needsContext) {
            var3[var4++] = var2;
         }

         try {
            return this.m.invoke(this.instance, var3);
         } catch (InvocationTargetException var6) {
            if(var6.getCause() != null) {
               throw UnsafeUtil.throwException(UserFault.filterStackTrace(var6.getCause()));
            } else {
               throw UnsafeUtil.throwException(UserFault.filterStackTrace(var6));
            }
         } catch (Throwable var7) {
            throw UnsafeUtil.throwException(UserFault.filterStackTrace(var7));
         }
      }
   }

   private static final class PojoHandlerAsStreamHandler implements RequestStreamHandler {
      public RequestHandler innerHandler;
      public final Optional inputType;
      public final Optional outputType;

      public PojoHandlerAsStreamHandler(RequestHandler var1, Optional var2, Optional var3) {
         this.innerHandler = var1;
         this.inputType = var2;
         this.outputType = var3;
         if(var2.isPresent()) {
            EventHandlerLoader.getSerializerCached(Platform.UNKNOWN, (Type)var2.get());
         }

         if(var3.isPresent()) {
            EventHandlerLoader.getSerializerCached(Platform.UNKNOWN, (Type)var3.get());
         }

      }

      public void handleRequest(InputStream var1, OutputStream var2, Context var3) throws IOException {
         Platform var5 = EventHandlerLoader.getPlatform(var3);

         Object var4;
         try {
            if(this.inputType.isPresent()) {
               var4 = EventHandlerLoader.getSerializerCached(var5, (Type)this.inputType.get()).fromJson(var1);
            } else {
               var4 = null;
            }
         } catch (Throwable var10) {
            throw new RuntimeException("An error occurred during JSON parsing", UserFault.filterStackTrace(var10));
         }

         Object var6;
         try {
            var6 = this.innerHandler.handleRequest(var4, var3);
         } catch (Throwable var9) {
            throw UnsafeUtil.throwException(UserFault.filterStackTrace(var9));
         }

         try {
            if(this.outputType.isPresent()) {
               PojoSerializerFactory.PojoSerializer var7 = EventHandlerLoader.getSerializerCached(var5, (Type)this.outputType.get());
               var7.toJson(var6, var2);
            } else {
               var2.write(EventHandlerLoader._JsonNull);
            }

         } catch (Throwable var8) {
            throw new RuntimeException("An error occurred during JSON serialization of response", var8);
         }
      }
   }

   private static final class LambdaS3Serializer implements PojoSerializerFactory.PojoSerializer {
      private final S3Serializer inner;
      private final Functions.R1 getRecords;
      private final Functions.R1 constructor;

      public LambdaS3Serializer(Class var1) {
         this.inner = new S3Serializer(var1.getSuperclass());
         this.constructor = ReflectUtil.loadConstructor1(var1, false, List.class);
         this.getRecords = ReflectUtil.loadInstanceR0(var1.getSuperclass(), "getRecords", false, List.class);
      }

      private Object convert(Object var1) {
         return this.constructor.call(this.getRecords.call(var1));
      }

      public Object fromJson(InputStream var1) {
         Object var2 = this.inner.fromJson(var1);
         return this.convert(var2);
      }

      public Object fromJson(String var1) {
         Object var2 = this.inner.fromJson(var1);
         return this.convert(var2);
      }

      public void toJson(Object var1, OutputStream var2) {
         this.inner.toJson(var1, var2);
      }
   }

   private static final class S3Serializer implements PojoSerializerFactory.PojoSerializer {
      private final Optional awsJackson;
      private final Functions.R1 s3Deserializer;
      private final Class clazz;

      public S3Serializer(Class var1) {
         this.s3Deserializer = ReflectUtil.loadStaticR1(var1, "parseJson", false, var1, String.class);
         this.awsJackson = getJackson(var1.getClassLoader());
         this.clazz = var1;
      }

      private static Optional getJackson(ClassLoader var0) {
         try {
            return Optional.of(AwsJackson.getCached(var0));
         } catch (Throwable var2) {
            System.err.println("Error loading Jackson from Java SDK: ");
            var2.printStackTrace();
            return Optional.empty();
         }
      }

      public Object fromJson(InputStream var1) {
         String var2 = EventHandlerLoader.convertStreamToString(var1);
         return this.fromJson(var2);
      }

      public Object fromJson(String var1) {
         return this.s3Deserializer.call(var1);
      }

      public void toJson(Object var1, OutputStream var2) {
         if(!this.awsJackson.isPresent()) {
            EventHandlerLoader.getSerializer(Platform.UNKNOWN, this.clazz).toJson(var1, var2);
         }

         String var3 = ((AwsJackson)this.awsJackson.get()).toJsonString(var1);

         try {
            OutputStreamWriter var4 = new OutputStreamWriter(var2);
            Throwable var5 = null;

            try {
               var4.write(var3);
            } catch (Throwable var15) {
               var5 = var15;
               throw var15;
            } finally {
               if(var4 != null) {
                  if(var5 != null) {
                     try {
                        var4.close();
                     } catch (Throwable var14) {
                        var5.addSuppressed(var14);
                     }
                  } else {
                     var4.close();
                  }
               }

            }

         } catch (IOException var17) {
            throw new UncheckedIOException(var17);
         }
      }
   }

   public static final class CrossLoaderDateTimeHandler {
      private final JsonDeserializer deserializer;
      private final JsonSerializer serializer;

      public CrossLoaderDateTimeHandler(Class var1) {
         Class var2 = ReflectUtil.loadClass(var1.getClassLoader(), "org.joda.time.DateTimeZone");
         final Object var3 = ReflectUtil.getStaticField(var2, "UTC", Object.class);
         final Functions.R2 var4 = ReflectUtil.loadConstructor2(var1, false, Long.TYPE, var2);
         final Functions.R1 var5 = ReflectUtil.loadInstanceR0(var1, "getMillis", false, Long.TYPE);
         this.deserializer = new JsonDeserializer() {
            private final DateTimeModule.Deserializer inner = new DateTimeModule.Deserializer();

            public Object deserialize(JsonParser var1, DeserializationContext var2) throws IOException {
               DateTime var3x = this.inner.deserialize(var1, var2);
               return var4.call(Long.valueOf(var3x.getMillis()), var3);
            }
         };
         this.serializer = new JsonSerializer() {
            private final DateTimeModule.Serializer inner = new DateTimeModule.Serializer();

            public void serialize(Object var1, JsonGenerator var2, SerializerProvider var3) throws IOException, JsonProcessingException {
               long var4 = ((Long)var5.call(var1)).longValue();
               this.inner.serialize(new DateTime(var4, DateTimeZone.UTC), var2, var3);
            }
         };
      }
   }

   private static final class Failure {
      private final String errorMessage;
      private final String errorType;
      private final String[] stackTrace;
      private final Failure cause;

      public Failure(Throwable var1) {
         this.errorMessage = var1.getLocalizedMessage() == null?var1.getClass().getName():var1.getLocalizedMessage();
         this.errorType = var1.getClass().getName();
         StackTraceElement[] var2 = var1.getStackTrace();
         this.stackTrace = new String[var2.length];

         for(int var3 = 0; var3 < var2.length; ++var3) {
            this.stackTrace[var3] = var2[var3].toString();
         }

         Throwable var4 = var1.getCause();
         this.cause = var4 == null?null:new Failure(var4);
      }
   }

   private static enum Platform {
      ANDROID,
      IOS,
      UNKNOWN;
   }
}
