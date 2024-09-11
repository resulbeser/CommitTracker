package io.oobeya.committracker.repository;

import io.oobeya.committracker.entity.CommitFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitFileRepository extends JpaRepository<CommitFile, Long> {
}