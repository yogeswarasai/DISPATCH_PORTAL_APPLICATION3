package com.iocl.Dispatch_Portal_Application.Controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.OutputStream;
import java.lang.reflect.Field;

import com.iocl.Dispatch_Portal_Application.DTO.ParcelInDto;
import com.iocl.Dispatch_Portal_Application.DTO.ParcelOutDto;
import com.iocl.Dispatch_Portal_Application.Entity.MstCourier;
import com.iocl.Dispatch_Portal_Application.Entity.MstDepartment;
import com.iocl.Dispatch_Portal_Application.Entity.MstEmployee;
import com.iocl.Dispatch_Portal_Application.Entity.TrnParcelIn;
import com.iocl.Dispatch_Portal_Application.Entity.TrnParcelOut;
import com.iocl.Dispatch_Portal_Application.Security.JwtUtils;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.CustomFooter;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.DispatchService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.EmployeeService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.MstCourierService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.MstDepartmentService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.MstLocationService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.TrnParcelCountService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.TrnParcelInService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.TrnParcelOutService;
import com.itextpdf.text.DocumentException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

@RestController
@RequestMapping("/api/v1/dispatch")
public class DispatchController {

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private TrnParcelCountService trnparcelCountService;
    
	@Autowired
	 private TrnParcelInService trnparcelinService;
	
	@Autowired
	 private TrnParcelOutService trnparceloutService;
	
	@Autowired
	 private EmployeeService employeeService;
	
	@Autowired
	private MstCourierService mstCourierService;
	
	@Autowired
	private MstDepartmentService mstDepartmentService;
	
	@Autowired
	private MstLocationService mstLocationService;
	
   private static final Logger logger=LoggerFactory.getLogger(DispatchController.class);
    
//    @GetMapping("/profile")
//    public ResponseEntity<ProfileResponse> getProfile() {
//        return dispatchService.getProfile();
//    }

    @PostMapping("/sendotp")
    public ResponseEntity<?> sendOtp(@RequestParam Long mobileNumber) throws IOException, URISyntaxException, InterruptedException {
        return dispatchService.sendOtp(mobileNumber);
    }


    @PostMapping("/login")

    public ResponseEntity<?> login(@RequestParam Long mobileNumber, @RequestParam int otp, HttpServletResponse httpresponse) {

        return dispatchService.verifyOtpAndGenerateJwt(mobileNumber, otp, httpresponse);

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body("You've been signed out!");
    }
    

    @GetMapping("/totals")
    public ResponseEntity<Map<String, Object>> getDispatchTotals(HttpServletRequest request) {
        Map<String, Object> totals = new HashMap<>();

        // Get JWT token from cookies
        String token = jwtUtils.getJwtFromCookies(request);

        // Validate and extract locCode from the JWT token
        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
        if (locCode == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Total count of parcels dispatched (parcel out) for the given locCode
        long totalCountParcelOut = trnparcelCountService.totalCountParcelOutByLocCode(locCode);
        totals.put("ParcelOut", totalCountParcelOut);

        // Total count of parcels received (parcel in) for the given locCode
        long totalCountParcelIn = trnparcelCountService.totalCountParcelInByLocCode(locCode);
        totals.put("ParcelIn", totalCountParcelIn);

        // Total count of parcels in + parcels out for the given locCode
        long totalParcels = totalCountParcelIn + totalCountParcelOut;
        totals.put("totalParcels", totalParcels);

        return ResponseEntity.ok(totals);
    }

//    @GetMapping("/totalsByDate")
//    public ResponseEntity<?> getDispatchTotalsByDate(
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//        if (date == null) {
//            date = LocalDate.now(); // default to current date if parameter not present
//        }
//
//        // Use 'date' parameter for your business logic
//
//        Map<String, Object> totalsByDate = new HashMap<>();
//        // Example logic using date parameter
//        long totalCountParcelOutByDate = trnparcelCountService.totalCountParcelOutByDate(date);
//        totalsByDate.put("ParcelOut", totalCountParcelOutByDate);
//
//        long totalCountParcelInByDate = trnparcelCountService.totalCountParcelInByDate(date);
//        totalsByDate.put("ParcelIn", totalCountParcelInByDate);
//
//        long totalParcelsByDate = totalCountParcelInByDate + totalCountParcelOutByDate;
//        totalsByDate.put("totalParcels", totalParcelsByDate);
//
//        return ResponseEntity.ok(totalsByDate);
//    }
    
    @GetMapping("/totalsByDate")
    public ResponseEntity<?> getDispatchTotalsByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {
        
        // Default to current date if the date parameter is not provided
        if (date == null) {
            date = LocalDate.now();
        }

        // Extract locCode from JWT token
        String token = jwtUtils.getJwtFromCookies(request);
        String locCode = jwtUtils.getLocCodeFromJwtToken(token);

        if (locCode == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or locCode not found.");
        }

        // Fetch parcel totals by date and locCode
        Map<String, Object> totalsByDate = trnparcelCountService.getTotalsByDateAndLocCode(date, locCode);

        return ResponseEntity.ok(totalsByDate);
    }
   

    
    @GetMapping("/history/all")
    public ResponseEntity<?> getDispatchHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam String type,
            @RequestParam(defaultValue = "false") boolean exportPdf,
    @RequestParam(defaultValue = "false") boolean exportExcel,
            @RequestParam(required = false) String senderLocCode,
            @RequestParam(required = false) String senderDepartment,
            @RequestParam(required = false) String consignmentNumber,
            HttpServletRequest request,
            HttpServletResponse response) {
    	

        logger.info("Received request to fetch dispatch history. Type: {}, FromDate: {}, ToDate: {}, ExportPdf: {}", type, fromDate, toDate, exportPdf);

        String token = jwtUtils.getJwtFromCookies(request);
        String locCode = jwtUtils.getLocCodeFromJwtToken(token);

        if (locCode == null) {
            logger.warn("Invalid token or locCode not found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or locCode not found.");
        }

        try {

        	if ("in".equalsIgnoreCase(type)) {
        	    logger.info("Fetching IN dispatch history for locCode: {}", locCode);
        	    List<ParcelInDto> parcelsIn = trnparcelinService.findByDateRangeAndLocCode(fromDate, toDate, locCode);

        	    // Log size before filtering
        	    logger.info("Retrieved {} parcels before filtering.", parcelsIn.size());

        	    // Filter by senderLocCode
        	    if (senderLocCode != null) {
        	        logger.info("Applying filter: senderLocCode = {}", senderLocCode);
        	        parcelsIn = parcelsIn.stream()
        	                .filter(parcel -> senderLocCode.equalsIgnoreCase(parcel.getSenderLocCode()))
        	                .collect(Collectors.toList());
        	    }

        	    // Filter by senderDepartment
        	    if (senderDepartment != null) {
        	        logger.info("Applying filter: senderDepartment = {}", senderDepartment);
        	        parcelsIn = parcelsIn.stream()
        	                .filter(parcel -> senderDepartment.equals(parcel.getSenderDepartment()))
        	                .collect(Collectors.toList());
        	    }
        	    
        	    if(consignmentNumber !=null) {
        	    	
        	    	parcelsIn = parcelsIn.stream()
        	                .filter(parcel -> consignmentNumber.equalsIgnoreCase(parcel.getConsignmentNumber()))
        	                .collect(Collectors.toList());
        	    	
        	    }
        	    // Log size after filtering
        	    logger.info("Filtered parcels count: {}", parcelsIn.size());

        	    // Log data being sent to PDF generation
        	    parcelsIn.forEach(parcel -> logger.info("Filtered Parcel Data: {}", parcel));

        	    if (exportPdf) {
        	        logger.info("Generating PDF for filtered data...");
        	        generateInPdf(parcelsIn, response, fromDate, toDate, locCode);
        	        return ResponseEntity.ok().build();
        	    }
        	    
        	    try {
        	    	 response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        	            response.setHeader("Content-Disposition", "attachment; filename=dispatch_history_" + type + ".xlsx");
        	    	 if(exportExcel) {
        	    		 generateInExcel(parcelsIn, response, fromDate, toDate, locCode);
                  	 return ResponseEntity.ok().build();
                     }
        	    } catch (Exception e) {
        	        logger.error("Error generating Excel file", e);
        	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate Excel");
        	    }

              
    

        	    return ResponseEntity.ok(parcelsIn);
        	}
        	else if("out".equalsIgnoreCase(type)) {
        		
        		 List<ParcelOutDto> parcelsout = trnparceloutService.findByDateRangeAndLocCode(fromDate, toDate, locCode);
        		 logger.info("Retrieved {} parcels before filtering.", parcelsout.size());

         	    // Filter by senderLocCode
         	    if (senderLocCode != null) {
         	        logger.info("Applying filter: senderLocCode = {}", senderLocCode);
         	       parcelsout = parcelsout.stream()
         	                .filter(parcel -> senderLocCode.equalsIgnoreCase(parcel.getSenderLocCode()))
         	                .collect(Collectors.toList());
         	    }

         	    // Filter by senderDepartment
         	    if (senderDepartment != null) {
         	        logger.info("Applying filter: senderDepartment = {}", senderDepartment);
         	       parcelsout = parcelsout.stream()
         	                .filter(parcel -> senderDepartment.equals(parcel.getSenderDepartment()))
         	                .collect(Collectors.toList());
         	    }
         	    
         	    if(consignmentNumber !=null) {
         	    	
         	    	parcelsout = parcelsout.stream()
         	                .filter(parcel -> consignmentNumber.equalsIgnoreCase(parcel.getConsignmentNumber()))
         	                .collect(Collectors.toList());
         	    	
         	    }

         	    // Log size after filtering
         	    logger.info("Filtered parcels count: {}", parcelsout.size());

         	    // Log data being sent to PDF generation
         	   parcelsout.forEach(parcel -> logger.info("Filtered Parcel Data: {}", parcel));

         	    if (exportPdf) {
         	        logger.info("Generating PDF for filtered data...");
         	       generateOutPdf(parcelsout, response, fromDate, toDate, locCode);
         	        return ResponseEntity.ok().build();
         	    }
         	   if(exportExcel) {
         		  generateOutExcel(parcelsout, response, fromDate, toDate, locCode);
            	   return ResponseEntity.ok().build();
               }

         	    return ResponseEntity.ok(parcelsout);
        	}
        
        
        	else {
                logger.warn("Invalid parcel type received: {}", type);
                return ResponseEntity.badRequest().body("Invalid parcel type. Must be 'in' or 'out'.");
            }
        } catch (Exception e) {
            logger.error("An error occurred while fetching dispatch history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }



    private void generateInExcel(List<ParcelInDto> parcelsIn, HttpServletResponse response, LocalDate fromDate, LocalDate toDate, String locCode) throws IOException {
        // Set content type for Excel
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=dispatch_history_in.xlsx");

        // Create Excel workbook and sheet
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Dispatch History IN");

            // Create styles
            CellStyle titleStyle = createCellStyle(workbook, true, 14);
            CellStyle subtitleStyle = createCellStyle(workbook, false, 12);
            CellStyle headerStyle = createCellStyle(workbook, true, 12);
            CellStyle dataStyle = createCellStyle(workbook, false, 11);

            // Row 1: Title
            Row titleRow = sheet.createRow(0);
            createMergedCell(sheet, titleRow, 0, 7, "Dispatch IN History ", titleStyle);

            // Row 2: Date Range
            Row dateRow = sheet.createRow(1);
            String dateRangeMessage = "From Date: " + fromDate.toString() + " To Date: " + toDate.toString();
            createMergedCell(sheet, dateRow, 0, 7, dateRangeMessage, titleStyle);
            
            String locationname_with_code;
            if (locCode != null && !locCode.trim().isEmpty()) {
            	locCode = locCode.trim();
                String senderLocName = mstLocationService.getLocNameByCode(locCode);
                locationname_with_code=(senderLocName != null && !senderLocName.trim().isEmpty()
                    ? senderLocName + " (" + locCode + ")"
                  //  : "Unknown Location (" + senderLocCode + ")"
                    		 :locCode
                		);
            } else {
            	locationname_with_code="unknow location";
            }
            
            // Row 3: Location Code
            Row locCodeRow = sheet.createRow(2);
            createMergedCell(sheet, locCodeRow, 0, 7, locationname_with_code, titleStyle);

            // Row 4: Header
            Row headerRow = sheet.createRow(3);
            createCell(headerRow, 0, "Sender Location", headerStyle, sheet);
            createCell(headerRow, 1, "Sender Department", headerStyle, sheet);
            createCell(headerRow, 2, "Sender Name", headerStyle, sheet);
            createCell(headerRow, 3, "Recipient Department", headerStyle, sheet);
            createCell(headerRow, 4, "Recipient Name", headerStyle, sheet);
            createCell(headerRow, 5, "Consignment DateTime", headerStyle, sheet);
            createCell(headerRow, 6, "Consignment Number", headerStyle, sheet);
            createCell(headerRow, 7, "Created Date", headerStyle, sheet);

            // Write data rows
            int rowIdx = 4;
            for (ParcelInDto parcel : parcelsIn) {
                Row dataRow = sheet.createRow(rowIdx++);
                createCell(dataRow, 0, parcel.getSenderLocCode(), dataStyle, sheet);
                createCell(dataRow, 1, parcel.getSenderDepartment(), dataStyle, sheet);
                createCell(dataRow, 2, parcel.getSenderName(), dataStyle, sheet);
                createCell(dataRow, 3, parcel.getRecipientDepartment(), dataStyle, sheet);
                createCell(dataRow, 4, parcel.getRecipientName(), dataStyle, sheet);
                createCell(dataRow, 5, parcel.getConsignmentDate() != null ? parcel.getConsignmentDate().toString() : "", dataStyle, sheet);
                createCell(dataRow, 6, parcel.getConsignmentNumber(), dataStyle, sheet);
                createCell(dataRow, 7, parcel.getCreatedDate() != null ? parcel.getCreatedDate().toString() : "", dataStyle, sheet);
            }

            // Auto-size columns after writing all data
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write workbook to response output stream
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            } catch (IOException e) {
                throw new IOException("Error writing Excel to response output stream", e);
            }
        }
    }

    // Helper method to create a merged cell
    private void createMergedCell(XSSFSheet sheet, Row row, int startCol, int endCol, String value, CellStyle style) {
        Cell cell = row.createCell(startCol);
        cell.setCellValue(value);
        cell.setCellStyle(style);

        // Merge cells across columns
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), startCol, endCol));

        // Apply the style to the merged region
        for (int i = startCol + 1; i <= endCol; i++) {
            Cell emptyCell = row.createCell(i);
            emptyCell.setCellStyle(style);
        }
    }

    // Helper method to create a cell with specific style
    private void createCell(Row row, int colIndex, String value, CellStyle style, XSSFSheet sheet) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    // Helper method to create a cell style
    private CellStyle createCellStyle(XSSFWorkbook workbook, boolean bold, int fontSize) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(bold);
        font.setFontHeight(fontSize);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }


    private void generateOutExcel(List<ParcelOutDto> parcelsOut, HttpServletResponse response, LocalDate fromDate, LocalDate toDate, String locCode) throws IOException {
        // Set content type and header for Excel file
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=dispatch_history_out.xlsx");

        // Create the workbook and sheet
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Dispatch History OUT");

            // Create cell styles
            CellStyle titleStyle = createCellStyle(workbook, true, 14);
            CellStyle subtitleStyle = createCellStyle(workbook, false, 12);
            CellStyle headerStyle = createCellStyle(workbook, true, 12);
            CellStyle dataStyle = createCellStyle(workbook, false, 11);

            // Row 1: Title
            Row titleRow = sheet.createRow(0);
            createMergedCell(sheet, titleRow, 0, 10, "Dispatch OUT History", titleStyle);

            // Row 2: Date Range
            Row dateRow = sheet.createRow(1);
            String dateRangeMessage = "From Date: " + fromDate.toString() + " To Date: " + toDate.toString();
            createMergedCell(sheet, dateRow, 0, 10, dateRangeMessage, titleStyle);

            String locationname_with_code;
            if (locCode != null && !locCode.trim().isEmpty()) {
            	locCode = locCode.trim();
                String senderLocName = mstLocationService.getLocNameByCode(locCode);
                locationname_with_code=(senderLocName != null && !senderLocName.trim().isEmpty()
                    ? senderLocName + " (" + locCode + ")"
                  //  : "Unknown Location (" + senderLocCode + ")"
                    		 :locCode
                		);
            } else {
            	locationname_with_code="unknow location";
            }
            
            // Row 3: Location Name with Code
       //     String locationNameWithCode = getLocationNameWithCode(locCode);
            Row locCodeRow = sheet.createRow(2);
            createMergedCell(sheet, locCodeRow, 0, 10, locationname_with_code, titleStyle);

            // Row 4: Header Row
            Row headerRow = sheet.createRow(3);
           
            createCell(headerRow, 0, "Sender Department", headerStyle, sheet);
            createCell(headerRow, 1, "Sender Name", headerStyle, sheet);
            createCell(headerRow, 2, "Recipient Location", headerStyle, sheet);
            createCell(headerRow, 3, "Recipient Department", headerStyle, sheet);
            createCell(headerRow, 4, "Recipient Name", headerStyle, sheet);
            createCell(headerRow, 5, "Consignment DateTime", headerStyle, sheet);
            createCell(headerRow, 6, "Consignment Number", headerStyle, sheet);
            createCell(headerRow, 7, "Courier name", headerStyle, sheet);
            createCell(headerRow, 8, "weight", headerStyle, sheet);
            createCell(headerRow, 9, "Distance", headerStyle, sheet);
            createCell(headerRow, 10, "Created Date", headerStyle, sheet);

            // Row 5 and beyond: Data Rows
            int rowIdx = 4;
            for (ParcelOutDto parcel : parcelsOut) {
                Row dataRow = sheet.createRow(rowIdx++);
                
                createCell(dataRow, 0, parcel.getSenderDepartment(), dataStyle, sheet);
                createCell(dataRow, 1, parcel.getSenderName(), dataStyle, sheet);
                createCell(dataRow, 2, parcel.getRecipientLocCode(), dataStyle, sheet);
                createCell(dataRow, 3, parcel.getRecipientDepartment(), dataStyle, sheet);
                createCell(dataRow, 4, parcel.getRecipientName(), dataStyle, sheet);
                createCell(dataRow, 5, parcel.getConsignmentDate() != null ? parcel.getConsignmentDate().toString() : "", dataStyle, sheet);
                createCell(dataRow, 6, parcel.getConsignmentNumber(), dataStyle, sheet);
                createCell(dataRow, 7, parcel.getCourierName(), dataStyle, sheet);
                createCell(dataRow, 8, parcel.getWeight().toString(), dataStyle, sheet);
                createCell(dataRow, 9, parcel.getDistance().toString(), dataStyle, sheet);
                createCell(dataRow, 10, parcel.getCreatedDate() != null ? parcel.getCreatedDate().toString() : "", dataStyle, sheet);
            }

            // Auto-size all columns
            for (int i = 0; i < 13; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write workbook to response output stream
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            } catch (IOException e) {
                throw new IOException("Error writing Excel to response output stream", e);
            }
        }
    }





    private void generateInPdf(List<ParcelInDto> parcelsIn, HttpServletResponse response, LocalDate fromDate, LocalDate toDate, String locCode) throws Exception {
        logger.info("Generating PDF for IN dispatch history.");
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=dispatch_history_in.pdf");

        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate()); // Use A4 landscape for more space
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Add custom footer event
            String generatedText = String.format(
                "Generated from Despatch Application Dated: %s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yy h:mm a"))
            );
            String signatureText = "Signature";
            writer.setPageEvent(new CustomFooter(generatedText, signatureText));

            document.open();

            // Title: "Despatch Details-In" centered
            Paragraph title = new Paragraph("Despatch Details-In", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Date range: "fromDate to toDate" centered
            Paragraph dateRange = new Paragraph(String.format("%s to %s", fromDate, toDate), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            dateRange.setAlignment(Element.ALIGN_CENTER);
            document.add(dateRange);
             
            String locationname_with_code;
            if (locCode != null && !locCode.trim().isEmpty()) {
            	locCode = locCode.trim();
                String senderLocName = mstLocationService.getLocNameByCode(locCode);
                locationname_with_code=(senderLocName != null && !senderLocName.trim().isEmpty()
                    ? senderLocName + " (" + locCode + ")"
                  //  : "Unknown Location (" + senderLocCode + ")"
                    		 :locCode
                		);
            } else {
            	locationname_with_code="unknow location";
            }
            // Location code: "locCode" centered
            Paragraph locationCode = new Paragraph(locationname_with_code, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            locationCode.setAlignment(Element.ALIGN_CENTER);
            document.add(locationCode);

            document.add(new Paragraph("\n")); // Add some space before the table

            // Create table with 9 columns
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100); // Use full page width
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{7, 7, 7, 7, 7, 7, 7, 7, 7}); // Adjust column widths

            // Add table headers
            Stream.of("Sender Location", "Sender Department", "Sender Name", "Recipient Department", 
                      "Recipient Name", "Consignment DateTime", "Consignment Number", 
                      "Courier Name", "created Date")
                  .forEach(headerTitle -> {
                      PdfPCell header = new PdfPCell();
                      header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                      header.setBorderWidth(2);
                      header.setPhrase(new Phrase(headerTitle, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                      header.setHorizontalAlignment(Element.ALIGN_CENTER);
                      table.addCell(header);
                  });

            // Populate table rows
            for (ParcelInDto parcel : parcelsIn) {
                table.addCell(new Phrase(parcel.getSenderLocCode()));
                table.addCell(new Phrase(parcel.getSenderDepartment()));
                table.addCell(new Phrase(parcel.getSenderName()));
                table.addCell(new Phrase(parcel.getRecipientDepartment()));
                table.addCell(new Phrase(parcel.getRecipientName()));
                table.addCell(new Phrase(parcel.getConsignmentDate().toString()));
                table.addCell(new Phrase(parcel.getConsignmentNumber()));
                table.addCell(new Phrase(parcel.getCourierName()));
                table.addCell(new Phrase(parcel.getCreatedDate().toString()));
            }

            document.add(table);
            document.close();
            logger.info("PDF for IN dispatch history generated successfully.");
        }
    }
   
    private void generateOutPdf(List<ParcelOutDto> parcelsout, HttpServletResponse response, LocalDate fromDate, LocalDate toDate, String locCode) throws Exception {
        logger.info("Generating PDF for OUT dispatch history.");
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=dispatch_history_out.pdf");

        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate()); // Use A4 landscape for more space
            PdfWriter writer =PdfWriter.getInstance(document, out);
            
            // Add custom footer event
            String generatedText = String.format(
                "Generated from Despatch Application Dated: %s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yy h:mm a"))
            );
            String signatureText = "Signature";
            writer.setPageEvent(new CustomFooter(generatedText, signatureText));
            document.open();
            
            Paragraph title = new Paragraph("Despatch Details-OUT", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            // Date range: "fromDate to toDate" centered
            Paragraph dateRange = new Paragraph(String.format("%s to %s", fromDate, toDate), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            dateRange.setAlignment(Element.ALIGN_CENTER);
            document.add(dateRange);
            
            String locationname_with_code;
            if (locCode != null && !locCode.trim().isEmpty()) {
            	locCode = locCode.trim();
                String senderLocName = mstLocationService.getLocNameByCode(locCode);
                locationname_with_code=(senderLocName != null && !senderLocName.trim().isEmpty()
                    ? senderLocName + " (" + locCode + ")"
                  //  : "Unknown Location (" + senderLocCode + ")"
                    		 :locCode
                		);
            } else {
            	locationname_with_code="unknow location";
            }
            // Location code: "locCode" centered
            Paragraph locationCode = new Paragraph(locationname_with_code, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            locationCode.setAlignment(Element.ALIGN_CENTER);
            document.add(locationCode);

            document.add(new Paragraph("\n")); // Add some space before the table

            PdfPTable table = new PdfPTable(11); // Adjust column count to match data
            table.setWidthPercentage(100); // Use full page width
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{9, 12, 15, 15, 15, 15, 15, 14, 9, 8, 9}); // Adjust column widths
            
            Stream.of("Sender Department", "Sender Name", "Recipient Loc Code",
                    "Recipient Department", "Recipient Name","Consignment date","Consignment Number", "Courier Name", "Weight","Distance", "Created Date" )
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setPadding(2);
                    header.setPaddingTop(5);
                    header.setPaddingBottom(5);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });

            // Populate table rows
            for (ParcelOutDto parcel : parcelsout) {
              //  table.addCell(new Phrase(parcel.getSenderLocCode()));
            	 table.addCell(new Phrase(parcel.getSenderDepartment()));
                 table.addCell(new Phrase(parcel.getSenderName()));
                 table.addCell(new Phrase(parcel.getRecipientLocCode()));
                 table.addCell(new Phrase(parcel.getRecipientDepartment()));
                 table.addCell(new Phrase(parcel.getRecipientName()));
                 table.addCell(new Phrase(String.valueOf(parcel.getConsignmentDate())));
                table.addCell(new Phrase(parcel.getConsignmentNumber()));
                table.addCell(new Phrase(parcel.getCourierName()));
                table.addCell(new Phrase(String.valueOf(parcel.getWeight())));
            //    table.addCell(new Phrase(parcel.getUnit()));
                table.addCell(new Phrase(String.valueOf(parcel.getDistance())));
                table.addCell(new Phrase(parcel.getCreatedDate().toString()));
               
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.close();
            logger.info("PDF for OUT dispatch history generated successfully.");
        }
    }



    
//    @GetMapping("/daily/monthly/report")
//    public ResponseEntity<?> getDispatchHistory(
//            @RequestParam String reportType,  // daily or monthly
//            @RequestParam String type,        // in or out
//            HttpServletRequest request) {
//
//        // Extract locCode from JWT token
//        String token = jwtUtils.getJwtFromCookies(request);
//        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
//
//        if (locCode == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or locCode not found.");
//        }
//
//        LocalDate fromDate;
//        LocalDate toDate;
//
//        // Logic for daily and monthly reports
//        if ("daily".equalsIgnoreCase(reportType)) {
//            // Daily report: Use current date for both fromDate and toDate
//            fromDate = LocalDate.now();
//            toDate = LocalDate.now();
//        } else if ("monthly".equalsIgnoreCase(reportType)) {
//            // Monthly report: Use the first and last days of the current month
//            fromDate = LocalDate.now().withDayOfMonth(1);  // First day of the month
//            toDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());  // Last day of the month
//        } else {
//            return ResponseEntity.badRequest().body("Invalid report type. Must be 'daily' or 'monthly'.");
//        }
//
//        // Logic for parcel type "in" or "out"
//        if ("in".equalsIgnoreCase(type)) {
//            // Fetch parcels for the specified date range (daily or monthly)
//            List<TrnParcelIn> parcelsIn = trnparcelinService.findByDateRangeAndLocCode(fromDate, toDate, locCode);
//            return ResponseEntity.ok(parcelsIn);
//        } else if ("out".equalsIgnoreCase(type)) {
//            // Fetch parcels for the specified date range (daily or monthly)
//            List<TrnParcelOut> parcelsOut = trnparceloutService.findByDateRangeAndLocCode(fromDate, toDate, locCode);
//            return ResponseEntity.ok(parcelsOut);
//        } else {
//            return ResponseEntity.badRequest().body("Invalid parcel type. Must be 'in' or 'out'.");
//        }
//    }

    
//    @GetMapping("/daily/report")
//    public ResponseEntity<?> getDailyDispatchHistory(
//            @RequestParam String type,  // in or out
//            HttpServletRequest request) {
//
//        // Extract locCode from JWT token
//        String token = jwtUtils.getJwtFromCookies(request);
//        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
//
//        if (locCode == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or locCode not found.");
//        }
//
//        // Daily report: Use current date for both fromDate and toDate
//        LocalDate fromDate = LocalDate.now();
//        LocalDate toDate = LocalDate.now();
//
//        // Logic for parcel type "in" or "out"
//        if ("in".equalsIgnoreCase(type)) {
//            // Fetch daily "in" parcels
//            List<TrnParcelIn> parcelsIn = trnparcelinService.findByDateRangeAndLocCode(fromDate, toDate, locCode);
//            return ResponseEntity.ok(parcelsIn);
//        } else if ("out".equalsIgnoreCase(type)) {
//            // Fetch daily "out" parcels
//            List<TrnParcelOut> parcelsOut = trnparceloutService.findByDateRangeAndLocCode(fromDate, toDate, locCode);
//            return ResponseEntity.ok(parcelsOut);
//        } else {
//            return ResponseEntity.badRequest().body("Invalid parcel type. Must be 'in' or 'out'.");
//        }
//    }

//    @GetMapping("/monthly/report")
//    public ResponseEntity<?> getMonthlyDispatchHistory(
//            @RequestParam String type,  // in or out
//            HttpServletRequest request) {
//
//        // Extract locCode from JWT token
//        String token = jwtUtils.getJwtFromCookies(request);
//        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
//
//        if (locCode == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or locCode not found.");
//        }
//
//        // Monthly report: Use the first and last days of the current month
//        LocalDate fromDate = LocalDate.now().withDayOfMonth(1);  // First day of the month
//        LocalDate toDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());  // Last day of the month
//
//        // Logic for parcel type "in" or "out"
//        if ("in".equalsIgnoreCase(type)) {
//            // Fetch monthly "in" parcels
//            List<TrnParcelIn> parcelsIn = trnparcelinService.findByDateRangeAndLocCode(fromDate, toDate, locCode);
//            return ResponseEntity.ok(parcelsIn);
//        } else if ("out".equalsIgnoreCase(type)) {
//            // Fetch monthly "out" parcels
//            List<TrnParcelOut> parcelsOut = trnparceloutService.findByDateRangeAndLocCode(fromDate, toDate, locCode);
//            return ResponseEntity.ok(parcelsOut);
//        } else {
//            return ResponseEntity.badRequest().body("Invalid parcel type. Must be 'in' or 'out'.");
//        }
//    }
//    
    
//    @GetMapping("/monthly/report")
//    public ResponseEntity<?> getMonthlyDispatchHistory(
//            @RequestParam(required = false) Integer month,  // optional, defaults to current month if not provided
//            @RequestParam(required = false) Integer year, 
//            @RequestParam String type, // optional, defaults to current year if not provided
//            HttpServletRequest request) {
//
//        // Extract locCode from JWT token
//        String token = jwtUtils.getJwtFromCookies(request);
//        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
//
//        if (locCode == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or locCode not found.");
//        }
//
//        // Use current month and year if they are not provided
//        LocalDate now = LocalDate.now();
//        int selectedMonth = (month != null) ? month : now.getMonthValue();
//        int selectedYear = (year != null) ? year : now.getYear();
//
//        // Validate month range
//        if (selectedMonth < 1 || selectedMonth > 12) {
//            return ResponseEntity.badRequest().body("Invalid month. Must be between 1 and 12.");
//        }
//
//        // Validate year range (you can adjust the range as per your application's needs)
//        if (selectedYear < 2000 || selectedYear > now.getYear()) {
//            return ResponseEntity.badRequest().body("Invalid year. Must be between 2000 and the current year.");
//        }
//
//        // Set the first and last day of the selected month
//        LocalDate fromDate = LocalDate.of(selectedYear, selectedMonth, 1);
//        LocalDate toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());  // Last day of the selected month
//
//        // Logic for parcel type "in" or "out"
//        if ("in".equalsIgnoreCase(type)) {
//            // Fetch monthly "in" parcels
//            List<TrnParcelIn> parcelsIn = trnparcelinService.findByDateRangeAndLocCode(fromDate, toDate, locCode);
//            return ResponseEntity.ok(parcelsIn);
//        } else if ("out".equalsIgnoreCase(type)) {
//            // Fetch monthly "out" parcels
//            List<TrnParcelOut> parcelsOut = trnparceloutService.findByDateRangeAndLocCode(fromDate, toDate, locCode);
//            return ResponseEntity.ok(parcelsOut);
//        } else {
//            return ResponseEntity.badRequest().body("Invalid parcel type. Must be 'in' or 'out'.");
//        }
//    }

    
    @GetMapping("/names/{dept}")
    public ResponseEntity<List<String>> getEmployeeNamesByLoc(HttpServletRequest request , @PathVariable String dept)
    {
    	  String token = jwtUtils.getJwtFromCookies(request);
    	  
	        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
	        
	        List<String> employeeNames=employeeService.getEmployeeNamesByLoc(locCode,dept);
             return employeeNames.isEmpty()?ResponseEntity.notFound().build() :ResponseEntity.ok(employeeNames);

    }
    
    @GetMapping("/names/by/all-loc")
    public ResponseEntity<List<String>> getAllEmployeeNames() {
        List<String> employeeNames = employeeService.getAllEmployeeNames();
        return employeeNames.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(employeeNames);
    }
    
//    @GetMapping("/courier-names")
//    public ResponseEntity<List<String>> getAllCourierNames() {
//        List<MstCourier> couriers = mstCourierService.findAll();
//        List<String> courierNames = couriers.stream()
//                                            .map(MstCourier::getCourierName) // Assuming getCourierName() is the method to get the name
//                                            .collect(Collectors.toList());
//        return ResponseEntity.ok(courierNames);
//    }
    
    @GetMapping("/departments-by-loccode")
    public ResponseEntity<List<String>> getDepartmentNames(HttpServletRequest request) {
        String token = jwtUtils.getJwtFromCookies(request);
        String locCode = jwtUtils.getLocCodeFromJwtToken(token);

        List<MstDepartment> departments = mstDepartmentService.getDepartmentsByLocationCode(locCode);
        List<String> departmentNames = departments.stream()
                                                  .map(MstDepartment::getDeptName) // Assuming getDeptName() is the method to get the department name
                                                  .collect(Collectors.toList());
        return ResponseEntity.ok(departmentNames);
    }

    
    @GetMapping("/locCodes")
    public ResponseEntity<List<String>> getAllLocationCodes() {
        List<String> locationCodes = mstLocationService.findAllLocationCodes();
        return ResponseEntity.ok(locationCodes);
    }
    
//    @GetMapping("/locNames")
//    public ResponseEntity<List<String>> getAllLocationNames() {
//        List<String> locationCodes = mstLocationService.findAllLocationNames();
//        return ResponseEntity.ok(locationCodes);
//    }
    
    
    @GetMapping("/locNames")
    public ResponseEntity<List<String>> getAllLocationNames() {
        List<String> locationNamesWithCodes = mstLocationService.findAllLocationNamesWithCodes();
        return ResponseEntity.ok(locationNamesWithCodes);
    }
    @GetMapping("/departments/by-code/{locCode}")
    public ResponseEntity<List<String>> getDepartmentsByLocationCode(@PathVariable String locCode) {
        List<MstDepartment> departments = mstDepartmentService.getDepartmentsByLocationCode(locCode);
        List<String> departmentNames = departments.stream()
                                                  .map(MstDepartment::getDeptName) // Assuming getDeptName() is the method to get the department name
                                                  .collect(Collectors.toList());
        return ResponseEntity.ok(departmentNames);
    }
    
    @GetMapping("/departments/by-name/{locName}")
    public ResponseEntity<List<String>> getDepartmentsByLocationName(@PathVariable String locName) {
        List<String> departmentNames = mstDepartmentService.getDepartmentsByLocationName(locName);
        return ResponseEntity.ok(departmentNames);
    }
    @GetMapping("/namesBy/{locName}/{psa}")
    public ResponseEntity<List<String>> getEmployeeNamesByPsaCode(@PathVariable String locName , @PathVariable String psa) {
        List<String> employeeNames = employeeService.getEmployeeNamesByPsaCode(locName , psa);
        return ResponseEntity.ok(employeeNames);
    }
   

}
