package ua.softserve.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.softserve.persistence.entity.*;
import ua.softserve.persistence.repo.AcademyRepository;
import ua.softserve.service.*;
import ua.softserve.service.dto.AcademyDTO;
import ua.softserve.service.dto.AcademyDropDownLists;
import ua.softserve.service.dto.GroupOverviewDropDownLists;
import ua.softserve.validator.GroupValidator;

import java.sql.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AcademyServiceImpl implements AcademyService {
    private final Logger logger = LoggerFactory.getLogger(AcademyServiceImpl.class.getName());

    @Autowired
    AcademyRepository academyRepository;

    @Autowired
    AcademyStagesService academyStagesService;

    @Autowired
    DirectionService directionService;

    @Autowired
    TechnologyService technologyServiceImpl;

    @Autowired
    ProfileService profileService;

    @Autowired
    LanguageTranslationsService languageTranslationsService;

    @Autowired
    GroupInfoService groupInfoService;

    @Autowired
    GroupValidator groupValidator;

    @Autowired
    HistoryService historyService;

    @Autowired
    AcademyService academyService;

    @Autowired
    CityService cityService;


    @Autowired
    TechnologyService technologyService;

    @Autowired
    StudentService studentService;

    /**
     * Saves a given entity.
     *
     * @param academy
     * @return id of saved entity.
     */
    @Transactional
    @Override
    public Integer save(Academy academy) {
        return academyRepository.save(academy).getAcademyId();
    }

    /**
     * Validate academyDTO if all is good saves Academy and GroupInfo entity.
     *
     * @param academyDTO what is come from client.
     */
    @Transactional
    @Override
    public void saveAcademyAndGroupInfoFromAcademyDTO(AcademyDTO academyDTO) {
            logger.info("Before groupValidator.validate(academyDTO)");
            groupValidator.validate(academyDTO);

            Academy academy = academyDTO.getAcademyId() == 0 ? new Academy() : academyService.findOne(academyDTO.getAcademyId());

            academy.setName(academyDTO.getNameForSite());
            academy.setAcademyStages(getAcademyStages(academyDTO.getAcademyStagesId()));
            academy.setStartDate(convertLongToDate(academyDTO.getStartDate()));
            academy.setEndDate(convertLongToDate(academyDTO.getEndDate()));
            academy.setCity(getCity(academyDTO.getCityId()));
            academy.setFree(academyDTO.getPayment());
            academy.setDirections(getDirection(academyDTO.getDirectionId()));
            academy.setTechnologies(getTechnologies(academyDTO.getTechnologieId()));

            int academyId = save(academy);

            GroupInfo groupInfo = academyDTO.getGroupInfoId() == 0 ? new GroupInfo() : groupInfoService.findOneGroupInfoByAcademyId(academyDTO.getAcademyId());

            groupInfo.setAcademy(getAcademyById(academyId));
            groupInfo.setGroupName(academyDTO.getGrName());
            groupInfo.setProfileInfo(getProfileInfo(academyDTO.getProfileId()));
            groupInfo.setStudentsPlannedToEnrollment(academyDTO.getStudentPlannedToEnrollment());
            groupInfo.setStudentsPlannedToGraduate(academyDTO.getStudentPlannedToGraduate());

            groupInfoService.save(groupInfo);

            historyService.saveModifyby(academyId);
    }

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal null} if none found
     * @throws NoSuchElementException if {@code id} is {@literal null}
     */
    @Transactional
    @Override
    public Academy findOne(int id) {
        Academy findGroup = academyRepository.findOne(id);
        if (findGroup == null) {
            logger.error("Group with id " + id + " not found");
            throw new NoSuchElementException("Group with id " + id + " not found");
        }
        return findGroup;
    }

    /**
     * Method combines information for dropdown lists on the UI to DTO.
     *
     * @return DTO that contains information for dropdown lists.
     */
    @Transactional
    @Override
    public AcademyDropDownLists getAcademyDTO() {
        AcademyDropDownLists academyDropDownLists = new AcademyDropDownLists();
        academyDropDownLists.setAcademyStages(academyStagesService.getAllAcademyStagesService());
        academyDropDownLists.setDirection(directionService.findAllDirectionsInIta());
        academyDropDownLists.setTechnologie(technologyServiceImpl.findAllTechonologyInIta());
        academyDropDownLists.setProfile(profileService.findAll());
        academyDropDownLists.setCityNames(languageTranslationsService.getAllLanguageTranslationsName());
        return academyDropDownLists;
    }

    /**
     * Method combines information for dropdown lists on the UI to DTO.
     *
     * @return DTO that contains information for dropdown lists.
     */
    @Transactional
    @Override
    public GroupOverviewDropDownLists getGroupOverviewDTO() {
        GroupOverviewDropDownLists groupOverviewDropDownLists = new GroupOverviewDropDownLists();
        groupOverviewDropDownLists.setDirection(directionService.findAllDirectionsInIta());
        groupOverviewDropDownLists.setCityNames(languageTranslationsService.getAllLanguageTranslationsName());
        groupOverviewDropDownLists.setGroupNames(groupInfoService.findAllGroupsWithAcademies());
        return groupOverviewDropDownLists;
    }

    /**
     * Method returns all instances of the Academy type.
     *
     * @return all entities
     */
    @Transactional
    @Override
    public List<Academy> getAllAcademies() {
        return academyRepository.findAll();
    }

    private Academy getAcademyById(int academyId) {
        return academyService.findOne(academyId);
    }

    private ProfileInfo getProfileInfo(int profileInfoId) {
        return profileService.findOne(profileInfoId);
    }

    private Date convertLongToDate(Long dateMilliseconds) {
        return new Date(dateMilliseconds);
    }

    private City getCity(int id) {
        return cityService.findOne(id);
    }

    private AcademyStages getAcademyStages(int academyStagesId) {
        return academyStagesService.findOne(academyStagesId);
    }

    private Directions getDirection(int direcrionId) {
        return directionService.findOne(direcrionId);
    }

    private Technologies getTechnologies(int technologieId) {
        return technologyService.findOne(technologieId);
    }
}
