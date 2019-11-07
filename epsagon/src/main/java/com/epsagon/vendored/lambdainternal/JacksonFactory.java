package com.epsagon.vendored.lambdainternal;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

public class JacksonFactory implements PojoSerializerFactory {
   private static final ObjectMapper globalMapper = createObjectMapper();
   private static final JacksonFactory instance;
   private final ObjectMapper mapper;

   public static JacksonFactory getInstance() {
      return instance;
   }

   private JacksonFactory(ObjectMapper var1) {
      this.mapper = var1;
   }

   public ObjectMapper getMapper() {
      return this.mapper;
   }

   private static ObjectMapper createObjectMapper() {
      ObjectMapper var0 = new ObjectMapper(createJsonFactory());
      SerializationConfig var1 = var0.getSerializationConfig();
      var1 = var1.withFeatures(new SerializationFeature[]{SerializationFeature.FAIL_ON_SELF_REFERENCES, SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, SerializationFeature.WRAP_EXCEPTIONS});
      var1 = var1.withoutFeatures(new SerializationFeature[]{SerializationFeature.CLOSE_CLOSEABLE, SerializationFeature.EAGER_SERIALIZER_FETCH, SerializationFeature.FAIL_ON_EMPTY_BEANS, SerializationFeature.FLUSH_AFTER_WRITE_VALUE, SerializationFeature.INDENT_OUTPUT, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID, SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, SerializationFeature.WRAP_ROOT_VALUE});
      var0.setConfig(var1);
      DeserializationConfig var2 = var0.getDeserializationConfig();
      var2 = var2.withFeatures(new DeserializationFeature[]{DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, DeserializationFeature.WRAP_EXCEPTIONS});
      var2 = var2.withoutFeatures(new DeserializationFeature[]{DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES});
      var0.setConfig(var2);
      var0.setSerializationInclusion(Include.NON_NULL);
      var0.enable(new MapperFeature[]{MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS});
      var0.enable(new MapperFeature[]{MapperFeature.AUTO_DETECT_FIELDS});
      var0.enable(new MapperFeature[]{MapperFeature.AUTO_DETECT_GETTERS});
      var0.enable(new MapperFeature[]{MapperFeature.AUTO_DETECT_IS_GETTERS});
      var0.enable(new MapperFeature[]{MapperFeature.AUTO_DETECT_SETTERS});
      var0.enable(new MapperFeature[]{MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS});
      var0.enable(new MapperFeature[]{MapperFeature.USE_STD_BEAN_NAMING});
      var0.enable(new MapperFeature[]{MapperFeature.USE_ANNOTATIONS});
      var0.disable(new MapperFeature[]{MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES});
      var0.disable(new MapperFeature[]{MapperFeature.AUTO_DETECT_CREATORS});
      var0.disable(new MapperFeature[]{MapperFeature.INFER_PROPERTY_MUTATORS});
      var0.disable(new MapperFeature[]{MapperFeature.SORT_PROPERTIES_ALPHABETICALLY});
      var0.disable(new MapperFeature[]{MapperFeature.USE_GETTERS_AS_SETTERS});
      var0.disable(new MapperFeature[]{MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME});
      var0.disable(new MapperFeature[]{MapperFeature.USE_STATIC_TYPING});
      var0.disable(new MapperFeature[]{MapperFeature.REQUIRE_SETTERS_FOR_GETTERS});
      return var0;
   }

   private static JsonFactory createJsonFactory() {
      JsonFactory var0 = new JsonFactory();
      var0.enable(Feature.ALLOW_NON_NUMERIC_NUMBERS);
      var0.enable(Feature.ALLOW_NUMERIC_LEADING_ZEROS);
      var0.enable(Feature.ALLOW_SINGLE_QUOTES);
      var0.enable(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
      var0.enable(Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
      var0.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
      var0.disable(Feature.ALLOW_COMMENTS);
      var0.disable(Feature.ALLOW_YAML_COMMENTS);
      var0.disable(Feature.AUTO_CLOSE_SOURCE);
      var0.disable(Feature.STRICT_DUPLICATE_DETECTION);
      var0.enable(com.fasterxml.jackson.core.JsonGenerator.Feature.IGNORE_UNKNOWN);
      var0.enable(com.fasterxml.jackson.core.JsonGenerator.Feature.QUOTE_FIELD_NAMES);
      var0.enable(com.fasterxml.jackson.core.JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
      var0.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
      var0.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET);
      var0.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.ESCAPE_NON_ASCII);
      var0.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM);
      var0.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
      var0.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
      var0.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
      return var0;
   }

   public PojoSerializerFactory.PojoSerializer getSerializer(Class var1) {
      return new ClassSerializer(this.mapper, var1);
   }

   public PojoSerializerFactory.PojoSerializer getSerializer(Type var1) {
      return new TypeSerializer(this.mapper, var1);
   }

   public JacksonFactory withNamingStrategy(Class var1, PropertyNamingStrategy var2) {
      return new JacksonFactory(this.mapper.copy().setPropertyNamingStrategy(var2));
   }

   public JacksonFactory withMixin(Class var1, Class var2) {
      return new JacksonFactory(this.mapper.copy().addMixIn(var1, var2));
   }

   static {
      instance = new JacksonFactory(globalMapper);
   }

   private static final class ClassSerializer extends InternalSerializer {
      public ClassSerializer(ObjectMapper var1, Class var2) {
         super(var1.reader(var2), var1.writerFor(var2));
      }
   }

   public static final class TypeSerializer extends InternalSerializer {
      public TypeSerializer(ObjectMapper var1, JavaType var2) {
         super(var1.reader(var2), var1.writerFor(var2));
      }

      public TypeSerializer(ObjectMapper var1, Type var2) {
         this(var1, var1.constructType(var2));
      }
   }

   private static class InternalSerializer implements PojoSerializerFactory.PojoSerializer {
      private final ObjectReader reader;
      private final ObjectWriter writer;

      public InternalSerializer(ObjectReader var1, ObjectWriter var2) {
         this.reader = var1;
         this.writer = var2;
      }

      public Object fromJson(InputStream var1) {
         try {
            return this.reader.readValue(var1);
         } catch (IOException var3) {
            throw new UncheckedIOException(var3);
         }
      }

      public Object fromJson(String var1) {
         try {
            return this.reader.readValue(var1);
         } catch (IOException var3) {
            throw new UncheckedIOException(var3);
         }
      }

      public void toJson(Object var1, OutputStream var2) {
         try {
            this.writer.writeValue(var2, var1);
         } catch (IOException var4) {
            throw new UncheckedIOException(var4);
         }
      }
   }
}
