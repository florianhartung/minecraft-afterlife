package repository;

import data.TokenEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;


@Repository
public interface TokenRepository extends CrudRepository<TokenEntity, String> {
    Optional<TokenEntity> findByPlayerUUID(String playerUUID);

    @Transactional
    void deleteByPlayerUUID(String playerUUID);
}
