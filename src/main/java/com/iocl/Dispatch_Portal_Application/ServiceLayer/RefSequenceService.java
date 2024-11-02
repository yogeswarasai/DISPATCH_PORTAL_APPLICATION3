package com.iocl.Dispatch_Portal_Application.ServiceLayer;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iocl.Dispatch_Portal_Application.Entity.RefSequence;
import com.iocl.Dispatch_Portal_Application.Repositaries.RefSequenceRepository;

@Service
public class RefSequenceService {

	@Autowired
    private RefSequenceRepository refSequenceRepository;

    public List<RefSequence> findAll() {
        return refSequenceRepository.findAll();
    }

    public Page<RefSequence> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return refSequenceRepository.findAll(pageable);
    }
    public Optional<RefSequence> findById(String locCode) {
        return refSequenceRepository.findById(locCode);
    }

//    public RefSequence save(RefSequence refSequence) {
//        return refSequenceRepository.save(refSequence);
//    }

    @Transactional
    public RefSequence save(RefSequence refSequence) {
        if (refSequenceRepository.existsByLocCode(refSequence.getLocCode())) {
            return null; // Indicate that the locCode already exists
        }
        return refSequenceRepository.save(refSequence);
    }
    public void deleteById(String locCode) {
        refSequenceRepository.deleteById(locCode);
    }

}
