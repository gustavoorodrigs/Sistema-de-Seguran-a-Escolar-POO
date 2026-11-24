package com.escolasegura.modules.students.repository;

import com.escolasegura.modules.students.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByBadgeCode(String badgeCode);

    boolean existsByBadgeCodeAndIdNot(String badgeCode, Long id);

    Optional<Student> findByBadgeCode(String badgeCode);

    @Query("""
        SELECT s FROM Student s
        WHERE (:name IS NULL OR :name = '' OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:className IS NULL OR :className = '' OR LOWER(s.className) LIKE LOWER(CONCAT('%', :className, '%')))
          AND (:active IS NULL OR s.active = :active)
        ORDER BY s.fullName ASC
    """)
    List<Student> searchStudents(
            @Param("name") String name,
            @Param("className") String className,
            @Param("active") Boolean active
    );
}
