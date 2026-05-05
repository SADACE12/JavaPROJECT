package org.example.javaalmas20.repository;

import org.example.javaalmas20.domain.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, UUID> {

    List<QuizResult> findByRoomCodeOrderByPercentageDesc(String roomCode);

    List<QuizResult> findByStudentNameOrderBySubmittedAtDesc(String studentName);
}
