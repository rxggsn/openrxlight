package cn.ggsn.rxlight.mapper;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;

import cn.ggsn.openrxlight.utils.JsonUtils;
import io.quarkus.hibernate.orm.JsonFormat;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import jakarta.enterprise.context.ApplicationScoped;

@PersistenceUnitExtension
@JsonFormat
@ApplicationScoped
public class JsonFormatMapper implements FormatMapper {

    @Override
    public <T> T fromString(CharSequence value, JavaType<T> javaType, WrapperOptions options) {
        return JsonUtils.fromJson(value.toString(), javaType.getJavaTypeClass());
    }

    @Override
    public <T> String toString(T value, JavaType<T> javaType, WrapperOptions options) {
        return JsonUtils.toJson(value);
    }

}
