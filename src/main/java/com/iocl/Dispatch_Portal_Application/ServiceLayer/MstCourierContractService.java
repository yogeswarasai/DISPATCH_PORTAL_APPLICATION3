package com.iocl.Dispatch_Portal_Application.ServiceLayer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.iocl.Dispatch_Portal_Application.DTO.MstCourierContracHistoryResponse;
import com.iocl.Dispatch_Portal_Application.DTO.MstCourierContractDiscountDTO;
import com.iocl.Dispatch_Portal_Application.DTO.MstCourierContractDto;
import com.iocl.Dispatch_Portal_Application.DTO.MstCourierContractRateDto;
import com.iocl.Dispatch_Portal_Application.Entity.MstCourierContract;
import com.iocl.Dispatch_Portal_Application.Entity.MstCourierContractDiscount;
import com.iocl.Dispatch_Portal_Application.Entity.MstCourierContractRate;
import com.iocl.Dispatch_Portal_Application.Repositaries.MstCourierContractDiscountRepository;
import com.iocl.Dispatch_Portal_Application.Repositaries.MstCourierContractRateRepository;
import com.iocl.Dispatch_Portal_Application.Repositaries.MstCourierContractRepository;
import com.iocl.Dispatch_Portal_Application.Security.JwtUtils;
import com.iocl.Dispatch_Portal_Application.composite_pk.MstCourierContractDiscountId;
import com.iocl.Dispatch_Portal_Application.composite_pk.MstCourierContractPK;

@Service
public class MstCourierContractService {
	
	private static final Logger logger=LoggerFactory.getLogger(MstCourierContractService.class);
	
	private MstCourierContractRepository mstCourierContractRepository;
	private MstCourierContractDiscountRepository mstCourierContractDiscountRepository;
	 private MstCourierContractRateRepository mstCourierContractRateRepository;
     private JwtUtils jwtUtils;
     private final ModelMapper modelMapper;


	public MstCourierContractService(MstCourierContractRepository mstCourierContractRepository,
			MstCourierContractDiscountRepository mstCourierContractDiscountRepository,
			MstCourierContractRateRepository mstCourierContractRateRepository, JwtUtils jwtUtils,
			ModelMapper modelMapper) {
		super();
		this.mstCourierContractRepository = mstCourierContractRepository;
		this.mstCourierContractDiscountRepository = mstCourierContractDiscountRepository;
		this.mstCourierContractRateRepository = mstCourierContractRateRepository;
		this.jwtUtils = jwtUtils;
		this.modelMapper = modelMapper;
	}


	public ResponseEntity<String> createContract(MstCourierContractDto request, HttpServletRequest httpRequest) {
		logger.debug("Received MstCourierContract Details:" +request);
		
		    String token = jwtUtils.getJwtFromCookies(httpRequest);
	        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
	        String username = jwtUtils.getUserNameFromJwtToken(token);		
		//String locCode="4400";
		String courierContNo=request.getCourierContNo();
		try {
            MstCourierContractPK contractPK = new MstCourierContractPK();
            contractPK.setLocCode(locCode);
            contractPK.setCourierContNo(courierContNo);

            // Check if the contract already exists
            Optional<MstCourierContract> existingContract = mstCourierContractRepository.findById(contractPK);
            if (existingContract.isPresent()) {
                logger.info("Courier contract with locCode {} and courierContNo {} already exists", 
                		locCode, courierContNo);
                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body("Contract already exists.");
            } else {
                // Process new contract logic
                MstCourierContract newContract = new MstCourierContract();
                newContract.setLocCode(locCode);
                newContract.setCourierContNo(courierContNo);
                newContract.setContractStartDate(request.getContractStartDate());
                newContract.setContractEndDate(request.getContractEndDate());
                newContract.setCourierCode(request.getCourierCode());
                newContract.setStatus("A"); 
                newContract.setCreatedBy(username);
                newContract.setCreatedDate(LocalDate.now());
                newContract.setLastUpdatedDate(null);
                mstCourierContractRepository.save(newContract);
              if(request.getCourierDiscounts()!=null)  {
            	  for (MstCourierContractDiscountDTO discountDTO : request.getCourierDiscounts()) {
					
            		  
            		  MstCourierContractDiscount discount=new MstCourierContractDiscount();
                      discount.setLocCode(locCode);
                      discount.setCourierContNo(courierContNo);
                      discount.setToMonthlyAmt(discountDTO.getToMonthlyAmt());
                      discount.setFromMonthlyAmt(discountDTO.getFromMonthlyAmt());
                      discount.setDiscountPercentage(discountDTO.getDiscountPercentage());
                      discount.setStatus("A");
                      discount.setCreatedBy(username);
                      discount.setCreatedDate(LocalDate.now());
                      
                      mstCourierContractDiscountRepository.save(discount);
				}
              
              }
              else{
            	  return ResponseEntity.status(HttpStatus.NOT_FOUND).body("discount object is not there");
              }
              
              if(request.getCourierRates() != null) {
            	    for (MstCourierContractRateDto ratesDto : request.getCourierRates()) {
            	        logger.debug("Received rate DTO: {}", ratesDto); // Log DTO values

            	        MstCourierContractRate courierRate = new MstCourierContractRate();
            	        courierRate.setLocCode(locCode);
            	        courierRate.setCourierContNo(courierContNo);
            	        courierRate.setFromWtGms(ratesDto.getFromWtGms());
            	        courierRate.setToWtGms(ratesDto.getToWtGms());
            	        courierRate.setFromDistanceKm(ratesDto.getFromDistanceKm());
            	        courierRate.setToDistanceKm(ratesDto.getToDistanceKm());
            	        courierRate.setRate(ratesDto.getRate());
            	        courierRate.setStatus("A");
            	        courierRate.setCreatedBy(username);
            	        courierRate.setCreatedDate(LocalDate.now());

            	        logger.debug("Saving courier rate: {}", courierRate); // Log before saving
            	        mstCourierContractRateRepository.save(courierRate);
            	    }
            	} else {
            	    logger.error("Courier rates are null");
            	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rates object is not present.");
            	}

           
                logger.info("Courier contract with locCode {} and courierContNo {} has been successfully created", 
                		locCode, courierContNo);
                return ResponseEntity.status(HttpStatus.CREATED).body("Contract processed successfully.");
            }
        } catch (Exception e) {
            logger.error("An error occurred while processing the courier contract", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("An error occurred. Please try again.");
        }
		
	}


	public ResponseEntity<String> updateContract( String courierContNo, MstCourierContractDto request,HttpServletRequest httpRequest) {
	    logger.info("Updating MstCourierContract Details: " + request);
	    logger.info("Updating MstCourierContract Details: " + request.getCourierDiscounts());
	    String token = jwtUtils.getJwtFromCookies(httpRequest);
        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
          String  userid =jwtUtils.getUserNameFromJwtToken(token);

	    try {
	        MstCourierContractPK contractPK = new MstCourierContractPK();
	        contractPK.setLocCode(locCode);
	        contractPK.setCourierContNo(courierContNo);

	        // Fetch the existing contract
	        Optional<MstCourierContract> existingContract = mstCourierContractRepository.findById(contractPK);
	        if (existingContract.isPresent()) {
	            MstCourierContract contract = existingContract.get();
	            
	          
	            contract.setLocCode(locCode);
	            contract.setCourierContNo(courierContNo);
	            contract.setContractStartDate(request.getContractStartDate());
	            contract.setContractEndDate(request.getContractEndDate());
	            contract.setLastUpdatedDate(LocalDateTime.now());

	            // Save the contract (without modifying the primary key)
	            mstCourierContractRepository.save(contract);
	            logger.info(" BEFORE IF CONDITION");
	            
	            // Update discount and rates logic (as before)
	            if (request.getCourierDiscounts() != null) {
	           
	            	  logger.info(" AFTER IF CONDITION");
	                
	                List<MstCourierContractDiscount> existingDiscount = 
	                    mstCourierContractDiscountRepository.findByLocCodeAndCourierContNo(locCode.trim(),courierContNo.trim());
//	                logger.info("recieved existing discount  {}"+existingDiscount.toString());


	                if (!existingDiscount.isEmpty()) {
		                logger.info(" existing discount object prasent and enter into if condition ");
		                
		                // Delete the old discount record
	                    mstCourierContractDiscountRepository.deleteAll(existingDiscount);
	                }
	                
	                for (MstCourierContractDiscountDTO discountDTO : request.getCourierDiscounts()) {
	                	MstCourierContractDiscount newDiscount = new MstCourierContractDiscount();

	                    newDiscount.setLocCode(locCode.trim());
	                   newDiscount.setCourierContNo(courierContNo.trim());
	                  newDiscount.setFromMonthlyAmt(discountDTO.getFromMonthlyAmt());
	                  newDiscount.setToMonthlyAmt(discountDTO.getToMonthlyAmt());
	                    newDiscount.setDiscountPercentage(discountDTO.getDiscountPercentage());
	                    newDiscount.setCreatedBy(userid);
	                    newDiscount.setStatus("A");
	                    newDiscount.setCreatedDate(LocalDate.now());

	     	                    mstCourierContractDiscountRepository.save(newDiscount);
					}
	                    

	                }
	               
	            
	            logger.info(" BEFORE IF CONDITION OF RATES");
	            if (request.getCourierRates() != null) {
	            	 logger.info(" AFTER IF CONDITION OF RATES");
	                // Delete all the existing rates for the given locCode and courierContNo ONCE
	                List<MstCourierContractRate> existingRates = 
	                    mstCourierContractRateRepository.findByLocCodeAndCourierContNo(locCode.trim(), courierContNo.trim());

	                if (!existingRates.isEmpty()) {
	                    mstCourierContractRateRepository.deleteAll(existingRates);
	                }

	                // Now insert the list of new rates
	                for (MstCourierContractRateDto ratesDto : request.getCourierRates()) {
	                    MstCourierContractRate rate = new MstCourierContractRate();
	                    rate.setLocCode(locCode);
	                    rate.setCourierContNo(courierContNo);

	                    rate.setFromWtGms(ratesDto.getFromWtGms());
	                    rate.setToWtGms(ratesDto.getToWtGms());
	                    rate.setFromDistanceKm(ratesDto.getFromDistanceKm());
	                    rate.setToDistanceKm(ratesDto.getToDistanceKm());
	                    rate.setRate(ratesDto.getRate());
	                    rate.setCreatedBy(userid);
	                    rate.setStatus("A");
	                    rate.setCreatedDate(LocalDate.now());

	                    // Save each rate in the loop
	                    mstCourierContractRateRepository.save(rate);
	                }
	            }


	            logger.info("Courier contract with locCode {} and courierContNo {} has been updated successfully", 
	                        httpRequest, courierContNo);
	            return ResponseEntity.status(HttpStatus.OK).body("Contract updated successfully.");
	        } else {
	            logger.info("Courier contract with locCode {} and courierContNo {} not found for update", 
	                        httpRequest, courierContNo);
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contract not found.");
	        }
	    } catch (Exception e) {
	        logger.error("An error occurred while updating the courier contract", e);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("An error occurred. Please try again.");
	    }
	}



	public ResponseEntity<String> deleteContract(String courierContNo, HttpServletRequest httpRequest) {
	    String token = jwtUtils.getJwtFromCookies(httpRequest);
	    String locCode = jwtUtils.getLocCodeFromJwtToken(token);

	    try {
	        // Fetch the contract based on the location code and courier contract number
	        Optional<MstCourierContract> existingContract = 
	            mstCourierContractRepository.findByLocCodeAndCourierContNo(locCode, courierContNo);

	        if (existingContract.isPresent()) {
	            MstCourierContract mstCourierContract = existingContract.get();
	            mstCourierContract.setStatus("D"); // Set the status to "D"
	            mstCourierContract.setLastUpdatedDate(LocalDateTime.now());
	            mstCourierContractRepository.save(mstCourierContract); // Save the updated contract
	            
	            // Update the related discounts
	            List<MstCourierContractDiscount> existingDiscounts = 
	                mstCourierContractDiscountRepository.findByLocCodeAndCourierContNo(locCode, courierContNo);
	            for (MstCourierContractDiscount discount : existingDiscounts) {
	                discount.setStatus("D"); // Set the status to "D"
	                discount.setCreatedDate(LocalDate.now()); // Update created date if needed
	                mstCourierContractDiscountRepository.save(discount); // Save the updated discount
	            }

	            // Update the related rates
	            List<MstCourierContractRate> existingRates = 
	                mstCourierContractRateRepository.findByLocCodeAndCourierContNo(locCode, courierContNo);
	            for (MstCourierContractRate rate : existingRates) {
	                rate.setStatus("D"); // Set the status to "D"
	                rate.setCreatedDate(LocalDate.now()); // Update created date if needed
	                mstCourierContractRateRepository.save(rate); // Save the updated rate
	            }

	            return ResponseEntity.ok("Contract deactivated successfully.");
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contract not found.");
	        }
	    } catch (Exception e) {
	        // Log the error and return a user-friendly message
	        System.err.println("Error occurred while deactivating the contract: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("Error occurred while deactivating the contract.");
	    }
	}




	 public List<MstCourierContracHistoryResponse> getAllContractsBasedOnLocCodeAndStatus(HttpServletRequest request) {
	        String token = jwtUtils.getJwtFromCookies(request);
	        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
	        
	        ModelMapper modelMapper=new ModelMapper();
	        // Fetch contracts based on locCode
	        List<MstCourierContract> contracts = mstCourierContractRepository.findByLocCode(locCode);
	        
	        List<MstCourierContracHistoryResponse> contractHistoryResponses = new ArrayList<>();
	        
	        // Convert each contract to the DTO
	        for (MstCourierContract contract : contracts) {
	            MstCourierContracHistoryResponse dto = modelMapper.map(contract, MstCourierContracHistoryResponse.class);
	            
	            // Map discounts
	            List<MstCourierContractDiscountDTO> discountDTOs = new ArrayList<>();
	            for (MstCourierContractDiscount discount : contract.getDiscounts()) {
	                MstCourierContractDiscountDTO discountDTO = modelMapper.map(discount, MstCourierContractDiscountDTO.class);
	                discountDTOs.add(discountDTO);
	            }
	            dto.setDiscounts(discountDTOs);
	            
	            // Map rates
	            List<MstCourierContractRateDto> rateDTOs = new ArrayList<>();
	            for (MstCourierContractRate rate : contract.getRates()) {
	                MstCourierContractRateDto rateDTO = modelMapper.map(rate, MstCourierContractRateDto.class);
	                rateDTOs.add(rateDTO);
	            }
	            dto.setRates(rateDTOs);
	            
	            contractHistoryResponses.add(dto);
	        }

	        return contractHistoryResponses;
	    }

	public List<MstCourierContracHistoryResponse> getAllActiveContractsBasedOnLocCode(HttpServletRequest request) {
	    String token = jwtUtils.getJwtFromCookies(request);
	    String locCode = jwtUtils.getLocCodeFromJwtToken(token);
	    
	    ModelMapper modelMapper = new ModelMapper();
	    // Fetch only active contracts based on locCode
	    List<MstCourierContract> contracts = mstCourierContractRepository.findByLocCodeAndStatus(locCode, "A");
	    
	    List<MstCourierContracHistoryResponse> contractHistoryResponses = new ArrayList<>();
	    
	    // Convert each contract to the DTO
	    for (MstCourierContract contract : contracts) {
	        MstCourierContracHistoryResponse dto = modelMapper.map(contract, MstCourierContracHistoryResponse.class);
	        
	        // Map discounts
	        List<MstCourierContractDiscountDTO> discountDTOs = new ArrayList<>();
	        for (MstCourierContractDiscount discount : contract.getDiscounts()) {
	            MstCourierContractDiscountDTO discountDTO = modelMapper.map(discount, MstCourierContractDiscountDTO.class);
	            discountDTOs.add(discountDTO);
	        }
	        dto.setDiscounts(discountDTOs);
	        
	        // Map rates
	        List<MstCourierContractRateDto> rateDTOs = new ArrayList<>();
	        for (MstCourierContractRate rate : contract.getRates()) {
	            MstCourierContractRateDto rateDTO = modelMapper.map(rate, MstCourierContractRateDto.class);
	            rateDTOs.add(rateDTO);
	        }
	        dto.setRates(rateDTOs);
	        
	        contractHistoryResponses.add(dto);
	    }

	    return contractHistoryResponses;
	}



	    
	    public List<MstCourierContracHistoryResponse> getContractBasedOnLocCodeCourierContNo(String courierContNo, HttpServletRequest request) {
	        String token = jwtUtils.getJwtFromCookies(request);
	        String locCode = jwtUtils.getLocCodeFromJwtToken(token);

	        Optional<MstCourierContract> contracts = mstCourierContractRepository.findByLocCodeAndCourierContNo(locCode, courierContNo);

	        return contracts.stream().map(contract -> {
	            MstCourierContracHistoryResponse response = modelMapper.map(contract, MstCourierContracHistoryResponse.class);

	            // Ensure all fields are mapped
	            response.setCourierContNo(contract.getCourierContNo());
	            response.setStatus(contract.getStatus());
	            response.setCreatedBy(contract.getCreatedBy());
	            response.setCourierCode(contract.getCourierCode());
	            response.setContractStartDate(contract.getContractStartDate());
	            response.setContractEndDate(contract.getContractEndDate());
	            response.setCreatedDate(contract.getCreatedDate());
	            response.setLastUpdatedDate(contract.getLastUpdatedDate());

	            // Populate discounts
	            List<MstCourierContractDiscountDTO> discounts = mstCourierContractDiscountRepository.findByLocCodeAndCourierContNo(locCode, courierContNo)
	                    .stream()
	                    .map(this::convertToDiscountDTO)
	                    .collect(Collectors.toList());
	            response.setDiscounts(discounts);

	            // Populate rates
	            List<MstCourierContractRateDto> rates = mstCourierContractRateRepository.findByLocCodeAndCourierContNo(locCode, courierContNo)
	                    .stream()
	                    .map(this::convertToRateDTO)
	                    .collect(Collectors.toList());
	            response.setRates(rates);

	            return response;
	        }).collect(Collectors.toList());
	    }

	    private MstCourierContractDiscountDTO convertToDiscountDTO(MstCourierContractDiscount discount) {
	        return modelMapper.map(discount, MstCourierContractDiscountDTO.class);
	    }

	    private MstCourierContractRateDto convertToRateDTO(MstCourierContractRate rate) {
	        return modelMapper.map(rate, MstCourierContractRateDto.class);
	    }
}
