package com.ahsmart.campusmarket.service.mentor;

import com.ahsmart.campusmarket.model.Mentor;
import com.ahsmart.campusmarket.repositories.MentorRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MentorServiceImpl implements MentorService {

    private final MentorRepository mentorRepository;
    private final UsersRepository usersRepository;

    public MentorServiceImpl(MentorRepository mentorRepository, UsersRepository usersRepository) {
        this.mentorRepository = mentorRepository;
        this.usersRepository = usersRepository;
    }

    @Override
    public List<Mentor> getAllMentors() {
        return mentorRepository.findAll();
    }

    @Override
    public Mentor addMentor(String mentorName, String mentorEmail) {
        if (mentorName == null || mentorName.isBlank()) {
            throw new IllegalArgumentException("Mentor name is required");
        }
        if (mentorEmail == null || mentorEmail.isBlank()) {
            throw new IllegalArgumentException("Mentor email is required");
        }
        if (mentorRepository.existsByMentorEmail(mentorEmail)) {
            throw new IllegalArgumentException("Mentor email already exists");
        }

        Mentor mentor = new Mentor();
        mentor.setMentorName(mentorName.trim());
        mentor.setMentorEmail(mentorEmail.trim());
        return mentorRepository.save(mentor);
    }

    @Override
    @Transactional
    public void deleteMentor(Long mentorId) {
        if (mentorId == null) {
            throw new IllegalArgumentException("Mentor id is required");
        }
        long usageCount = usersRepository.countByMentorMentorId(mentorId);
        if (usageCount > 0) {
            throw new IllegalArgumentException("Cannot delete mentor: assigned to " + usageCount + " user(s)");
        }
        mentorRepository.deleteById(mentorId);
    }
}

