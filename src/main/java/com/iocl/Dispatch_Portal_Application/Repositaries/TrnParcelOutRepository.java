package com.iocl.Dispatch_Portal_Application.Repositaries;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.iocl.Dispatch_Portal_Application.Entity.TrnParcelOut;
import com.iocl.Dispatch_Portal_Application.composite_pk.TrnParcelOutPK;


public interface TrnParcelOutRepository extends JpaRepository<TrnParcelOut, TrnParcelOutPK>{

	//List<TrnParcelOut> findBySenderLocCode(String senderLocCode);
	
	   long countBySenderLocCode(String locCode);
	   
	    long countParcelOutBySenderLocCode(String locCode);
	   
	   
	    long countParcelOutByCreatedDateAndSenderLocCode(LocalDate date, String locCode);
	
   // List<TrnParcelOut> findBySenderLocCodeOrderByOutTrackingIdDesc(String senderLocCode);
	    
	    Page<TrnParcelOut> findBySenderLocCodeOrderByOutTrackingIdDesc(String senderLocCode, Pageable pageable); 


	
	//count parcels by today date
		Long countByCreatedDate(LocalDate createdDate);
	//fetch parcels by in between dates
	   // List<TrnParcelOut> findByCreatedDateBetween(LocalDate fromDate, LocalDate toDate);
	    
	   // List<TrnParcelOut> findByCreatedDateBetweenAndSenderLocCodeOrderByOutTrackingIdDesc(LocalDate fromDate, LocalDate toDate, String locCode);
//		@Query("SELECT t FROM TrnParcelOut t where t.createdDate BETWEEN :fromDate And :toDate And t.senderLocCode :locCode And OrderBy t.outTrackingId:Desc")
//		List<TrnParcelOut> findByCreatedDateBetweenAndSenderLocCodeOrderByOutTrackingIdDesc(LocalDate fromDate, LocalDate toDate, String locCode);
		
		@Query("SELECT t FROM TrnParcelOut t WHERE t.createdDate BETWEEN :fromDate AND :toDate AND t.senderLocCode = :locCode AND t.recordStatus = 'A' ORDER BY t.outTrackingId DESC")
		List<TrnParcelOut> findByCreatedDateBetweenAndSenderLocCodeOrderByOutTrackingIdDesc(
		    @Param("fromDate") LocalDate fromDate, 
		    @Param("toDate") LocalDate toDate, 
		    @Param("locCode") String locCode
		);

	    
//	    List<TrnParcelOut> findByCreatedDateBetweenAndSenderNameAndSenderLocCode(
//	            LocalDate fromDate, LocalDate toDate, String senderName, String senderLocCode);

		
		@Query("SELECT t FROM TrnParcelOut t WHERE t.createdDate BETWEEN :fromDate AND :toDate AND t.senderName = :senderName AND t.senderLocCode = :senderLocCode AND t.recordStatus = 'A'")
		List<TrnParcelOut> findByCreatedDateBetweenAndSenderNameAndSenderLocCode(
		    @Param("fromDate") LocalDate fromDate, 
		    @Param("toDate") LocalDate toDate, 
		    @Param("senderName") String senderName, 
		    @Param("senderLocCode") String senderLocCode
		);

		
	    boolean existsByConsignmentNumber(String consignmentNumber);

	    
	    @Query("select max(tpo.outTrackingId) from TrnParcelOut tpo")
		Long fetchNextId();

	  //  List<TrnParcelOut> findBySenderLocCodeAndSenderName(String locCode, String name);

	    
        Page<TrnParcelOut> findBySenderLocCodeAndSenderNameOrderByOutTrackingIdDesc(String locCode, String name, Pageable pageable);

	    Page<TrnParcelOut> findBySenderLocCodeAndSenderNameAndCreatedDateOrderByOutTrackingId(String locCode, String name, LocalDate sentDate, Pageable pageable);

//	    @Query("SELECT t FROM TrnParcelOut t WHERE " +
//		           "LOWER(t.senderName) LIKE LOWER(CONCAT('%', :namePart1, '%')) OR " +
//		           "LOWER(t.senderName) LIKE LOWER(CONCAT('%', :namePart2, '%')) OR " +
//		           "LOWER(t.senderName) LIKE LOWER(CONCAT('%', :namePart3, '%')) OR " +
//		           "LOWER(t.senderName) LIKE LOWER(CONCAT('%', :namePart4, '%'))")
//		    List<TrnParcelOut> findBySenderNameParts(
//		            @Param("namePart1") String namePart1,
//		            @Param("namePart2") String namePart2,
//		            @Param("namePart3") String namePart3,
//		            @Param("namePart4") String namePart4);
}
  