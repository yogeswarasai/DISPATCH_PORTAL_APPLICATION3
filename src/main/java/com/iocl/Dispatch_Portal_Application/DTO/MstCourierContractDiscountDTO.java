package com.iocl.Dispatch_Portal_Application.DTO;

public class MstCourierContractDiscountDTO{
   
    private Double fromMonthlyAmt;
    private Double toMonthlyAmt;
    private Double discountPercentage;
	
	public Double getFromMonthlyAmt() {
		return fromMonthlyAmt;
	}
	public void setFromMonthlyAmt(Double fromMonthlyAmt) {
		this.fromMonthlyAmt = fromMonthlyAmt;
	}
	public Double getToMonthlyAmt() {
		return toMonthlyAmt;
	}
	public void setToMonthlyAmt(Double toMonthlyAmt) {
		this.toMonthlyAmt = toMonthlyAmt;
	}
	public Double getDiscountPercentage() {
		return discountPercentage;
	}
	public void setDiscountPercentage(Double discountPercentage) {
		this.discountPercentage = discountPercentage;
	}
    
    
}
