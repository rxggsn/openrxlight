package cn.ggsn.openrxlight.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.MonthDayDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.MonthDaySerializer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

public class XmlUtils {

    private static final XmlMapper XML_MAPPER = XmlMapper.xmlBuilder()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRAP_ROOT_VALUE, true)
            .configure(SerializationFeature.CLOSE_CLOSEABLE, true)
            .configure(ToXmlGenerator.Feature.UNWRAP_ROOT_OBJECT_NODE, true)
            .configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS, SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS)
            .defaultUseWrapper(false)
            .build();

    static {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        module.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ISO_LOCAL_TIME));
        module.addSerializer(MonthDay.class, new MonthDaySerializer(DateTimeFormatter.ofPattern("MM-dd")));
        module.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE));
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ISO_LOCAL_TIME));
        module.addDeserializer(MonthDay.class, new MonthDayDeserializer(DateTimeFormatter.ofPattern("MM-dd")));

        SimpleModule simpleModule = new SimpleModule();
        // simpleModule.setSerializerModifier(new BeanSerializerModifier());
        simpleModule.addDeserializer(String.class, new StdDeserializer<>(String.class) {
            @Override
            public String deserialize(JsonParser parser, DeserializationContext context)
                    throws IOException {
                String result = StringDeserializer.instance.deserialize(parser, context);
                if (StringUtils.isBlank(result)) {
                    return null;
                }
                return result;
            }
        });

        XML_MAPPER.registerModule(module);
        XML_MAPPER.registerModule(simpleModule);
        XML_MAPPER.registerModule(new Jdk8Module());
        XML_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        XML_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static <T> String toXml(T val, String charset) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            XMLStreamWriter writer = XML_MAPPER.getFactory().getXMLOutputFactory().createXMLStreamWriter(stream,
                    charset);
            writer.writeStartDocument();

            try {
                XML_MAPPER.writeValue(writer, val);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            writer.writeEndDocument();
            writer.flush();

            return stream.toString();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromXml(Reader stream, Class<T> tClass) {
        try {
            XMLStreamReader reader = XML_MAPPER.getFactory().getXMLInputFactory().createXMLStreamReader(stream);

            reader.next();
            T value = XML_MAPPER.readValue(reader, tClass);
            reader.close();

            return value;
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromXml(String data, Class<T> aClass) {
        return fromXml(new StringReader(data), aClass);
    }
}
