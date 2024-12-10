package com.iocl.Dispatch_Portal_Application.ServiceLayer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.iocl.Dispatch_Portal_Application.DTO.ParcelInDto;
import com.iocl.Dispatch_Portal_Application.Entity.MstEmployee;
import com.iocl.Dispatch_Portal_Application.Entity.MstUser;
import com.iocl.Dispatch_Portal_Application.Entity.TrnParcelIn;
import com.iocl.Dispatch_Portal_Application.Repositaries.EmployeeRepository;
import com.iocl.Dispatch_Portal_Application.Repositaries.MstUserRepository;
import com.iocl.Dispatch_Portal_Application.Repositaries.TrnParcelInRepository;
import com.iocl.Dispatch_Portal_Application.Security.JwtUtils;
import com.iocl.Dispatch_Portal_Application.composite_pk.TrnParcelInPK;
import com.iocl.Dispatch_Portal_Application.modal.StatusCodeModal;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

//@SuppressWarnings("unused")
@Service
public class TrnParcelInService {

	@Autowired
    private TrnParcelInRepository trnParcelInRepository;
	 @Autowired
	    private MstUserRepository mstUserRepositary;

	    @Autowired
	    private EmployeeRepository employeeRepository;

	    @Autowired
	    private EmailService emailService;
	    
	    @Autowired
	    private JwtUtils jwtUtils;
	    
	    @Autowired
	    private MstLocationService mstLocationService;
	    
	    
	    private static final Logger logger = LoggerFactory.getLogger(TrnParcelInService.class);

    public List<TrnParcelIn> findAll() {
        return trnParcelInRepository.findAll();
    }

    public Optional<TrnParcelIn> findById(TrnParcelInPK id) {
        return trnParcelInRepository.findById(id);
    }
   
//    public ResponseEntity<?> createParcelIn(ParcelInDto parcelInRequest, HttpServletRequest request) throws IOException, URISyntaxException, InterruptedException {
//        
//        TrnParcelIn parcelIn = parcelInRequest.toTrnParcelIn();
//
//        String token = jwtUtils.getJwtFromCookies(request);
//
//        // Validate and extract information from the JWT token
//        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
//        String username = jwtUtils.getUserNameFromJwtToken(token);
//
//        if (locCode == null || username == null) {
//            StatusCodeModal statusCodeModal = new StatusCodeModal();
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(statusCodeModal);
//        }
//
//        parcelIn.setRecipientLocCode(locCode);
//        parcelIn.setCreatedBy(username);
//        parcelIn.setReceivedDate(LocalDate.now());
//        parcelIn.setCreatedDate(LocalDate.now());
//
//        if (trnParcelInRepository.existsByConsignmentNumber(parcelIn.getConsignmentNumber())) {
//            StatusCodeModal statusCodeModal = new StatusCodeModal();
//            statusCodeModal.setStatus("Consignment number already exists: " + parcelIn.getConsignmentNumber());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(statusCodeModal);
//        }
//
//        TrnParcelIn createdParcel = trnParcelInRepository.save(parcelIn);
//
//        StatusCodeModal statusCodeModal = new StatusCodeModal();
//        if (createdParcel != null) {
//            // Generate PDF
//            byte[] pdfBytes = generatePdf(createdParcel);
//
//            // Fetch employee email based on locCode and recipient's name
//            Optional<MstEmployee> recipientEmployeeOpt = employeeRepository.findByLocCodeAndEmpName(locCode, parcelIn.getRecipientName());
//            
//            if (recipientEmployeeOpt.isPresent()) {
//                MstEmployee recipientEmployee = recipientEmployeeOpt.get();
//        //        String email = "madanmohant@indianoil.in";
//              String email = "hbaishya@indianoil.in";
//               // String email = recipientEmployee.getEmailId();
//                
//                String name = recipientEmployee.getEmpName();
//
//                if (email != null) {
//                    // Send email with parcel details
//                    String subject = "Parcel Notification";
//                    String messageBody = "<p>Dear " + name + ",</p>" +
//                                         "<p>You have received a new parcel with Tracking ID: " + createdParcel.getConsignmentNumber() + ".</p>" +
//                                         "<p>Please find the details in the attached PDF.</p>" +
//                                         "<p>Best regards,<br>Indian Oil Corporation Limited</p>";
//
//                    emailService.sendEmail(email, subject, messageBody, pdfBytes);
//
//                    // Optionally send email using external mail API
//                  //  sendEmployeeEmail(recipientEmployeeOpt, createdParcel.getConsignmentNumber(), "create", "http://approval-link");
//                }
//            }
//
//            statusCodeModal.setStatus_code(HttpStatus.CREATED.value());
//            statusCodeModal.setStatus("Parcel created successfully with id: " + createdParcel.getConsignmentNumber());
//            return ResponseEntity.status(HttpStatus.CREATED).body(statusCodeModal);
//        } else {
//            statusCodeModal.setStatus_code(HttpStatus.BAD_REQUEST.value());
//            statusCodeModal.setStatus("Failed to create parcel. Please try again.");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(statusCodeModal);
//        }
//    }
//
//    // Email Sending Logic (Modified from your example)
//    public void sendEmployeeEmail(Optional<MstEmployee> employeeEntity, String consignmentNumber, String status, String link) throws IOException, URISyntaxException, InterruptedException {
//        JSONObject json = new JSONObject();
//        json.put("from", "gatepass@indianoil.in");
//
//        JSONObject recipient = new JSONObject();
//        recipient.put("recipientMailId", employeeEntity.get().getEmailId());
//        recipient.put("recipientType", "Employee");
//     //   recipient.put("recipientCode", addLeadingZeros(employeeEntity.get().getEmpCode()));
//
//        json.put("to", new JSONObject[]{recipient});
//        json.put("cc", JSONObject.NULL);
//        json.put("bcc", JSONObject.NULL);
//
//        if (status.equalsIgnoreCase("create")) {
//            json.put("subject", "Parcel Notification - Action Required");
//            json.put("body", "<html><body><h1>" + "Dear " + employeeEntity.get().getEmpName() +
//                    ", A new parcel with Consignment Number " + consignmentNumber + " is waiting for your approval. Click " + link + " to approve/reject. - IndianOil" +
//                    "</h1></body></html>");
//        }
//
//        json.put("applicationName", "Parcel Notification System");
//
//        String url = "https://webapp.indianoil.co.in/common-mail-api/transmit-mail/simple";
//        URL urlObj = new URL(url.trim());
//        HttpRequest request = HttpRequest.newBuilder()
//                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
//                .uri(urlObj.toURI())
//                .setHeader("Authorization", "Basic UkRCQ09NTU9OTUFJTDpNQUlMIzIzMQ==")
//                .setHeader("Content-Type", "application/json")
//                .build();
//
//       // HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//       // System.out.println(response);
//    }

    
    
//    public ResponseEntity<?> createParcelIn(ParcelInDto parcelInRequest, HttpServletRequest request) throws IOException {
//
//        TrnParcelIn parcelIn = parcelInRequest.toTrnParcelIn();
//
//        String token = jwtUtils.getJwtFromCookies(request);
//
//
//
//        // Validate and extract information from the JWT token
//
//        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
//
//        String username = jwtUtils.getUserNameFromJwtToken(token);
//
//
//
//        if (locCode == null || username == null) {
//
//            StatusCodeModal statusCodeModal = new StatusCodeModal();
//
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(statusCodeModal);
//
//        }
//
//
//
//        parcelIn.setRecipientLocCode(locCode);
//
//        parcelIn.setCreatedBy(username);
//
//        parcelIn.setReceivedDate(LocalDate.now());
//
//        parcelIn.setCreatedDate(LocalDate.now());
//
//
//
//        if (trnParcelInRepository.existsByConsignmentNumber(parcelIn.getConsignmentNumber())) {
//
//            StatusCodeModal statusCodeModal = new StatusCodeModal();
//
//            statusCodeModal.setStatus("Consignment number already exists: " + parcelIn.getConsignmentNumber());
//
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(statusCodeModal);
//
//        }
//
//
//
//        TrnParcelIn createdParcel = trnParcelInRepository.save(parcelIn);
//
//
//
//        StatusCodeModal statusCodeModal = new StatusCodeModal();
//
//        if (createdParcel != null) {
//
//            // Generate PDF
//
//            byte[] pdfBytes = generatePdf(createdParcel);
//
//
//
//            // Fetch employee email based on locCode and recipient's name
//
//            Optional<MstEmployee> recipientEmployeeOpt = employeeRepository.findByLocCodeAndEmpName(locCode, parcelIn.getRecipientName());
//
//            if (recipientEmployeeOpt.isPresent()) {
//
//                MstEmployee recipientEmployee = recipientEmployeeOpt.get();
//
//               // String email = recipientEmployee.getEmailId();
//
//             //   String email = "madanmohant@indianoil.in";
//                String email = "hbaishya@indianoil.in";
//
//             // String email = "kummarishireesha4025@gmail.com";
//
//                String name=recipientEmployee.getEmpName();
//
//                if (email != null) {                	
//
//                	String subject = "Parcel Notification";
//
//                	String messageBody = "<p>Dear " + name + ",</p>" +
//
//                            "<p>You have received a new parcel with Tracking ID: " + createdParcel.getConsignmentNumber() + ".</p>" +
//
//                            "<p>Please find the details in the attached PDF.</p>" +
//
//                            "<p>Best regards,<br>" +
//
//                            "Indian Oil Corporation Limited</p>";
//
//                    emailService.sendEmail(email, subject, messageBody, pdfBytes);
//
//                   // emailService.sendEmail(email, "Parcel Notification", "You have received a new parcel with Tracking ID: " + createdParcel.getConsignmentNumber(), pdfBytes);
//
//                }
//
//                           }
//
//            statusCodeModal.setStatus_code(HttpStatus.CREATED.value());
//
//            statusCodeModal.setStatus("Parcel created successfully with id: " + createdParcel.getConsignmentNumber());
//
//            return ResponseEntity.status(HttpStatus.CREATED).body(statusCodeModal);
//
//        } else {
//
//            statusCodeModal.setStatus_code(HttpStatus.BAD_REQUEST.value());
//
//            statusCodeModal.setStatus("Failed to create parcel. Please try again.");
//
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(statusCodeModal);
//
//        }
//
//    }
    
    
    
    
    public ResponseEntity<?> createParcelIn(ParcelInDto parcelInRequest, HttpServletRequest request) throws IOException {
        TrnParcelIn parcelIn = parcelInRequest.toTrnParcelIn();
        String token = jwtUtils.getJwtFromCookies(request);

        // Validate and extract information from the JWT token
        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
        String username = jwtUtils.getUserNameFromJwtToken(token);

        if (locCode == null || username == null) {
            StatusCodeModal statusCodeModal = new StatusCodeModal();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(statusCodeModal);
        }

        parcelIn.setRecipientLocCode(locCode);
        parcelIn.setCreatedBy(username);
        parcelIn.setReceivedDate(LocalDate.now());
        parcelIn.setCreatedDate(LocalDate.now());

        if (trnParcelInRepository.existsByConsignmentNumber(parcelIn.getConsignmentNumber())) {
            StatusCodeModal statusCodeModal = new StatusCodeModal();
            statusCodeModal.setStatus("Consignment number already exists: " + parcelIn.getConsignmentNumber());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(statusCodeModal);
        }

        TrnParcelIn createdParcel = trnParcelInRepository.save(parcelIn);

        StatusCodeModal statusCodeModal = new StatusCodeModal();
        if (createdParcel != null) {
            // Generate PDF
            byte[] pdfBytes = generatePdf(createdParcel);

            // Fetch employee email based on locCode and recipient's name
            Optional<MstEmployee> recipientEmployeeOpt = employeeRepository.findByLocCodeAndEmpName(locCode.trim(), parcelIn.getRecipientName());
          logger.info("details to before if: ");     
            if (recipientEmployeeOpt.isPresent()) {
                logger.info("detais after if");     

                MstEmployee recipientEmployee = recipientEmployeeOpt.get();
                logger.info("Recipient Employee Details: {}", recipientEmployee);

                String email = recipientEmployee.getEmailId();  // Fetch email from employee details
                String name = recipientEmployee.getEmpName(); 
                logger.info("Calling sendEmail method...Debug");  // Log before sending email

              if (email != null) {                	

              	String subject = "Parcel Notification";

              	String messageBody = "<p>Dear " + name + ",</p>" +

                          "<p>You have received a new parcel with Tracking ID: " + createdParcel.getConsignmentNumber() + ".</p>" +

                          "<p>Please find the details in the attached PDF.</p>" +

                          "<p>Best regards,<br>" +

                          "Indian Oil Corporation Limited</p>";

               //   emailService.sendEmail(email, subject, messageBody, pdfBytes);

                 // emailService.sendEmail(email, "Parcel Notification", "You have received a new parcel with Tracking ID: " + createdParcel.getConsignmentNumber(), pdfBytes);

              }

                         }

          statusCodeModal.setStatus_code(HttpStatus.CREATED.value());

          statusCodeModal.setStatus("Parcel created successfully with id: " + createdParcel.getConsignmentNumber());

          return ResponseEntity.status(HttpStatus.CREATED).body(statusCodeModal);

      } else {

          statusCodeModal.setStatus_code(HttpStatus.BAD_REQUEST.value());

          statusCodeModal.setStatus("Failed to create parcel. Please try again.");

          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(statusCodeModal);

      }

    }


    

    
    public ResponseEntity<StatusCodeModal> updateParcelIn(String recipientLocCode, Long inTrackingId, TrnParcelIn trnParcelIn) {
        TrnParcelInPK id = new TrnParcelInPK(recipientLocCode, inTrackingId);
        Optional<TrnParcelIn> parcelin = trnParcelInRepository.findById(id);
        if (!parcelin.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        TrnParcelIn updateparcelwith = parcelin.get();
        trnParcelIn.setRecipientLocCode(recipientLocCode);
        trnParcelIn.setInTrackingId(inTrackingId);
        trnParcelIn.setRecordStatus(updateparcelwith.getRecordStatus());
        trnParcelIn.setCreatedBy(updateparcelwith.getCreatedBy());
        trnParcelIn.setLastUpdatedDate(LocalDateTime.now());

        TrnParcelIn updatedParcel = trnParcelInRepository.save(trnParcelIn);

        StatusCodeModal statusCodeModal = new StatusCodeModal();
        if (updatedParcel != null) {
            statusCodeModal.setStatus_code(200);
            statusCodeModal.setStatus("Parcel updated successfully with id: " + updatedParcel.getInTrackingId());
        } else {
            statusCodeModal.setStatus_code(400);
            statusCodeModal.setStatus("Parcel update failed. Try again.");
        }

        return ResponseEntity.ok(statusCodeModal);
    }

    
    
    
//    public ResponseEntity<StatusCodeModal> deleteParcelIn(Long inTrackingId, HttpServletRequest request) {
//        // Get JWT token from cookies
//        String token = jwtUtils.getJwtFromCookies(request);
//
//        // Validate and extract location code from JWT token
//        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
//        if (locCode == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        TrnParcelInPK id = new TrnParcelInPK(locCode, inTrackingId);
//        Optional<TrnParcelIn> parcelOptional = trnParcelInRepository.findById(id);
//
//        if (parcelOptional.isPresent()) {
//            TrnParcelIn trnParcel = parcelOptional.get();
//            trnParcel.setRecordStatus("D");
//            trnParcelInRepository.save(trnParcel); // Use save() to update the record
//
//            // Generate PDF
//            byte[] pdfBytes;
//            try {
//                pdfBytes = generatePdf(trnParcel);
//            } catch (IOException e) {
//                e.printStackTrace();
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//            }
//
//            // Find loc_admin from user table with case-insensitive roleId
//            Optional<MstUser> locAdminUserOpt = mstUserRepositary.findByRoleIdAndLocCodeIgnoreCase("LOC_ADMIN", locCode);
//            
//            if (locAdminUserOpt.isPresent()) {
//                MstUser locAdminUser = locAdminUserOpt.get();
//                logger.info("Fetched role ID: {}", locAdminUser.getRoleId());
//                logger.info("Fetched location code: {}", locAdminUser.getLocCode());
//                // Remove leading zeroes from userId to match employee code format
//                String formattedEmpCode = removeLeadingZeros(locAdminUser.getUserId()).trim();
//                Optional<MstEmployee> locAdminEmpOpt = employeeRepository.findByEmpCode(Integer.parseInt(formattedEmpCode));
//                if (locAdminEmpOpt.isPresent()) {
//                    MstEmployee locAdmin = locAdminEmpOpt.get();
//                    String email = locAdmin.getEmailId();
//                    
//                    logger.info("Fetched email: {}", email);
//                    System.out.println(email); // For console output
//
//                   System.out.println(email);
//                    if (email != null) {
//                        try {
//                            emailService.sendEmail(email, "Parcel Status Changed", "The status of the parcel with Tracking ID " + inTrackingId + " has been changed to 'D'.", pdfBytes);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//            StatusCodeModal statusCodeModal = new StatusCodeModal();
//            statusCodeModal.setStatus_code(200);
//            statusCodeModal.setStatus("Parcel deleted successfully");
//
//            return ResponseEntity.ok(statusCodeModal);
//        }
//
//        return ResponseEntity.noContent().build();
//    }
    
    
 

    public ResponseEntity<StatusCodeModal> deleteParcelIn(Long inTrackingId, HttpServletRequest request) {
        Logger logger = LoggerFactory.getLogger(TrnParcelInService.class);

        // Get JWT token from cookies
        String token = jwtUtils.getJwtFromCookies(request);

        // Validate and extract location code from JWT token
        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
        logger.info("Extracted location code from JWT: {}", locCode); // Log the extracted location code
        if (locCode == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TrnParcelInPK id = new TrnParcelInPK(locCode, inTrackingId);
        Optional<TrnParcelIn> parcelOptional = trnParcelInRepository.findById(id);

        if (parcelOptional.isPresent()) {
            TrnParcelIn trnParcel = parcelOptional.get();
            trnParcel.setRecordStatus("D");
            trnParcel.setLastUpdatedDate(LocalDateTime.now());
            trnParcelInRepository.save(trnParcel); // Use save() to update the record

            // Generate PDF
            byte[] pdfBytes;
            try {
                pdfBytes = generatePdf(trnParcel);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // Find loc_admin from user table with case-insensitive roleId
            logger.info("Querying for user with role 'loc_admin' and location code '{}'", locCode);
            Optional<MstUser> locAdminUserOpt = mstUserRepositary.findByRoleIdAndLocCodeIgnoreCase("LOC_ADMIN", locCode.trim());
            
            if (locAdminUserOpt.isPresent()) {
                MstUser locAdminUser = locAdminUserOpt.get();

                // Log the role ID and location code
                logger.info("Fetched role ID: {}", locAdminUser.getRoleId());
                logger.info("Fetched location code: {}", locAdminUser.getLocCode());

                // Remove leading zeroes from userId to match employee code format
                String formattedEmpCode = removeLeadingZeros(locAdminUser.getUserId()).trim();
                logger.info("Formatted employee code: {}", formattedEmpCode);
                
                Optional<MstEmployee> locAdminEmpOpt = employeeRepository.findByEmpCode(Integer.parseInt(formattedEmpCode));
                
                if (locAdminEmpOpt.isPresent()) {
                    MstEmployee locAdmin = locAdminEmpOpt.get();
                    String email = locAdmin.getEmailId();
                    logger.info("Fetched email: {}", email);
                    System.out.println(email); // For console output

                    if (email != null) {
                        try {
//                            emailService.sendEmail(email, "Parcel Status Changed", "The status of the parcel with Tracking ID " + inTrackingId + " has been changed to 'D'.", pdfBytes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    logger.warn("No employee found with employee code: {}", formattedEmpCode);
                }
            } else {
                logger.warn("No user found with role 'loc_admin' and location code '{}'", locCode);
            }

            StatusCodeModal statusCodeModal = new StatusCodeModal();
            statusCodeModal.setStatus_code(200);
            statusCodeModal.setStatus("Parcel deleted successfully");

            return ResponseEntity.ok(statusCodeModal);
        }

        return ResponseEntity.noContent().build();
    }


    public String removeLeadingZeros(String id) {
        // Remove leading zeroes
        return id.replaceFirst("^0+(?!$)", "");
    }

    public byte[] generatePdf(TrnParcelIn parcel) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Initialize Document
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        try {
            // Initialize PdfWriter
            PdfWriter.getInstance(document, baos);

            // Open the document
            document.open();

            // Add content
            document.add(new Paragraph("Parcel Details"));
            document.add(new Paragraph("Tracking ID: " + parcel.getInTrackingId()));
            document.add(new Paragraph("Consignment Number: " + parcel.getConsignmentNumber()));
            document.add(new Paragraph("Consignment Date: " + parcel.getConsignmentDate()));
            document.add(new Paragraph("Sender: " + parcel.getSenderName() + " (" + parcel.getSenderDepartment() + ")"));
            document.add(new Paragraph("Recipient: " + parcel.getRecipientName() + " (" + parcel.getRecipientDepartment() + ")"));
            document.add(new Paragraph("Courier: " + parcel.getCourierName()));
            document.add(new Paragraph("Record Status: " + parcel.getRecordStatus()));
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            // Close the document
            document.close();
        }
        return baos.toByteArray();
    }

    
//    public List<TrnParcelIn> getparcelsByLocationCode(String recipientLocCode) {
//        return trnParcelInRepository.findByRecipientLocCodeOrderByInTrackingIdDesc(recipientLocCode);
//    }
    
    
    
//    public Page<TrnParcelIn> getparcelsByLocationCode(String recipientLocCode , Pageable pageable) {
//        return trnParcelInRepository.findByRecipientLocCodeOrderByInTrackingIdDesc(recipientLocCode ,pageable);
//    }
//    

    public Page<ParcelInDto> getparcelsByLocationCode(String recipientLocCode, Pageable pageable) {
        Page<TrnParcelIn> parcels = trnParcelInRepository.findByRecipientLocCodeOrderByInTrackingIdDesc(recipientLocCode, pageable);

        return parcels.map(parcel -> {
            ParcelInDto dto = new ParcelInDto();
            dto.setRecipientLocCode(parcel.getRecipientLocCode());
            dto.setInTrackingId(parcel.getInTrackingId());
            dto.setConsignmentNumber(parcel.getConsignmentNumber());
            dto.setConsignmentDate(parcel.getConsignmentDate());
            dto.setReceivedDate(parcel.getReceivedDate());
//            String senderLocCode = parcel.getSenderLocCode();
//            if (senderLocCode != null) {
//                senderLocCode = senderLocCode.trim();
//                String senderLocName = mstLocationService.getLocNameByCode(senderLocCode);
//                // Set formatted location information in senderLocCode
//                dto.setSenderLocCode(senderLocName + " (" + senderLocCode + ")");
//            } else {
//                // Handle the case where senderLocCode is null
//                dto.setSenderLocCode("Unknown Location");
//            }
            
            String senderLocCode = parcel.getSenderLocCode();
            if (senderLocCode != null && !senderLocCode.trim().isEmpty()) {
                senderLocCode = senderLocCode.trim();
                String senderLocName = mstLocationService.getLocNameByCode(senderLocCode);
                dto.setSenderLocCode(senderLocName != null && !senderLocName.trim().isEmpty()
                    ? senderLocName + " (" + senderLocCode + ")"
//                    : "Unknown Location (" + senderLocCode + ")"
                    		 :senderLocCode
                    );
            } else {
                dto.setSenderLocCode("Unknown Location");
            }

            dto.setSenderDepartment(parcel.getSenderDepartment());
            dto.setSenderName(parcel.getSenderName());
            dto.setRecipientDepartment(parcel.getRecipientDepartment());
            dto.setRecipientName(parcel.getRecipientName());
            dto.setCourierName(parcel.getCourierName());
            dto.setRecordStatus(parcel.getRecordStatus());
            dto.setCreatedBy(parcel.getCreatedBy());
            dto.setCreatedDate(parcel.getCreatedDate());
            dto.setLastUpdatedDate(parcel.getLastUpdatedDate());

         
        
            
    
            return dto;
        });
    }

    
    public Page<ParcelInDto> getParcelsByLocationCodeAndName(String locCode, String name, Pageable pageable) {
    	Page<TrnParcelIn> parcels = trnParcelInRepository.findByRecipientLocCodeAndRecipientNameOrderByInTrackingIdDesc(locCode, name, pageable);
	    

        return parcels.map(parcel -> {
            ParcelInDto dto = new ParcelInDto();
            dto.setRecipientLocCode(parcel.getRecipientLocCode());
            dto.setInTrackingId(parcel.getInTrackingId());
            dto.setConsignmentNumber(parcel.getConsignmentNumber());
            dto.setConsignmentDate(parcel.getConsignmentDate());
            dto.setReceivedDate(parcel.getReceivedDate());
//            String senderLocCode = parcel.getSenderLocCode();
//            if (senderLocCode != null) {
//                senderLocCode = senderLocCode.trim();
//                String senderLocName = mstLocationService.getLocNameByCode(senderLocCode);
//                // Set formatted location information in senderLocCode
//                dto.setSenderLocCode(senderLocName + " (" + senderLocCode + ")");
//            } else {
//                // Handle the case where senderLocCode is null
//                dto.setSenderLocCode("Unknown Location");
//            }
            
            String senderLocCode = parcel.getSenderLocCode();
            if (senderLocCode != null && !senderLocCode.trim().isEmpty()) {
                senderLocCode = senderLocCode.trim();
                String senderLocName = mstLocationService.getLocNameByCode(senderLocCode);
                dto.setSenderLocCode(senderLocName != null && !senderLocName.trim().isEmpty()
                    ? senderLocName + " (" + senderLocCode + ")"
               //     : "Unknown Location (" + senderLocCode + ")"
                    		 :senderLocCode 
                		);
            } else {
                dto.setSenderLocCode("Unknown Location");
            }

            dto.setSenderDepartment(parcel.getSenderDepartment());
            dto.setSenderName(parcel.getSenderName());
            dto.setRecipientDepartment(parcel.getRecipientDepartment());
            dto.setRecipientName(parcel.getRecipientName());
            dto.setCourierName(parcel.getCourierName());
            dto.setRecordStatus(parcel.getRecordStatus());
            dto.setCreatedBy(parcel.getCreatedBy());
            dto.setCreatedDate(parcel.getCreatedDate());
            dto.setLastUpdatedDate(parcel.getLastUpdatedDate());

        
            
    
            return dto;
        });
	}

	  public Page<ParcelInDto> getTodaysParcelsByLocationCodeAndName(String locCode, String name, Pageable pageable) {
	        LocalDate today = LocalDate.now();
	        Page<TrnParcelIn> parcels= trnParcelInRepository.findByRecipientLocCodeAndRecipientNameAndCreatedDateOrderByInTrackingIdDesc(locCode, name, today, pageable);
	        
	        return parcels.map(parcel -> {
	            ParcelInDto dto = new ParcelInDto();
	            dto.setRecipientLocCode(parcel.getRecipientLocCode());
	            dto.setInTrackingId(parcel.getInTrackingId());
	            dto.setConsignmentNumber(parcel.getConsignmentNumber());
	            dto.setConsignmentDate(parcel.getConsignmentDate());
	            dto.setReceivedDate(parcel.getReceivedDate());
//	            String senderLocCode = parcel.getSenderLocCode();
//	            if (senderLocCode != null) {
//	                senderLocCode = senderLocCode.trim();
//	                String senderLocName = mstLocationService.getLocNameByCode(senderLocCode);
//	                // Set formatted location information in senderLocCode
//	                dto.setSenderLocCode(senderLocName + " (" + senderLocCode + ")");
//	            } else {
//	                // Handle the case where senderLocCode is null
//	                dto.setSenderLocCode("Unknown Location");
//	            }
	            
	            String senderLocCode = parcel.getSenderLocCode();
	            if (senderLocCode != null && !senderLocCode.trim().isEmpty()) {
	                senderLocCode = senderLocCode.trim();
	                String senderLocName = mstLocationService.getLocNameByCode(senderLocCode);
	                dto.setSenderLocCode(senderLocName != null && !senderLocName.trim().isEmpty()
	                    ? senderLocName + " (" + senderLocCode + ")"
	                  //  : "Unknown Location (" + senderLocCode + ")"
	                    		 :senderLocCode
	                		);
	            } else {
	                dto.setSenderLocCode("Unknown Location");
	            }

	            dto.setSenderDepartment(parcel.getSenderDepartment());
	            dto.setSenderName(parcel.getSenderName());
	            dto.setRecipientDepartment(parcel.getRecipientDepartment());
	            dto.setRecipientName(parcel.getRecipientName());
	            dto.setCourierName(parcel.getCourierName());
	            dto.setRecordStatus(parcel.getRecordStatus());
	            dto.setCreatedBy(parcel.getCreatedBy());
	            dto.setCreatedDate(parcel.getCreatedDate());
	         
	            dto.setLastUpdatedDate(parcel.getLastUpdatedDate());

	            
	    
	            return dto;
	        });
	    }
//	  public List<TrnParcelIn> findByDateRange(LocalDate fromDate, LocalDate toDate) {
//	        return trnParcelInRepository.findByCreatedDateBetween(fromDate, toDate);
//	    }
//	  
//	  public List<TrnParcelIn> findByDateRangeAndLocCode(LocalDate fromDate, LocalDate toDate, String locCode) {
//	        return trnParcelInRepository.findByReceivedDateBetweenAndRecipientLocCodeOrderByInTrackingIdDesc(fromDate, toDate, locCode);
//	    }
	  public List<ParcelInDto> findByDateRangeAndLocCode(LocalDate fromDate, LocalDate toDate, String locCode) {
		    
		    // Fetch parcels from the repository
		    List<TrnParcelIn> parcels = trnParcelInRepository.findByReceivedDateBetweenAndRecipientLocCodeOrderByInTrackingIdDesc(
		            fromDate, toDate, locCode);
		    
		    // Map TrnParcelIn to ParcelInDto
		    return parcels.stream().map(parcel -> {
		        ParcelInDto dto = new ParcelInDto();
		        dto.setRecipientLocCode(parcel.getRecipientLocCode());
		        dto.setInTrackingId(parcel.getInTrackingId());
		        dto.setConsignmentNumber(parcel.getConsignmentNumber());
		        dto.setConsignmentDate(parcel.getConsignmentDate());
		        dto.setReceivedDate(parcel.getReceivedDate());
		        
		        String senderLocCode = parcel.getSenderLocCode();
//		        if (senderLocCode != null) {
//		            senderLocCode = senderLocCode.trim();
//		            String senderLocName = mstLocationService.getLocNameByCode(senderLocCode);
//		            dto.setSenderLocCode(senderLocName + " (" + senderLocCode + ")");
//		        } else {
//		            dto.setSenderLocCode("Unknown Location");
//		        }
		        if (senderLocCode != null && !senderLocCode.trim().isEmpty()) {
	                senderLocCode = senderLocCode.trim();
	                String senderLocName = mstLocationService.getLocNameByCode(senderLocCode);
	                dto.setSenderLocCode(senderLocName != null && !senderLocName.trim().isEmpty()
	                    ? senderLocName + " (" + senderLocCode + ")"
	                  //  : "Unknown Location (" + senderLocCode + ")"
	                    		 :senderLocCode
	                		);
	            } else {
	                dto.setSenderLocCode("Unknown Location");
	            }
		        
		        dto.setSenderDepartment(parcel.getSenderDepartment());
		        dto.setSenderName(parcel.getSenderName());
		        dto.setRecipientDepartment(parcel.getRecipientDepartment());
		        dto.setRecipientName(parcel.getRecipientName());
		        dto.setCourierName(parcel.getCourierName());
		        dto.setRecordStatus(parcel.getRecordStatus());
		        dto.setCreatedBy(parcel.getCreatedBy());
		        dto.setCreatedDate(parcel.getCreatedDate());
	            dto.setLastUpdatedDate(parcel.getLastUpdatedDate());


		        return dto;
		    }).collect(Collectors.toList());
		}


	  public List<ParcelInDto> findByDateRangeAndRecipientNameAndLocCode(LocalDate fromDate, LocalDate toDate, String recipientName, String recipientLocCode) {
		  List<TrnParcelIn> parcels= trnParcelInRepository.findByReceivedDateBetweenAndRecipientNameAndRecipientLocCode(fromDate, toDate, recipientName, recipientLocCode);
	        
	        return parcels.stream().map(parcel -> {
		        ParcelInDto dto = new ParcelInDto();
		        dto.setRecipientLocCode(parcel.getRecipientLocCode());
		        dto.setInTrackingId(parcel.getInTrackingId());
		        dto.setConsignmentNumber(parcel.getConsignmentNumber());
		        dto.setConsignmentDate(parcel.getConsignmentDate());
		        dto.setReceivedDate(parcel.getReceivedDate());
		        
		        String senderLocCode = parcel.getSenderLocCode();
//		        if (senderLocCode != null) {
//		            senderLocCode = senderLocCode.trim();
//		            String senderLocName = mstLocationService.getLocNameByCode(senderLocCode);
//		            dto.setSenderLocCode(senderLocName + " (" + senderLocCode + ")");
//		        } else {
//		            dto.setSenderLocCode("Unknown Location");
//		        }
		        if (senderLocCode != null && !senderLocCode.trim().isEmpty()) {
	                senderLocCode = senderLocCode.trim();
	                String senderLocName = mstLocationService.getLocNameByCode(senderLocCode);
	                dto.setSenderLocCode(senderLocName != null && !senderLocName.trim().isEmpty()
	                    ? senderLocName + " (" + senderLocCode + ")"
	                  //  : "Unknown Location (" + senderLocCode + ")"
	                    		 :senderLocCode
	                		);
	            } else {
	                dto.setSenderLocCode("Unknown Location");
	            }
		        
		        dto.setSenderDepartment(parcel.getSenderDepartment());
		        dto.setSenderName(parcel.getSenderName());
		        dto.setRecipientDepartment(parcel.getRecipientDepartment());
		        dto.setRecipientName(parcel.getRecipientName());
		        dto.setCourierName(parcel.getCourierName());
		        dto.setRecordStatus(parcel.getRecordStatus());
		        dto.setCreatedBy(parcel.getCreatedBy());
		        dto.setCreatedDate(parcel.getCreatedDate());
	            dto.setLastUpdatedDate(parcel.getLastUpdatedDate());


		        return dto;
		    }).collect(Collectors.toList());
	    }

		public Long fetchNextId() {
			
			return trnParcelInRepository.fetchNextId();

		}
		
//		 public List<TrnParcelIn> getParcelsByLocationCodeAndName(String locCode, String name) {
//		        return trnParcelInRepository.findByRecipientLocCodeAndRecipientName(locCode, name);
//		    }
		

		
//		public List<TrnParcelIn> getParcelsByName(String recipientName) {
//	        // Normalize the name
//	        String normalizedRecipientName = recipientName.trim().toLowerCase();
//
//	        // Split the name into parts
//	        String[] nameParts = normalizedRecipientName.split("\\s+");
//
//	        // Pass name parts to repository query
//	        return trnParcelInRepository.findByRecipientNameParts(
//	                nameParts.length > 0 ? nameParts[0] : "",
//	                nameParts.length > 1 ? nameParts[1] : "",
//	                nameParts.length > 2 ? nameParts[2] : "",
//	                nameParts.length > 3 ? nameParts[3] : "");
//	    }
}

