package drlugha.translator.shared.model;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

public class BaseEntityListener {

    @PrePersist
    public void onCreate(Object entity) {
        BaseEntity baseEntity = (BaseEntity) entity;
        baseEntity.setCreatedAt(new Date());

    }

    @PreUpdate
    public void onUpdate(Object entity) {
        BaseEntity baseEntity = (BaseEntity) entity;
        baseEntity.setUpdatedAt(new Date());

    }
}
