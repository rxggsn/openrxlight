package cn.ggsn.openrxlight.domain;

import java.util.List;

import cn.ggsn.openrxlight.lang.Lists2;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.transaction.Transactional;

@MappedSuperclass
public abstract class BaseEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    public Long id;

    @Transactional
    public void save() {
        if (this.id == null || this.id == 0) {
            QuarkusTransaction.joiningExisting().run(() -> {
                this.persist();
            });
        } else {
            QuarkusTransaction.joiningExisting().run(() -> {
                getEntityManager().merge(this);
            });
        }
    }

    @Transactional
    public static <T extends BaseEntity> void batchSave(List<T> entities) {
        T.persist(Lists2.filter(entities, e -> e.id == null || e.id == 0));
    }
}
