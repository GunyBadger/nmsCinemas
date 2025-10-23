package com.nmsCinemas.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nmsCinemas.models.Theatre;
import com.nmsCinemas.repository.TheatreRepository;

@Service
@Transactional(readOnly = true)
public class TheatreService {

    private final TheatreRepository theatreRepository;

    // Constructor injection (no Lombok)
    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    public List<Theatre> findAll() {
        return theatreRepository.findAll();
    }

    public Optional<Theatre> findById(Long id) {
        return theatreRepository.findById(id);
    }

    @Transactional
    public Theatre save(Theatre theatre) {
        return theatreRepository.save(theatre);
    }

    @Transactional
    public void deleteById(Long id) {
        theatreRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return theatreRepository.existsById(id);
    }
}
