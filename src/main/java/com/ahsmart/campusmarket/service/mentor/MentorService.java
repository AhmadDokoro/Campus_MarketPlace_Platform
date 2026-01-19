package com.ahsmart.campusmarket.service.mentor;

import com.ahsmart.campusmarket.model.Mentor;

import java.util.List;

public interface MentorService {

    List<Mentor> getAllMentors();

    Mentor addMentor(String mentorName, String mentorEmail);

    void deleteMentor(Long mentorId);
}

