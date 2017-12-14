package ua.softserve.service;

import ua.softserve.persistence.entity.Academy;
import ua.softserve.service.dto.AcademyDTO;
import ua.softserve.service.dto.AcademyForSaveDTO;

import java.util.List;

public interface AcademyService {
    Academy getById(Integer id);

    Integer save(Academy academy);

    void saveAcademyFromAcademyDTO(AcademyForSaveDTO academyDTO);

    // void saveCustom(int id,String role,int[] arr, EmployeeService employeeService);

    Academy findOne(int id);

    AcademyForSaveDTO getAcademyDTO();

    List<Academy> getAllAcademies();
}
