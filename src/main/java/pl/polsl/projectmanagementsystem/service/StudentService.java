package pl.polsl.projectmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.polsl.projectmanagementsystem.dto.*;
import pl.polsl.projectmanagementsystem.exception.SemesterNotFoundException;
import pl.polsl.projectmanagementsystem.exception.UserNotFoundException;
import pl.polsl.projectmanagementsystem.mapper.SemesterMapper;
import pl.polsl.projectmanagementsystem.mapper.student.StudentMapper;
import pl.polsl.projectmanagementsystem.mapper.user.UserMapper;
import pl.polsl.projectmanagementsystem.model.Semester;
import pl.polsl.projectmanagementsystem.model.Student;
import pl.polsl.projectmanagementsystem.model.StudentSemester;
import pl.polsl.projectmanagementsystem.repository.SemesterRepository;
import pl.polsl.projectmanagementsystem.repository.StudentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final StudentMapper studentMapper;
    private final SemesterMapper semesterMapper;
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;

    @Transactional
    public StudentDto createStudent(UserDto userDto, StudentDto studentDto, Long semesterId) {
        Student student = studentMapper.mapDtoToNewEntity(studentDto);

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new SemesterNotFoundException("No such semester found"));

        StudentSemester studentSemester = StudentSemester.builder()
                .student(student)
                .semester(semester)
                .build();

        student.getStudentSemesterList().add(studentSemester);

        userDto.setRole("STUDENT");

        String s = keycloakService.addUser(userDto);
        student.setUserId(s);

        return studentMapper.mapEntityToDto(studentRepository.save(student));
    }

    public void importStudent(UserDto userDto, StudentDto studentDto, List<SemesterDto> semesterDtos) {
        Student student = studentMapper.mapDtoToNewEntity(studentDto);

        semesterDtos.forEach(i -> {
            //semesterRepository.findByFieldOfStudyAndYearAndSemester(i.getFieldOfStudy(), i.getYear(), i.getSemester())
            semesterRepository.findById(1L)
                    .ifPresentOrElse(k -> {
                        StudentSemester studentSemester = StudentSemester.builder()
                                .student(student)
                                .semester(k)
                                .build();

                        student.getStudentSemesterList().add(studentSemester);
                        log.info("Semester found during import");

                    } ,() -> {
                        log.warn("Semester not found during import");
                    });
        });

        userDto.setRole("STUDENT");

        String s = keycloakService.addUser(userDto);
        student.setUserId(s);

        if(!student.getStudentSemesterList().isEmpty()) {
            studentRepository.save(student);
        }
    }

    @Transactional
    public UserDto deleteStudent(String id) {
        Student student = studentRepository.findByAlbumNo(id).orElseThrow(() -> new UserNotFoundException("Student not found"));

        UserRepresentation userRepresentation = keycloakService.deleteUser(student.getUserId());
        userRepresentation.setId(id);

        student.setIsActive(false);

        return userMapper.mapModelApiToDto(userRepresentation);
    }

    public StudentDto updateStudent(UserDto userDto, StudentDto studentDto, String id) {
        Student student = studentRepository.findByAlbumNo(id).orElseThrow(() -> new UserNotFoundException("Student not found"));

        userDto.setRole("STUDENT");
        studentDto.setUserId(student.getUserId());

        keycloakService.updateUser(userDto, student.getUserId());

        student = studentMapper.mapDtoToEntity(studentDto);
        student.setAlbumNo(id);
        student.setIsActive(true);
        student = studentRepository.save(student);

        return studentMapper.mapEntityToDto(student);
    }

    public FindResultDto<StudentDto> findStudentsBySemster(SearchDto searchDto, Long semesterId) {
        PageRequest pageRequest = PageRequest.of(searchDto.getPage().intValue(), searchDto.getLimit().intValue());

        Page<Student> students = studentRepository.findStudentsBySemester(semesterId, pageRequest);

        return FindResultDto.<StudentDto>builder()
                .count((long) students.getNumberOfElements())
                .results(students.getContent().stream()
                        .map(studentMapper::mapEntityToDto)
                        .collect(Collectors.toList()))
                .startElement(pageRequest.getOffset())
                .totalCount(students.getTotalElements())
                .build();
    }

    public FindResultDto<StudentDto> findAllStudents(SearchDto searchDto) {
        PageRequest pageRequest = PageRequest.of(searchDto.getPage().intValue(), searchDto.getLimit().intValue());

        Page<Student> students = studentRepository.findAllByIsActiveTrue(pageRequest);

        return FindResultDto.<StudentDto>builder()
                .count((long) students.getNumberOfElements())
                .results(students.getContent().stream()
                        .map(studentMapper::mapEntityToDto)
                        .collect(Collectors.toList()))
                .startElement(pageRequest.getOffset())
                .totalCount(students.getTotalElements())
                .build();
    }

    public StudentDto getInfo() {
        String userId = keycloakService.getUserId();

        Student student = studentRepository.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("Lecturer not found"));

        return studentMapper.mapEntityToDto(student);
    }

    public List<SemesterDto> getStudentSemesters() {
        StudentDto studentDto = getInfo();

        List<Semester> semesters = semesterRepository.findAllStudentSemesters(studentDto.getAlbumNo());

        return semesters.stream()
                .map(semesterMapper::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public StudentDto getStudentById(String id) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Lecturer not found"));

        UserRepresentation serviceUser = keycloakService.getUser(student.getUserId());

        StudentDto studentDto = studentMapper.mapEntityToDto(student);

        studentDto.setEmail(serviceUser.getEmail());

        return studentDto;
    }

    public List<StudentDto> findAllByLastName(String lastName) {
        return studentRepository.findAllByLastNameStartingWithIgnoreCaseAndIsActiveTrue(lastName)
                .stream()
                .map(studentMapper::mapEntityToDto)
                .collect(Collectors.toList());
    }
}
