package ua.softserve.service.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.softserve.persistence.entity.Academy;
import ua.softserve.persistence.entity.GroupInfo;
import ua.softserve.persistence.entity.ProfileInfo;
import ua.softserve.service.AcademyService;
import ua.softserve.service.ProfileService;
import ua.softserve.service.dto.AcademyForSaveDTO;

@Service
public class GroupInfoConverter {

    @Autowired
    AcademyService academyService;

    @Autowired
    ProfileService profileService;


    public GroupInfo toEntity(int academyId, AcademyForSaveDTO academyDTO) {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setAcademy(getAcademyById(academyId));
        groupInfo.setGroupName(academyDTO.getGrName());
        groupInfo.setProfileInfo(getProfileInfo(academyDTO.getProfileId()));
        groupInfo.setStudentsPlannedToEnrollment(academyDTO.getStudentPlannedToEnrollment());
        groupInfo.setStudentsPlannedToGraduate(academyDTO.getStudentPlannedToGraduate());

        return groupInfo;
    }

    private Academy getAcademyById(int academyId) {
        return academyService.findOne(academyId);
    }

    private ProfileInfo getProfileInfo(int profileInfoId) {
        return profileService.findOne(profileInfoId);
    }
}
