package ua.softserve.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.softserve.persistence.entity.*;
import ua.softserve.persistence.repo.*;
import ua.softserve.service.*;
import ua.softserve.service.dto.ItaTacticalPlanByGroupStageDto;

import java.util.*;

import static ua.softserve.persistence.constants.ConstantsFromDb.*;

@Service
public class ItaTacticalPlanByGroupStageDtoServiceImpl implements ItaTacticalPlanByGroupStageDtoService {

    @Autowired
    AcademyService academyService;
    @Autowired
    LanguageTranslationsRepository languageTranslationsRepository;
    @Autowired
    GroupInfoRepository groupInfoRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    TeacherTypeRepository teacherTypeRepository;
    @Autowired
    GroupInfoTeachersRepository groupInfoTeachersRepository;
    @Autowired
    EmployeeRepository employeeRepository;


    @Override
    public ItaTacticalPlanByGroupStageDto findById(int id) {
        ItaTacticalPlanByGroupStageDto dto = new ItaTacticalPlanByGroupStageDto();
        Academy academy = academyService.findOne(id);
        dto.setGroupId(id);
        dto.setCG(academy.getTechnologies().getName());
        dto.setLocation(languageTranslationsRepository.getOneCityNameTranslationByItemId(academy.getCity().getCityId()));
        dto.setStartDate(academy.getStartDate());
        dto.setEndDate(academy.getEndDate());
        dto.setGroupStatus(academy.getAcademyStages().getName());
        dto.setPaymentSatus(academy.getFree() == 1 ? "Founded by Softserve" : "Open group");
        GroupInfo academyInfo = groupInfoRepository.findByAcademyAcademyId(academy.getAcademyId());
        if (academyInfo != null) {
            dto.setGroupName(academyInfo.getGroupName());
            ProfileInfo profileInfo = groupInfoRepository.findByAcademyAcademyId(academy.getAcademyId()).getProfileInfo();
            if (profileInfo != null) {
                dto.setProfile(profileInfo.getProfileName());
            } else {
                dto.setProfile(null);
            }
        } else {
            dto.setProfile(null);
        }
        this.calculationStudentsStatuses(dto);
        this.setTrainer(dto);

        return dto;
    }

    @Override
    public List<ItaTacticalPlanByGroupStageDto> findAll() {
        List<ItaTacticalPlanByGroupStageDto> itaTacticalPlanByGroupStageDtos = new ArrayList<ItaTacticalPlanByGroupStageDto>();
        List<Academy> academies = academyService.getAllAcademies();
        for (Academy a : academies) {
            itaTacticalPlanByGroupStageDtos.add(this.findById(a.getAcademyId()));
        }
        return itaTacticalPlanByGroupStageDtos;
    }

    @Override
    public List<ItaTacticalPlanByGroupStageDto> findPlanedGroupForTwoMoth() {
        List<ItaTacticalPlanByGroupStageDto> itaTacticalPlanByGroupStageDtos = new ArrayList<ItaTacticalPlanByGroupStageDto>();
        List<Academy> academies = academyService.getAllAcademies();
        Calendar dateForComparison = new GregorianCalendar();
        dateForComparison.add(Calendar.MONTH, 2);
        for (Academy a : academies) {
            Calendar academyStartDate = new GregorianCalendar();
            academyStartDate.setTimeInMillis(a.getStartDate().getTime());
            if ((a.getAcademyStages().getStageId() == 1) && academyStartDate.before(dateForComparison)) {
                itaTacticalPlanByGroupStageDtos.add(this.findById(a.getAcademyId()));
            }
        }
        return itaTacticalPlanByGroupStageDtos;
    }

    @Override
    public List<ItaTacticalPlanByGroupStageDto> findGroupInProces() {
        return null;
    }

    private void calculationStudentsStatuses(ItaTacticalPlanByGroupStageDto dto) {
        List<Student> allStudentsOfGroup = studentRepository.findAllByAcademy_AcademyId(dto.getGroupId());
        int studentRequested = 0;
        for (Student student : allStudentsOfGroup) {
            if (student.getStudentStatus().getId() == AS_GRADUATED_ID) {
                int currentValueGraduated = dto.getGraduated();
                dto.setGraduated(++currentValueGraduated);
            }
            if (student.getStudentStatus().getId() == SS_TRAINEE_ID || student.getStudentStatus().getId() == SS_TRAINEE_ID) {
                int currentValueStudentInProgress = dto.getStudyInProgress();
                dto.setStudyInProgress(++currentValueStudentInProgress);
            }
            if (student.getStudentStatus().getId() == SS_HIRED_ID) {
                int currentValueStudentHired = dto.getHired();
                dto.setStudyInProgress(++currentValueStudentHired);
            }
            studentRequested++;
        }
        dto.setRequested(studentRequested);
    }

    private void setTrainer(ItaTacticalPlanByGroupStageDto dto) {
        TeacherTypes typeTeacher = teacherTypeRepository.findOne(TT_TEACHER_ID);
        TeacherTypes typeExpert = teacherTypeRepository.findOne(TT_EXPERT_ID);
        String trainers = "";
        List<GroupInfoTeachers> allTeachersOfGroup = groupInfoTeachersRepository.findAllByAcademyAndTeacherType(academyService.getById(dto.getGroupId()), typeTeacher);
        List<GroupInfoTeachers> allExpertsOfGroup = groupInfoTeachersRepository.findAllByAcademyAndTeacherType(academyService.getById(dto.getGroupId()), typeExpert);
        for (GroupInfoTeachers teacherInfo : allTeachersOfGroup) {
            trainers += teacherInfo.getEmployee().getFirstNameEng() + " " + teacherInfo.getEmployee().getLastNameEng() + " ";
        }

        for (GroupInfoTeachers infoTeachers : allExpertsOfGroup) {
            trainers += infoTeachers.getEmployee().getFirstNameEng() + " " + infoTeachers.getEmployee().getLastNameEng() + " ";
        }

        dto.setTrainer(trainers);

    }
}
