package pl.polsl.projectmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.polsl.projectmanagementsystem.dto.LecturerDto;
import pl.polsl.projectmanagementsystem.dto.UserDto;
import pl.polsl.projectmanagementsystem.exception.UserNotFoundException;
import pl.polsl.projectmanagementsystem.mapper.LecturerMapper;
import pl.polsl.projectmanagementsystem.mapper.user.UserMapper;
import pl.polsl.projectmanagementsystem.model.Lecturer;
import pl.polsl.projectmanagementsystem.model.Student;
import pl.polsl.projectmanagementsystem.repository.LecturerRepository;

@Service
@RequiredArgsConstructor
public class LecturerService {

    private final KeycloakService keycloakService;
    private final LecturerRepository lecturerRepository;
    private final LecturerMapper lecturerMapper;
    private final UserMapper userMapper;

    @Transactional
    public LecturerDto createNewLecturer(UserDto userDto, LecturerDto lecturerDto) {
        userDto.setRole("LECTURER");

        String userId = keycloakService.addUser(userDto);

        lecturerDto.setUserId(userId);

        Lecturer save = lecturerRepository.save(lecturerMapper.mapDtoToEntity(lecturerDto));

        lecturerDto = lecturerMapper.mapEntityToDto(save);

        return lecturerDto;
    }

    @Transactional
    public UserDto deleteLecturer(Long id) {
        Lecturer lecturer = lecturerRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Lecturer not found"));

        UserRepresentation userRepresentation = keycloakService.deleteUser(lecturer.getUserId());

        lecturerRepository.delete(lecturer);

        return userMapper.mapModelApiToDto(userRepresentation);
    }

    @Transactional
    public LecturerDto updateLecturer(UserDto userDto, LecturerDto lecturerDto, Long id) {
        Lecturer lecturer = lecturerRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Lecturer not found"));

        userDto.setRole("LECTURER");

        keycloakService.updateUser(userDto, lecturer.getUserId());

        lecturerDto.setUserId(lecturer.getUserId());

        lecturer = lecturerMapper.mapDtoToEntity(lecturerDto);
        lecturer.setId(id);

        lecturerRepository.save(lecturer);

        return lecturerMapper.mapEntityToDto(lecturer);
    }
}
