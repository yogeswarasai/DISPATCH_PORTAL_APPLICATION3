package com.iocl.Dispatch_Portal_Application.Repositaries;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.iocl.Dispatch_Portal_Application.Entity.MstCourierContract;
import com.iocl.Dispatch_Portal_Application.composite_pk.MstCourierContractPK;

@Repository
public interface MstCourierContractRepository extends JpaRepository<MstCourierContract, MstCourierContractPK> {

	List<MstCourierContract> findByLocCode(String locCode);

	Optional<MstCourierContract> findByLocCodeAndCourierContNo(String locCode, String courierContNo);

	List<MstCourierContract> findByLocCodeAndStatus(String locCode, String string);

	
}

