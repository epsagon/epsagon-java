package com.epsagon.vendored.lambdainternal.mixin;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public final class DateTimeModule extends SimpleModule {
   private static final long serialVersionUID = 1L;

   public DateTimeModule() {
      super(PackageVersion.VERSION);
      this.addSerializer(DateTime.class, new Serializer());
   }

   public static final class Deserializer extends JsonDeserializer {
      public DateTime deserialize(JsonParser var1, DeserializationContext var2) throws IOException {
         return DateTime.parse(var1.getValueAsString());
      }
   }

   public static final class Serializer extends JsonSerializer<DateTime> {
      private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

      public void serialize(DateTime var1, JsonGenerator var2, SerializerProvider var3) throws IOException, JsonProcessingException {
         var2.writeString(formatter.print(var1));
      }
   }
}
