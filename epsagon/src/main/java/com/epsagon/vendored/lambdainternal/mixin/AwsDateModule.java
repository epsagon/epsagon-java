package com.epsagon.vendored.lambdainternal.mixin;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.Date;

public class AwsDateModule extends SimpleModule {
   private static final long serialVersionUID = 1L;

   private static double millisToSeconds(double var0) {
      return var0 / 1000.0D;
   }

   private static double secondsToMillis(double var0) {
      return var0 * 1000.0D;
   }

   public AwsDateModule() {
      super(PackageVersion.VERSION);
      this.addSerializer(Date.class, new Serializer());
      this.addDeserializer(Date.class, new Deserializer());
   }

   public static final class Deserializer extends JsonDeserializer {
      public Date deserialize(JsonParser var1, DeserializationContext var2) throws IOException {
         double var3 = var1.getValueAsDouble();
         return var3 == 0.0D?null:new Date((long)AwsDateModule.secondsToMillis(var3));
      }
   }

   public static final class Serializer extends JsonSerializer<Date> {
      public void serialize(Date var1, JsonGenerator var2, SerializerProvider var3) throws IOException {
         if(var1 != null) {
            var2.writeNumber(AwsDateModule.millisToSeconds((double)var1.getTime()));
         }

      }
   }
}
