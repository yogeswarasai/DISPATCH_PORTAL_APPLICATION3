package com.iocl.Dispatch_Portal_Application.DTO;

import java.time.LocalDate;
import java.util.List;

public class MstCourierContractDto {
   private String courierCode;
   private String courierContNo;
   private LocalDate contractStartDate;
   private LocalDate contractEndDate;
   private List<MstCourierContractDiscountDTO> courierDiscount;
   private List<MstCourierContractRateDto> courierRates;
public String getCourierCode() {
	return courierCode;
}
public void setCourierCode(String courierCode) {
	this.courierCode = courierCode;
}
public String getCourierContNo() {
	return courierContNo;
}
public void setCourierContNo(String courierContNo) {
	this.courierContNo = courierContNo;
}
public LocalDate getContractStartDate() {
	return contractStartDate;
}
public void setContractStartDate(LocalDate contractStartDate) {
	this.contractStartDate = contractStartDate;
}
public LocalDate getContractEndDate() {
	return contractEndDate;
}
public void setContractEndDate(LocalDate contractEndDate) {
	this.contractEndDate = contractEndDate;
}
public List<MstCourierContractDiscountDTO> getCourierDiscount() {
	return courierDiscount;
}
public void setCourierDiscount(List<MstCourierContractDiscountDTO> courierDiscount) {
	this.courierDiscount = courierDiscount;
}
public List<MstCourierContractRateDto> getCourierRates() {
	return courierRates;
}
public void setCourierRates(List<MstCourierContractRateDto> courierRates) {
	this.courierRates = courierRates;
}

    
}
