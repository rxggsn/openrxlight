package cn.ggsn.openrxlight.ormx;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import cn.ggsn.openrxlight.utils.JsonUtils;

import com.fasterxml.jackson.databind.JavaType;
import org.postgresql.util.PGobject;
import org.hibernate.type.SqlTypes;

import java.io.*;
import java.sql.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class GenericJsonbType<T> implements UserType<T> {

    private final JavaType javaType;

    protected GenericJsonbType() {
        Type superClass = getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) superClass;
        Type[] typeArgs = parameterizedType.getActualTypeArguments();
        this.javaType = JsonUtils.OBJECT_MAPPER.getTypeFactory().constructType(typeArgs[0]);
    }

    @Override
    public int getSqlType() {
        return SqlTypes.JSON;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> returnedClass() {
        return (Class<T>) javaType.getRawClass();
    }

    @Override
    public T nullSafeGet(ResultSet rs, int position,
            SharedSessionContractImplementor session,
            Object owner) throws SQLException {
        String json = rs.getString(position);
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            return JsonUtils.fromJson(json, returnedClass());
        } catch (Exception e) {
            throw new SQLException("Failed to deserialize JSONB", e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, T value, int index,
            SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }

        try {
            String json = JsonUtils.toJson(value);
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(json);
            st.setObject(index, pgObject);
        } catch (Exception e) {
            throw new SQLException("Failed to serialize JSONB", e);
        }
    }

    @Override
    public T deepCopy(T value) {
        if (value == null) {
            return null;
        }

        try {
            String json = JsonUtils.toJson(value);
            return JsonUtils.fromJson(json, returnedClass());
        } catch (Exception e) {
            throw new RuntimeException("Failed to deep copy", e);
        }
    }

    @Override
    public boolean equals(T x, T y) {
        if (x == y)
            return true;
        if (x == null || y == null)
            return false;

        try {
            return JsonUtils.toJson(x)
                    .equals(JsonUtils.toJson(y));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode(T x) {
        if (x == null)
            return 0;
        return x.hashCode();
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(T value) {
        return (Serializable) deepCopy(value);
    }

    @Override
    public T assemble(Serializable cached, Object owner) {
        return deepCopy((T) cached);
    }

    @Override
    public T replace(T detached, T managed, Object owner) {
        return deepCopy(detached);
    }
}