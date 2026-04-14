package org.tutorbooking.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.*;
import org.tutorbooking.domain.enums.TutorApplicationStatus;
import org.tutorbooking.domain.enums.TutorRequestStatus;
import org.tutorbooking.dto.request.TutorApplicationCreateRequest;
import org.tutorbooking.dto.request.TutorRequestCreateRequest;
import org.tutorbooking.dto.request.TutorRequestUpdateRequest;
import org.tutorbooking.dto.response.TutorApplicationResponse;
import org.tutorbooking.dto.response.TutorRequestResponse;
import org.tutorbooking.exception.ResourceNotFoundException;
import org.tutorbooking.repository.*;
import org.tutorbooking.service.EmailService;
import org.tutorbooking.service.TutorRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TutorRequestServiceImpl implements TutorRequestService {

    private final TutorRequestRepository tutorRequestRepository;
    private final TutorApplicationRepository tutorApplicationRepository;
    private final ParentRepository parentRepository;
    private final TutorRepository tutorRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final EmailService emailService;

    @Override
    public TutorRequest createRequest(Long userId, TutorRequestCreateRequest req) {
        Parent parent = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent không tồn tại"));

        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject không tồn tại"));

        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student không tồn tại"));

        if (!student.getParent().getId().equals(parent.getId())) {
            throw new RuntimeException("Học sinh này không thuộc quyền quản lý của bạn");
        }

        TutorRequest tutorRequest = TutorRequest.builder()
                .parent(parent)
                .subject(subject)
                .student(student)
                .gradeLevel(req.getGradeLevel())
                .desiredPrice(req.getDesiredPrice())
                .teachingMode(req.getTeachingMode())
                .preferredArea(req.getPreferredArea())
                .scheduleNote(req.getScheduleNote())
                .sessionsPerWeek(req.getSessionsPerWeek() != null ? req.getSessionsPerWeek() : 1)
                .status(TutorRequestStatus.SEARCHING)
                .build();

        return tutorRequestRepository.save(tutorRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public TutorRequestResponse getRequestDetail(Long requestId) {
        TutorRequest tutorRequest = tutorRequestRepository.findDetailById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu không tồn tại"));

        long applicantsCount = tutorApplicationRepository.countByRequestId(requestId);

        return mapToTutorRequestResponse(tutorRequest, applicantsCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorRequestResponse> getMyRequests(Long userId) {
        Parent parent = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent không tồn tại"));
        List<TutorRequest> requests = tutorRequestRepository.findByParentIdWithSubject(parent.getId());

        return requests.stream().map(tr -> {
            long applicantsCount = tutorApplicationRepository.countByRequestId(tr.getId());
            return mapToTutorRequestResponse(tr, applicantsCount);
        }).collect(Collectors.toList());
    }

    @Override
    public void updateRequest(Long requestId, Long userId, TutorRequestUpdateRequest req) {
        Parent parent = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent không tồn tại"));
        TutorRequest tutorRequest = tutorRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu không tồn tại"));

        if (!tutorRequest.getParent().getId().equals(parent.getId())) {
            throw new RuntimeException("Không có quyền cập nhật yêu cầu này");
        }

        if (tutorRequest.getStatus() == TutorRequestStatus.MATCHED ||
                tutorRequest.getStatus() == TutorRequestStatus.CANCELLED) {
            throw new RuntimeException("Không thể cập nhật yêu cầu ở trạng thái hiện tại");
        }

        if (req.getDesiredPrice() != null) {
            tutorRequest.setDesiredPrice(req.getDesiredPrice());
        }
        if (req.getTeachingMode() != null) {
            tutorRequest.setTeachingMode(req.getTeachingMode());
        }
        if (req.getPreferredArea() != null) {
            tutorRequest.setPreferredArea(req.getPreferredArea());
        }
        if (req.getScheduleNote() != null) {
            tutorRequest.setScheduleNote(req.getScheduleNote());
        }
        if (req.getSessionsPerWeek() != null) {
            tutorRequest.setSessionsPerWeek(req.getSessionsPerWeek());
        }

        tutorRequestRepository.save(tutorRequest);
    }

    @Override
    public void cancelRequest(Long requestId, Long userId) {
        Parent parent = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent không tồn tại"));
        TutorRequest tutorRequest = tutorRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu không tồn tại"));

        if (!tutorRequest.getParent().getId().equals(parent.getId())) {
            throw new RuntimeException("Không có quyền hủy yêu cầu này");
        }

        if (tutorRequest.getStatus() == TutorRequestStatus.MATCHED) {
            throw new RuntimeException("Không thể hủy yêu cầu đã được kết nối");
        }

        tutorRequest.setStatus(TutorRequestStatus.CANCELLED);
        tutorRequestRepository.save(tutorRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorRequestResponse> getAllRequests() {
        List<TutorRequest> requests = tutorRequestRepository.findByStatusIn(List.of(TutorRequestStatus.SEARCHING, TutorRequestStatus.HAS_APPLICANTS));

        return requests.stream().map(tr -> {
            long applicantsCount = tutorApplicationRepository.countByRequestId(tr.getId());
            return mapToTutorRequestResponse(tr, applicantsCount);
        }).collect(Collectors.toList());
    }

    @Override
    public TutorApplication applyForRequest(Long requestId, Long userId, TutorApplicationCreateRequest req) {
        TutorRequest tutorRequest = tutorRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu không tồn tại"));

        Tutor tutor = tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Gia sư không tồn tại"));

        if (!"approved".equalsIgnoreCase(tutor.getApprovalStatus())) {
             throw new RuntimeException("Tài khoản gia sư chưa được phê duyệt, không thể ứng tuyển");
        }

        if (tutorRequest.getStatus() == TutorRequestStatus.MATCHED || tutorRequest.getStatus() == TutorRequestStatus.CANCELLED) {
             throw new RuntimeException("Bài tuyển dụng đã đóng hoặc đã nhận đủ gia sư");
        }

        if (tutorApplicationRepository.existsByRequestIdAndTutorId(requestId, tutor.getId())) {
            throw new RuntimeException("Bạn đã ứng tuyển cho yêu cầu này rồi");
        }

        if (tutorRequest.getStatus() == TutorRequestStatus.SEARCHING) {
            tutorRequest.setStatus(TutorRequestStatus.HAS_APPLICANTS);
            tutorRequestRepository.save(tutorRequest);
        }

        TutorApplication application = TutorApplication.builder()
                .request(tutorRequest)
                .tutor(tutor)
                .proposedPrice(req.getProposedPrice())
                .coverLetter(req.getCoverLetter())
                .status(TutorApplicationStatus.PENDING)
                .build();

        application = tutorApplicationRepository.save(application);

        String parentEmail = tutorRequest.getParent().getUser().getEmail();
        if (parentEmail == null || parentEmail.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng đăng kí email để nhận thông báo");
        }

        emailService.sendApplicationNotificationEmail(
                parentEmail,
                tutorRequest.getParent().getUser().getFullName(),
                tutor.getUser().getFullName(),
                tutorRequest.getSubject().getName(),
                req.getProposedPrice()
        );

        return application;
    }

    @Override
    public void withdrawApplication(Long applicationId, Long userId) {
        Tutor tutor = tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Gia sư không tồn tại"));
        TutorApplication application = tutorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ứng tuyển không tồn tại"));

        if (!application.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("Không có quyền rút lại ứng tuyển này");
        }

        if (application.getStatus() != TutorApplicationStatus.PENDING) {
            throw new RuntimeException("Không thể rút lại ứng tuyển ở trạng thái hiện tại");
        }

        tutorApplicationRepository.delete(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorApplicationResponse> getApplicationsForRequest(Long requestId, Long userId) {
        Parent parent = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent không tồn tại"));
        TutorRequest tutorRequest = tutorRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu không tồn tại"));

        if (!tutorRequest.getParent().getId().equals(parent.getId())) {
            throw new RuntimeException("Không có quyền xem ứng tuyển cho yêu cầu này");
        }

        List<TutorApplication> applications = tutorApplicationRepository.findByRequestIdWithTutor(requestId);

        return applications.stream()
                .map(this::mapToTutorApplicationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorApplicationResponse> getMyApplications(Long userId) {
        Tutor tutor = tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Gia sư không tồn tại"));
        List<TutorApplication> applications = tutorApplicationRepository.findByTutorIdWithRequest(tutor.getId());

        return applications.stream()
                .map(this::mapToTutorApplicationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void acceptApplication(Long applicationId, Long userId) {
        Parent parent = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent không tồn tại"));
        TutorApplication application = tutorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ứng tuyển không tồn tại"));

        if (!application.getRequest().getParent().getId().equals(parent.getId())) {
            throw new RuntimeException("Không có quyền chấp nhận ứng tuyển này");
        }

        if (application.getStatus() != TutorApplicationStatus.PENDING) {
            throw new RuntimeException("Ứng tuyển không ở trạng thái chờ phản hồi");
        }

        application.setStatus(TutorApplicationStatus.ACCEPTED);
        application.setRespondedAt(LocalDateTime.now());

        // Cập nhật trạng thái yêu cầu thành MATCHED
        TutorRequest request = application.getRequest();
        request.setStatus(TutorRequestStatus.MATCHED);
        request.setApprovedAt(LocalDateTime.now());

        // Từ chối tất cả ứng tuyển khác
        List<TutorApplication> otherApplications = tutorApplicationRepository
                .findByRequestIdAndStatus(request.getId(), TutorApplicationStatus.PENDING);
        otherApplications.forEach(app -> {
            app.setStatus(TutorApplicationStatus.REJECTED);
            app.setRespondedAt(LocalDateTime.now());
        });

        tutorApplicationRepository.save(application);
        tutorApplicationRepository.saveAll(otherApplications);
        tutorRequestRepository.save(request);
    }

    @Override
    public void rejectApplication(Long applicationId, Long userId) {
        Parent parent = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent không tồn tại"));
        TutorApplication application = tutorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ứng tuyển không tồn tại"));

        if (!application.getRequest().getParent().getId().equals(parent.getId())) {
            throw new RuntimeException("Không có quyền từ chối ứng tuyển này");
        }

        if (application.getStatus() != TutorApplicationStatus.PENDING) {
            throw new RuntimeException("Ứng tuyển không ở trạng thái chờ phản hồi");
        }

        application.setStatus(TutorApplicationStatus.REJECTED);
        application.setRespondedAt(LocalDateTime.now());

        tutorApplicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public TutorApplicationResponse getApplicationDetail(Long applicationId) {
        TutorApplication application = tutorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ứng tuyển không tồn tại"));

        return mapToTutorApplicationResponse(application);
    }


    private TutorRequestResponse mapToTutorRequestResponse(TutorRequest tutorRequest, long applicantsCount) {
        TutorRequestResponse response = TutorRequestResponse.builder()
                .id(tutorRequest.getId())
                .parentId(tutorRequest.getParent().getId())
                .parentName(tutorRequest.getParent().getUser().getFullName())
                .parentPhone(tutorRequest.getParent().getUser().getPhone())
                .parentEmail(tutorRequest.getParent().getUser().getEmail())
                .subjectId(tutorRequest.getSubject().getId())
                .subjectName(tutorRequest.getSubject().getName())
                .studentId(tutorRequest.getStudent() != null ? tutorRequest.getStudent().getId() : null)
                .studentName(tutorRequest.getStudent() != null ? tutorRequest.getStudent().getFullName() : null)
                .gradeLevel(tutorRequest.getGradeLevel())
                .desiredPrice(tutorRequest.getDesiredPrice())
                .teachingMode(tutorRequest.getTeachingMode())
                .preferredArea(tutorRequest.getPreferredArea())
                .scheduleNote(tutorRequest.getScheduleNote())
                .sessionsPerWeek(tutorRequest.getSessionsPerWeek())
                .status(tutorRequest.getStatus())
                .approvedAt(tutorRequest.getApprovedAt())
                .createdAt(tutorRequest.getCreatedAt())
                .updatedAt(tutorRequest.getUpdatedAt())
                .applicantsCount(applicantsCount)
                .build();
        return response;
    }

    private TutorApplicationResponse mapToTutorApplicationResponse(TutorApplication application) {
        User tutorUser = application.getTutor().getUser();
        TutorRequest request = application.getRequest();
        Parent parent = request.getParent();

        return TutorApplicationResponse.builder()
                .id(application.getId())
                .requestId(request.getId())
                .parentId(parent.getId())
                .parentName(parent.getUser().getFullName())
                .subjectId(request.getSubject().getId())
                .subjectName(request.getSubject().getName())
                .gradeLevel(request.getGradeLevel())
                .tutorId(application.getTutor().getId())
                .tutorName(tutorUser.getFullName())
                .tutorEmail(tutorUser.getEmail())
                .tutorPhone(tutorUser.getPhone())
                .tutorEducationLevel(application.getTutor().getEducationLevel())
                .tutorExperience(application.getTutor().getExperience())
                .proposedPrice(application.getProposedPrice())
                .coverLetter(application.getCoverLetter())
                .status(application.getStatus())
                .respondedAt(application.getRespondedAt())
                .createdAt(application.getCreatedAt())
                .build();
    }
}
