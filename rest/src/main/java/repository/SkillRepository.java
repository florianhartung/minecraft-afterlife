package repository;

import data.SkillEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends CrudRepository<SkillEntity, Integer> {
    List<SkillEntity> findAllByUuid(String uuid);
}
