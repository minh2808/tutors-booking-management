package org.tutorbooking.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.Subject;
import org.tutorbooking.dto.request.SubjectCreateRequest;
import org.tutorbooking.exception.ResourceNotFoundException;
import org.tutorbooking.repository.SubjectRepository;
import org.tutorbooking.repository.TutorSubjectRepository;
import org.tutorbooking.service.SubjectService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final TutorSubjectRepository tutorSubjectRepository;

    @Override
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @Override
    @Transactional
    public Subject createSubject(SubjectCreateRequest request) {
        if (subjectRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên môn học này đã tồn tại trên hệ thống");
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return subjectRepository.save(subject);
    }

    @Override
    @Transactional
    public Subject updateSubject(Long id, SubjectCreateRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + id));

        if (!subject.getName().equalsIgnoreCase(request.getName()) && subjectRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên môn học này đã tồn tại trên hệ thống");
        }

        subject.setName(request.getName());
        subject.setDescription(request.getDescription());

        return subjectRepository.save(subject);
    }

    @Override
    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + id));

        boolean isUsed = tutorSubjectRepository.existsBySubjectId(id);
        if (isUsed) {
            throw new RuntimeException("Không thể xóa. Hiện đang có gia sư đăng ký dạy môn học này.");
        }

        subjectRepository.delete(subject);
    }
}
