package com.iocl.Dispatch_Portal_Application.DTO;

import java.time.LocalDate;
import java.util.List;

public class MstCourierContractDto {
   private String courierCode;
   private String courierContNo;
   private LocalDate contractStartDate;
   private LocalDate contractEndDate;
   private List<MstCourierContractDiscountDTO> courierDiscounts;
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
//public List<MstCourierContractDiscountDTO> getCourierDiscount() {
//	return courierDiscounts;
//}
//public void setCourierDiscount(List<MstCourierContractDiscountDTO> courierDiscount) {
//	this.courierDiscounts = courierDiscount;
//}
public List<MstCourierContractRateDto> getCourierRates() {
	return courierRates;
}
public List<MstCourierContractDiscountDTO> getCourierDiscounts() {
	return courierDiscounts;
}
public void setCourierDiscounts(List<MstCourierContractDiscountDTO> courierDiscounts) {
	this.courierDiscounts = courierDiscounts;
}
public void setCourierRates(List<MstCourierContractRateDto> courierRates) {
	this.courierRates = courierRates;
}

    
}
