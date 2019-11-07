package com.epsagon.vendored.lambdainternal;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface PojoSerializerFactory {
   PojoSerializer getSerializer(Class var1);

   PojoSerializer getSerializer(Type var1);

   public interface PojoSerializer {
      Object fromJson(InputStream var1);

      Object fromJson(String var1);

      void toJson(Object var1, OutputStream var2);
   }
}
