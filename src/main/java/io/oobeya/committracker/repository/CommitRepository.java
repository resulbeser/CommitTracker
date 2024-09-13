
package io.oobeya.committracker.repository;

import io.oobeya.committracker.entity.Commit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitRepository extends JpaRepository<Commit, Long> {
}
