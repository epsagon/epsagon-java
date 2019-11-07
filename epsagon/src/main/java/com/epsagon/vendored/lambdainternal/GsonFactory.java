package com.epsagon.vendored.lambdainternal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

public class GsonFactory implements PojoSerializerFactory {
   private static final Gson gson = (new GsonBuilder()).disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
   private static final GsonFactory instance = new GsonFactory();

   public static GsonFactory getInstance() {
      return instance;
   }

   public PojoSerializerFactory.PojoSerializer getSerializer(Class var1) {
      return InternalSerializer.create(var1);
   }

   public PojoSerializerFactory.PojoSerializer getSerializer(Type var1) {
      return InternalSerializer.create(var1);
   }

   private static class InternalSerializer implements PojoSerializerFactory.PojoSerializer {
      private final TypeAdapter adapter;

      public InternalSerializer(TypeAdapter var1) {
         this.adapter = var1.nullSafe();
      }

      public static InternalSerializer create(TypeToken var0) {
         return Void.TYPE.equals(var0.getRawType())?new InternalSerializer(GsonFactory.gson.getAdapter(Object.class)):new InternalSerializer(GsonFactory.gson.getAdapter(var0));
      }

      public static InternalSerializer create(Class var0) {
         return create(TypeToken.get(var0));
      }

      public static InternalSerializer create(Type var0) {
         return create(TypeToken.get(var0));
      }

      private Object fromJson(JsonReader var1) {
         var1.setLenient(true);

         try {
            try {
               var1.peek();
            } catch (EOFException var3) {
               return null;
            }

            return this.adapter.read(var1);
         } catch (IOException var4) {
            throw new UncheckedIOException(var4);
         }
      }

      public Object fromJson(InputStream var1) {
         try {
            JsonReader var2 = new JsonReader(new InputStreamReader(var1));
            Throwable var3 = null;

            Object var4;
            try {
               var4 = this.fromJson(var2);
            } catch (Throwable var14) {
               var3 = var14;
               throw var14;
            } finally {
               if(var2 != null) {
                  if(var3 != null) {
                     try {
                        var2.close();
                     } catch (Throwable var13) {
                        var3.addSuppressed(var13);
                     }
                  } else {
                     var2.close();
                  }
               }

            }

            return var4;
         } catch (IOException var16) {
            throw new UncheckedIOException(var16);
         }
      }

      public Object fromJson(String var1) {
         try {
            JsonReader var2 = new JsonReader(new StringReader(var1));
            Throwable var3 = null;

            Object var4;
            try {
               var4 = this.fromJson(var2);
            } catch (Throwable var14) {
               var3 = var14;
               throw var14;
            } finally {
               if(var2 != null) {
                  if(var3 != null) {
                     try {
                        var2.close();
                     } catch (Throwable var13) {
                        var3.addSuppressed(var13);
                     }
                  } else {
                     var2.close();
                  }
               }

            }

            return var4;
         } catch (IOException var16) {
            throw new UncheckedIOException(var16);
         }
      }

      public void toJson(Object var1, OutputStream var2) {
         try {
            JsonWriter var3 = new JsonWriter(new OutputStreamWriter(var2));
            Throwable var4 = null;

            try {
               var3.setLenient(true);
               var3.setSerializeNulls(false);
               var3.setHtmlSafe(false);
               this.adapter.write(var3, var1);
            } catch (Throwable var14) {
               var4 = var14;
               throw var14;
            } finally {
               if(var3 != null) {
                  if(var4 != null) {
                     try {
                        var3.close();
                     } catch (Throwable var13) {
                        var4.addSuppressed(var13);
                     }
                  } else {
                     var3.close();
                  }
               }

            }

         } catch (IOException var16) {
            throw new UncheckedIOException(var16);
         }
      }
   }
}
