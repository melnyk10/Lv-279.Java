package ua.softserve.persistence.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.softserve.persistence.entity.Student;

import java.util.List;

public interface StudentRepository  extends JpaRepository<Student,Integer> {

}
