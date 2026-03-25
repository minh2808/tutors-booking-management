package org.tutorbooking.service.Impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.tutorbooking.service.TutorService;
import org.tutorbooking.repository.TutorRepository;
import org.tutorbooking.repository.TutorSubjectRepository;
import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.TutorSubject;
import org.tutorbooking.domain.entity.Subject;
import org.tutorbooking.dto.request.UpdateTutorRequest;
import org.tutorbooking.dto.response.TutorDetailResponse;
import org.tutorbooking.dto.request.SubjectRequest;
import org.tutorbooking.repository.ParentRepository;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    private final TutorSubjectRepository tutorSubjectRepository;

    // =========================================
    // 1. LẤY DETAIL
    // =========================================
    @Override
    public TutorDetailResponse getTutorDetail(Long tutorId) {
        Tutor tutor = tutorRepository.findDetailById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));

        TutorDetailResponse res = new TutorDetailResponse();

        res.setId(tutor.getId());

        // user
        res.setFullName(tutor.getUser().getFullName());
        res.setAvatarUrl(tutor.getUser().getAvatarUrl());
        res.setEmail(tutor.getUser().getEmail());

        // tutor
        res.setEducationLevel(tutor.getEducationLevel());
        res.setExperience(tutor.getExperience());
        res.setQualifications(tutor.getQualifications());
        res.setTeachingMode(tutor.getTeachingMode());
        res.setTeachingArea(tutor.getTeachingArea());
        res.setApprovalStatus(tutor.getApprovalStatus());

        return res;
    }

    // =========================================
    // 2. LẤY PROFILE CỦA MÌNH
    // =========================================
    @Override
    public Tutor getMyProfile(Long userId) {
        return tutorRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
    }

    // =========================================
    // 3. UPDATE PROFILE
    // =========================================
    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateTutorRequest req) {

        Tutor tutor = tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));

        tutor.setEducationLevel(req.getEducationLevel());
        tutor.setExperience(req.getExperience());
        tutor.setQualifications(req.getQualifications());
        tutor.setTeachingMode(req.getTeachingMode());
        tutor.setTeachingArea(req.getTeachingArea());
    }

    // =========================================
    // 4. UPDATE SUBJECTS
    // =========================================
    @Override
    @Transactional
    public void updateSubjects(Long userId, List<SubjectRequest> reqs) {

        Tutor tutor = tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));

        //  xoá hết
        tutorSubjectRepository.deleteByTutorId(tutor.getId());

        //  insert lại
        List<TutorSubject> list = new ArrayList<>();

        for (SubjectRequest r : reqs) {

            TutorSubject ts = new TutorSubject();
            ts.setTutor(tutor);

            Subject subject = new Subject();
            subject.setId(r.getSubjectId());

            ts.setSubject(subject);
            ts.setGradeLevel(r.getGradeLevel());
            ts.setPricePerSession(r.getPricePerSession());

            list.add(ts);
        }

        tutorSubjectRepository.saveAll(list);
    }

    // =========================================
    // 5. LẤY SUBJECT
    // =========================================
    @Override
    public List<TutorSubject> getSubjects(Long tutorId) {
        return tutorSubjectRepository.findByTutorIdWithSubject(tutorId);
    }
}