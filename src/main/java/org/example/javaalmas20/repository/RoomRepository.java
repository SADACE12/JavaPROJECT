package org.example.javaalmas20.repository;

import org.example.javaalmas20.domain.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findByCodeAndIsActiveTrue(String code);

    List<Room> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(String createdBy);

    boolean existsByCode(String code);
}
