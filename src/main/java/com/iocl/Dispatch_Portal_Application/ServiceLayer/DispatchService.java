package com.iocl.Dispatch_Portal_Application.ServiceLayer;


import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.iocl.Dispatch_Portal_Application.Entity.MstEmployee;
import com.iocl.Dispatch_Portal_Application.Entity.MstUser;
import com.iocl.Dispatch_Portal_Application.Entity.OtpEntity;
import com.iocl.Dispatch_Portal_Application.Entity.RefSequence;
import com.iocl.Dispatch_Portal_Application.Repositaries.MstUserRepository;
import com.iocl.Dispatch_Portal_Application.Repositaries.OtpRepository;
import com.iocl.Dispatch_Portal_Application.Repositaries.RefSequenceRepository;
import com.iocl.Dispatch_Portal_Application.Security.JwtUtils;
@Service
public class DispatchService {

    @Autowired
    private OtpRepository otpRepository;
    
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MstUserRepository mstUserRepository;

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private RefSequenceRepository refSequenceRepository;

     
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private HttpClient httpClient;
    
    private static final Logger logger = LoggerFactory.getLogger(DispatchService.class);


    public ResponseEntity<?> sendOtp(Long mobileNumber) throws IOException, URISyntaxException, InterruptedException {
        Optional<MstUser> userOptional = mstUserRepository.findByMobileNumber(mobileNumber);

        if (userOptional.isPresent()) {
            MstUser user = userOptional.get();
            SendOtp(user);
            logger.info("OTP sent successfully to user with ID: {}", user.getUserId());
            return ResponseEntity.ok().body("OTP sent");
 
        } else {
            logger.warn("User not found for mobile number: {}", mobileNumber);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found for mobile number");
            // return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
    }

    public void SendOtp(MstUser user) throws IOException, URISyntaxException, InterruptedException {
    	
    	
    	 Optional<MstUser> userOptional = mstUserRepository.findByMobileNumber(user.getMobileNumber());
         if (userOptional.isPresent()) {
             MstUser usertest = userOptional.get();
             String userIdTrimmed = usertest.getUserId().trim();

             Optional<MstEmployee> employeeOpt = employeeService.getEmployeeById(Integer.parseInt(userIdTrimmed));
             if (employeeOpt.isPresent()) {
                 if (!"DISPATCH".equalsIgnoreCase(user.getRoleId())) {
                     logger.warn("User with role '{}' is not allowed to log in", user.getRoleId());
                 }
             }
    	 
    	 logger.info("Generating OTP for user ID: {}", user.getUserId());
    	// String mobileNumber = "9390617401";
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmpCode(user.getUserId());
        otpEntity.setOtp(ThreadLocalRandom.current().nextInt(100000, 1000000));
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpEntity.setExpirationAt(otpEntity.getCreatedAt().plusMinutes(10));
        otpRepository.save(otpEntity);

        String message = "OTP for Dispatch Application login is " + otpEntity.getOtp() + ", which expires in 120 seconds -IndianOil.";
        String url = "https://sandesh.indianoil.co.in/sandesh/smsPrioOutMessageRequest/priooutgoingSMS";
        //https://spandan.indianoil.co.in/sandesh/smsOutMessageRequest/outgoingSMS
        //https://spandan.indianoil.co.in/sandesh/smsPrioOutMessageRequest/priooutgoingSMS
        URL urlObj = new URL(url.trim());

        JSONArray ja = new JSONArray();
        JSONObject obj = new JSONObject();
      //  String formattedMobileNumber = "+91" + user.getMobileNumber();

        obj.put("mobile_no", String.valueOf(user.getMobileNumber()));
        obj.put("sms_content", message.toString());
        obj.put("gst_flag", "1");
        obj.put("template_id", "11071672132574427106");
        obj.put("ref_in_msg_unique_id", "11071672132574427106");
        ja.put(obj);
       
       
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(ja.toString()))
                .uri(urlObj.toURI())
                .setHeader("authenticationToken", "1085779bb2f0935b728713bc33e7d0ab")
                .setHeader("Content-Type", "application/json")
                .setHeader("appId", "GatePass")
              //  .setHeader("appId", "DispatchApp")
                .setHeader("appCategory", "GatePass_OTP")
               // .setHeader("appCategory", "Dispatch_OTP")
                .setHeader("msgType", "ENG")
                .build();
        request.headers().map().forEach((k, v) -> logger.info("Header: {} = {}", k, v));

        logger.info("Request URI: {}", request.uri());
        logger.info("Request headers: {}", request.headers());
        logger.info("Request body: {}", ja.toString());
        logger.info("Sending SMS request: {}", request);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("SMS API response status code: {}", response.statusCode());

        logger.info("SMS API response: {}", response.body());

        if (response.statusCode() == 200) {
            logger.info("OTP successfully generated and sent for user ID: {}", user.getUserId());
        } else {
            logger.error("Failed to send OTP. SMS API responded with status code: {}", response.statusCode());
        }
             }

    }


    
    public ResponseEntity<?> verifyOtpAndGenerateJwt(Long mobileNumber, int otp, HttpServletResponse httpresponse) {

        logger.info("Verifying OTP for mobile number: {}", mobileNumber);

        Optional<MstUser> userOptional = mstUserRepository.findByMobileNumber(mobileNumber);
        if (userOptional.isPresent()) {
            MstUser user = userOptional.get();
            String userIdTrimmed = user.getUserId().trim();

            Optional<MstEmployee> employeeOpt = employeeService.getEmployeeById(Integer.parseInt(userIdTrimmed));
            if (employeeOpt.isPresent()) {
                if (!"DISPATCH".equalsIgnoreCase(user.getRoleId())) {
                    logger.warn("User with role '{}' is not allowed to log in", user.getRoleId());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authorized for OTP-based login");
                }

                // Find the latest OTP for the user
                OtpEntity otpEntity = otpRepository.findFirstByEmpCodeOrderByCreatedAtDesc(user.getUserId());
                if (otpEntity != null && otpEntity.getOtp() == otp) {
                    if (otpEntity.getExpirationAt().isAfter(LocalDateTime.now())) {
                        logger.info("OTP verified successfully for user ID: {}", user.getUserId());

                        Map<String, Object> claims = new HashMap<>();
                        claims.put("userId", user.getUserId());
                        claims.put("username", user.getUserName());
                        claims.put("Designation", user.getRoleId());
                        claims.put("locName", getLocName(user.getLocCode()));
                        claims.put("locCode", user.getLocCode());

                        String jwt = jwtUtils.generateTokenFromUsernameAndClaims(user.getUserId(), claims);
                        logger.info("JWT generated successfully for user ID: {}", user.getUserId());

                        // Check if the location is implemented
                        Optional<RefSequence> refSequenceOptional = refSequenceRepository.findById(user.getLocCode().trim());
                        if (refSequenceOptional.isEmpty()) {
                            logger.warn("Location code '{}' is not implemented in the ref_sequence table", user.getLocCode());
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Application not implemented for the location");
                        }

                        ResponseCookie jwtCookie = jwtUtils.createJwtCookie(jwt);
                        httpresponse.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

                        // Create the response map
                        Map<String, Object> responseMap = new LinkedHashMap<>();
                        responseMap.put("EmpName", user.getUserName());
                        responseMap.put("Designation", getDesignation(Integer.parseInt(userIdTrimmed)));
                        responseMap.put("Role", user.getRoleId());
                        responseMap.put("locationName", getLocName(user.getLocCode()));

                        return ResponseEntity.ok(responseMap);
                    } else {
                        logger.warn("OTP expired for user ID: {}", user.getUserId());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OTP expired");
                    }
                } else {
                    logger.warn("Invalid OTP '{}' provided for user ID: {}", otp, user.getUserId());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid Employee Code For User");
            }
        } else {
            logger.warn("User with mobile number {} not found", mobileNumber);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }


    private String getLocName(String locCode) {
        TypedQuery<String> query = entityManager.createQuery(
                "SELECT l.locName FROM MstLocation l WHERE l.locCode = :locCode",
                String.class
        );
        query.setParameter("locCode", locCode);
        return query.getSingleResult();
    }
   

    private String getDesignation(int userId) {
        TypedQuery<String> query = entityManager.createQuery(
                "SELECT e.designation FROM MstEmployee e WHERE e.empCode = :userId",
                String.class
        );
        query.setParameter("userId", userId);
        return query.getSingleResult();
    }

    }