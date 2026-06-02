package cn.ggsn.openrxlight.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

public interface BaseDao<T> extends PanacheRepository<T> {
}
