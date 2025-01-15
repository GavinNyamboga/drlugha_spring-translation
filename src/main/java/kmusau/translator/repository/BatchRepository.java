package kmusau.translator.repository;

import kmusau.translator.entity.BatchEntity;
import kmusau.translator.enums.BatchType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchRepository extends JpaRepository<BatchEntity, Long> {
    public List<BatchEntity> findAllByBatchType(BatchType batchType);
}
