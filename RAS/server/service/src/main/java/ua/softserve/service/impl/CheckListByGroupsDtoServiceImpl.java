/*
 * CheckListByGroupsDto2ServiceImpl
 *
 * Version 1.0-SNAPSHOT
 *
 * 03.12.17
 *
 * All rights reserved by DoubleO Team (Team#1)
 * */

package ua.softserve.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.softserve.persistence.entity.*;
import ua.softserve.persistence.repo.*;
import ua.softserve.service.CheckListByGroupsDtoService;
import ua.softserve.service.dto.CheckListByGroupsDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ua.softserve.persistence.constants.ConstantsFromDb.*;
import static ua.softserve.service.dto.CheckListByGroupsDto.*;

@Service
public class CheckListByGroupsDtoServiceImpl implements CheckListByGroupsDtoService {
    @Autowired
    private AcademyRepository academyRepository;
    @Autowired
    private GroupInfoRepository groupInfoRepository;
    @Autowired
    private LanguageTranslationsRepository languageTranslationsRepository;
    @Autowired
    private AcademyStagesRepository academyStagesRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private GroupInfoTeachersRepository groupInfoTeachersRepository;

    @Override
    public List<CheckListByGroupsDto> getAllCheckListByGroupsDto() {

        List<Academy> allAcademies = academyRepository.findAll().stream()
                .filter(academy -> academy.getAcademyId() >= 586 && academy.getAcademyId() <= 932).limit(30)
                .collect(Collectors.toList());

        List<CheckListByGroupsDto> CheckListByGroupsDtos = new ArrayList<>();

        for (Academy academy : allAcademies) {
            CheckListByGroupsDtos.add(getCheckListByGroupDtoByAcademy(academy));
        }
        return CheckListByGroupsDtos;
    }

    private CheckListByGroupsDto getCheckListByGroupDtoByAcademy(Academy academy) {
        Integer academyId = academy.getAcademyId();
        CheckListByGroupsDto checkListByGroupsDto;
        GroupInfo groupInfo;
        String city;
        AcademyStages stage;
        List<Student> students;

        groupInfo = groupInfoRepository.findByAcademyAcademyId(academyId);

        city = languageTranslationsRepository.getOneCityNameTranslationByItemId(academy.getCity().getCityId());

        stage = academyStagesRepository.findOne(academy.getAcademyStages().getStageId());

        students = studentRepository.findAllByAcademy_AcademyId(academyId);

        checkListByGroupsDto = new CheckListByGroupsDto();
        checkListByGroupsDto.setCityName(city);
        checkListByGroupsDto.setGroupName((groupInfo == null) ? null : groupInfo.getGroupName());
        checkListByGroupsDto.setStatus(stage.getName());
        setTeachers(academyId, checkListByGroupsDto);
        Map<String, Integer> r = checkListByGroupsDto.getR();
        for (Map.Entry predicate : predicates.entrySet()) {
            r.put((String) predicate.getKey(), checkStudents((Predicate<Student>) predicate.getValue(), students));
        }

        checkListByGroupsDto.setTotal();
        return checkListByGroupsDto;
    }

    private void setTeachers(Integer academyId, CheckListByGroupsDto checkListByGroupsDto) {
        List<GroupInfoTeachers> teachers = groupInfoTeachersRepository.findAllByAcademyIdAndTeacherTypeId(academyId,
                TT_TEACHER_ID);
        ;
        List<GroupInfoTeachers> experts = groupInfoTeachersRepository.findAllByAcademyIdAndTeacherTypeId(academyId,
                TT_EXPERT_ID);
        ;
        List<GroupInfoTeachers> interviewers = groupInfoTeachersRepository.findAllByAcademyIdAndTeacherTypeId(academyId,
                TT_INTERVIEWER_ID);
        Map<String, Integer> r = checkListByGroupsDto.getR();
        checkListByGroupsDto.setTeachers(getTeachers(teachers));
        checkListByGroupsDto.setExperts(getTeachers(experts));
        r.put("teacherDefined", (teachers != null) ? 1 : 0);
        r.put("expertDefined", (experts != null) ? 1 : 0);
        r.put("interviewerDefined", (interviewers != null) ? 1 : 0);
        r.put("expertsLoadFilledIn", checkTeachers(git -> git.getContributedHours() != null, experts));
        r.put("interviewersLoadFilledIn", checkTeachers(git -> git.getContributedHours() != null, interviewers));
    }

    private String getTeachers(List<GroupInfoTeachers> teachers) {
        StringBuilder teachersSb = new StringBuilder();
        if (teachers != null) {
            for (GroupInfoTeachers git : teachers) {
                teachersSb.append(git.getEmployee().getFirstNameEng()).append(" ")
                        .append(git.getEmployee().getLastNameEng()).append("; ");
            }
        }
        return teachersSb.toString();
    }

    private Integer checkStudents(Predicate<Student> predicate, List<Student> students) {
        if (students == null) {
            return 0;
        }
        for (Student student : students) {
            if (!checkStudentStatus(student)) {
                continue;
            }
            if (!predicate.test(student)) {
                return 0;
            }
        }
        return 1;
    }

    private Integer checkTeachers(Predicate<GroupInfoTeachers> predicate, List<GroupInfoTeachers> groupInfoTeachers) {
        if (groupInfoTeachers == null) {
            return 0;
        }
        for (GroupInfoTeachers git : groupInfoTeachers) {
            if (!predicate.test(git)) {
                return 0;
            }
        }
        return 1;
    }

    private boolean checkStudentStatus(Student student) {
        if (student == null || student.getStudentStatus() == null) {
            return false;
        }
        int id = student.getStudentStatus().getId();
        return id == SS_TRAINEE_ID || id == SS_ACCEPTED_PRE_OFFER_ID || id == SS_GRADUATED_ID;
    }
}
