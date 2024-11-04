package com.iocl.Dispatch_Portal_Application.Controller;


import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iocl.Dispatch_Portal_Application.DTO.MstCourierContracHistoryResponse;
import com.iocl.Dispatch_Portal_Application.DTO.MstCourierContractDto;
import com.iocl.Dispatch_Portal_Application.Entity.MstCourierContract;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.MstCourierContractService;

@RestController
@RequestMapping("/api/courier-contracts")
public class MstCourierContractController {
	
	private MstCourierContractService mstCourierContractService;
	
	   public MstCourierContractController(MstCourierContractService mstCourierContractService){
	        this.mstCourierContractService = mstCourierContractService;
	    }
  
	   
	   @PostMapping
     public ResponseEntity<String> createContract(@RequestBody MstCourierContractDto request,HttpServletRequest httpRequest){
	   
	  return mstCourierContractService.createContract(request,httpRequest);
	   
   }
   
	   @PutMapping("/{courierContNo}")
	    public ResponseEntity<String> updateCourierContract(@PathVariable String courierContNo, @RequestBody MstCourierContractDto request,HttpServletRequest httpRequest) {
	    
	        return mstCourierContractService.updateContract(courierContNo,request,httpRequest);
	    }
	   
	   @DeleteMapping("/{courierContNo}")
	  public ResponseEntity<String> deleteContract( @PathVariable String courierContNo,HttpServletRequest httpRequest){
		  
		return mstCourierContractService.deleteContract(courierContNo,httpRequest);
		  
	  }
	   
	   
	   @GetMapping("/history")
	    public ResponseEntity<List<MstCourierContracHistoryResponse>> getAllContractsBasedOnLocCodeAndStatus(HttpServletRequest request) {
	        // Call the service to get contracts
	        List<MstCourierContracHistoryResponse> contracts = mstCourierContractService.getAllContractsBasedOnLocCodeAndStatus(request);
	        return ResponseEntity.ok(contracts);
	    }
	   @GetMapping("/active-history")
	   public ResponseEntity<List<MstCourierContracHistoryResponse>> getAllActiveContractsBasedOnLocCode(HttpServletRequest request) {
	       // Call the service to get active contracts
	       List<MstCourierContracHistoryResponse> contracts = mstCourierContractService.getAllActiveContractsBasedOnLocCode(request);
	       return ResponseEntity.ok(contracts);
	   }

	   @GetMapping("/{courierContNo}")
	   public ResponseEntity<List<MstCourierContracHistoryResponse>> getContractBasedOnLocCodeCourierContNo(@PathVariable String courierContNo, HttpServletRequest request) {
	       // Call the service to get active contracts
	       List<MstCourierContracHistoryResponse> contracts = mstCourierContractService.getContractBasedOnLocCodeCourierContNo(courierContNo,request);
	       return ResponseEntity.ok(contracts);
	   }
	}
 
	


