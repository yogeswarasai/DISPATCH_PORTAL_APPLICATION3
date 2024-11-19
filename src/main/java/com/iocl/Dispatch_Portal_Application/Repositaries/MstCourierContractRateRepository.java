package com.iocl.Dispatch_Portal_Application.Repositaries;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iocl.Dispatch_Portal_Application.Entity.MstCourierContractRate;
import com.iocl.Dispatch_Portal_Application.composite_pk.MstCourierContractRateId;

@Repository
public interface MstCourierContractRateRepository extends JpaRepository<MstCourierContractRate, MstCourierContractRateId> {

	MstCourierContractRate findByLocCodeAndCourierContNo(String locCode, String contno);

	List<MstCourierContractRate> findByCourierContNo(String courierContNo);

//	@Query("SELECT cd FROM MstCourierContractRate cd where cd.locCode=:locCode AND cd.courierContNo=:courierContNo")
//	List<MstCourierContractRate> findByLocCodeAndCourierContNo(@Param("locCode")  String locCode, @Param("courierContNo") String courierContNo);    
}
