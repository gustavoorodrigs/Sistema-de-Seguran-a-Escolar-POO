package com.escolasegura.modules.students.repository;

import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.model.StudentGuardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, Long> {

    List<StudentGuardian> findByStudentId(Long studentId);

    List<StudentGuardian> findByGuardianUserId(Long guardianUserId);

    boolean existsByStudentIdAndGuardianUserId(Long studentId, Long guardianUserId);

    Optional<StudentGuardian> findByStudentIdAndGuardianUserId(Long studentId, Long guardianUserId);

    void deleteByStudentIdAndGuardianUserId(Long studentId, Long guardianUserId);

    @Query("""
        SELECT sg.student FROM StudentGuardian sg
        WHERE sg.guardianUser.email = :email
        ORDER BY sg.student.fullName ASC
    """)
    List<Student> findStudentsByGuardianEmail(@Param("email") String email);

    @Query("""
        SELECT COUNT(sg) > 0 FROM StudentGuardian sg
        WHERE sg.student.id = :studentId AND sg.guardianUser.email = :email
    """)
    boolean isGuardianLinkedToStudent(@Param("studentId") Long studentId, @Param("email") String email);
}
