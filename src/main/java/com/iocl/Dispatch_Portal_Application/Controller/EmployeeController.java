
package com.iocl.Dispatch_Portal_Application.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.iocl.Dispatch_Portal_Application.DTO.ParcelInDto;
import com.iocl.Dispatch_Portal_Application.DTO.ParcelOutDto;
import com.iocl.Dispatch_Portal_Application.Entity.MstEmployee;
import com.iocl.Dispatch_Portal_Application.Entity.TrnParcelIn;
import com.iocl.Dispatch_Portal_Application.Entity.TrnParcelOut;
import com.iocl.Dispatch_Portal_Application.Security.JwtUtils;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.EmployeeService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.MstLocationService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.MstUserService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.TrnParcelCountService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.TrnParcelInService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.TrnParcelOutService;
import com.iocl.Dispatch_Portal_Application.modal.CaptchaResponseData;
import com.iocl.Dispatch_Portal_Application.modal.EmployeeLoginModal;
import com.iocl.Dispatch_Portal_Application.modal.ProfileResponse;


@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private TrnParcelInService trnParcelInService;

    @Autowired
    private TrnParcelOutService trnParcelOutService;
    
    @Autowired
    private MstUserService mstuserService;
    
    
    @Autowired
    private MstLocationService mstLocationService;

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private TrnParcelCountService trnparcelCountService;
    
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        return employeeService.getEmployeeProfile();
    }
    
//    @GetMapping("/name/{id}")
//    public ResponseEntity<String> getEmployeeName(@PathVariable String id) {
//        String name = employeeService.giveEmployeeName(id);
//        return ResponseEntity.ok(name);
//    }
    
    @GetMapping
    public Optional<MstEmployee> getEmployeeById(HttpServletRequest request) {
        return employeeService.getEmployee(request);
    }


    @GetMapping("/{empCode}")
    public ResponseEntity<MstEmployee> getEmployeeById(@PathVariable int empCode) {
        Optional<MstEmployee> employee = employeeService.getEmployeeById(empCode);
        return employee.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    
    @GetMapping("/locNames")
    public ResponseEntity<List<String>> getAllLocationNames() {
        List<String> locationNamesWithCodes = mstLocationService.findAllLocationNamesWithCodes();
        return ResponseEntity.ok(locationNamesWithCodes);
    }
    
    
    @GetMapping("/empCodes")
    public ResponseEntity<List<String>> getAllEmployeeCodesWithZeros() {
        List<String> employeeCodes = employeeService.getAllEmployeeCodesWithZeros();
        return ResponseEntity.ok(employeeCodes);
    }
    
    @GetMapping("/locCodes")
    public ResponseEntity<List<String>> getAllLocationCodes() {
        List<String> locationCodes = mstLocationService.findAllLocationCodes();
        return ResponseEntity.ok(locationCodes);
    }
    
//    @GetMapping("/empCodesByLocCode")
//    public ResponseEntity<List<String>> getEmpCodesByLocCode(@RequestParam String locCode) {
//        List<String> empCodes = employeeService.getEmpCodesByLocCodes(locCode);
//        return ResponseEntity.ok(empCodes);
//    }
    
    @GetMapping("/empCodesByLocCode")
    public ResponseEntity<List<String>> getEmpCodesByLocCode(@RequestParam String locCode) {
        // Extract the locCode from within the parentheses if present
        if (locCode.contains("(") && locCode.contains(")")) {
            int startIdx = locCode.indexOf('(') + 1;
            int endIdx = locCode.indexOf(')');
            locCode = locCode.substring(startIdx, endIdx);
        }
        
        // Now pass only the extracted locCode to the service
        List<String> empCodes = employeeService.getEmpCodesByLocCodes(locCode);
        return ResponseEntity.ok(empCodes);
    }

    
    @GetMapping("/empCodesByloguserLocCode")
    public ResponseEntity<List<String>> getEmpCodesByLoggedInUserLocCode(HttpServletRequest request) {
        // Extract JWT from cookies and get locCode from the token
        String token = jwtUtils.getJwtFromCookies(request);
        String userLocCode = jwtUtils.getLocCodeFromJwtToken(token); // Assuming locCode is stored in the JWT token

        // Fetch empCodes based on the logged-in user's locCode
        List<String> empCodes = employeeService.getEmpCodesByLocCodes(userLocCode);

        return ResponseEntity.ok(empCodes);
    }

    @GetMapping("/userNameByUserId")
    public ResponseEntity<String> getUserNameByUserId(@RequestParam String userId) {
        String userName = employeeService.getUserNameByUserId(userId);
        return ResponseEntity.ok(userName);
    }
    
  
    @GetMapping("/names/{loc_code}")
    public ResponseEntity<List<String>> getEmployeesByLoc(@PathVariable("loc_code") String loc) {
        List<String> employees = employeeService.getEmployeesByLoc(loc);
        return employees.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(employees);
    }
    
   



//    @GetMapping("/getjwt")
//    public ResponseEntity<?> getJwt(HttpServletRequest request) {
//        return employeeService.getJwt(request);
//    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody EmployeeLoginModal loginRequest,HttpServletResponse response) throws Exception {
        return employeeService.login(loginRequest,response);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        return employeeService.logout();
    }
    @GetMapping("/get-captcha")
    public @ResponseBody ResponseEntity<CaptchaResponseData> getCaptcha() throws IOException {
        return employeeService.getCaptcha();
    }

//    @PostMapping("/check-captcha")
//    public ResponseEntity<?> checkCaptcha(@RequestBody EmployeeLoginModal employeeLoginModal) {
//        return employeeService.checkCaptcha(employeeLoginModal);
//    }
    
//    @PostMapping("/check-captcha") 
//    public ResponseEntity<?> checkCaptcha(@RequestBody String Captcha) {
//        return employeeService.checkCaptcha(Captcha);
//    }
    
    @GetMapping("/check-captcha/{captchaValue}")

    public ResponseEntity<Map<String, String>> checkCaptcha(@PathVariable("captchaValue") String captchaValue) {
        return employeeService.checkCaptcha(captchaValue);
    }


//    @GetMapping("/requests")
//    public ResponseEntity<?> requestJwtCheck(HttpServletRequest httpServletRequest) throws Exception {
//        return employeeService.RequestJwtCheck(httpServletRequest);
//    }


//    @GetMapping("/my-incoming-parcels")
//    public ResponseEntity<?> getIncomingParcelsForUser(HttpServletRequest request) {
//     
//    	
//    	String token = jwtUtils.getJwtFromCookies(request);
//    	
//	    String recipientLocCode = jwtUtils.getLocCodeFromJwtToken(token);
//
//        String recipientName = jwtUtils.getNameFromJwtToken(token);
//
//        logger.info("Received name: {}", recipientName);
//        
//        // List<TrnParcelIn> parcel=trnParcelInService.getParcelsByLocationCodeAndName(recipientLocCode, recipientName);
//
//        List<TrnParcelIn> parcels = trnParcelInService.getParcelsByName(recipientName);
//        return ResponseEntity.ok(parcels);
//    }
//    
//    
//    @GetMapping("/my-outgoing-parcels")
//    public ResponseEntity<?> getOutgoingParcelsForUser(HttpServletRequest request) {
//    	
//        String token = jwtUtils.getJwtFromCookies(request);
//	        
//	    String senderLocCode = jwtUtils.getLocCodeFromJwtToken(token);
//	    
//        String senderName = jwtUtils.getNameFromJwtToken(token);
//
//     //   List<TrnParcelOut> parcel=trnParcelOutService.getParcelsByLocationCodeAndName(senderLocCode, senderName);
//
//        List<TrnParcelOut> parcels = trnParcelOutService.getParcelsByName(senderName);
//        return ResponseEntity.ok(parcels);
//    }
    
    @GetMapping("/my-incoming-parcels")
    public ResponseEntity<?> getIncomingParcelsForUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String token = jwtUtils.getJwtFromCookies(request);
        String recipientLocCode = jwtUtils.getLocCodeFromJwtToken(token);
        String recipientName = jwtUtils.getNameFromJwtToken(token);

        Pageable pageable = PageRequest.of(page, size);
        Page<ParcelInDto> parcelPage = trnParcelInService.getParcelsByLocationCodeAndName(recipientLocCode, recipientName, pageable);

        return ResponseEntity.ok(parcelPage);

    }

    @GetMapping("/my-outgoing-parcels")
    public ResponseEntity<?> getOutgoingParcelsForUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String token = jwtUtils.getJwtFromCookies(request);
        String senderLocCode = jwtUtils.getLocCodeFromJwtToken(token);
        String senderName = jwtUtils.getNameFromJwtToken(token);

        Pageable pageable = PageRequest.of(page, size);

        Page<ParcelOutDto> parcelPage = trnParcelOutService.getParcelsByLocationCodeAndName(senderLocCode, senderName, pageable);

        return ResponseEntity.ok(parcelPage);

    }

    

    

    @GetMapping("/my-incoming-parcels/today")

    public ResponseEntity<?> gettodayIncomingParcelsForUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String token = jwtUtils.getJwtFromCookies(request);

        String recipientLocCode = jwtUtils.getLocCodeFromJwtToken(token);

        String recipientName = jwtUtils.getNameFromJwtToken(token);

        Pageable pageable = PageRequest.of(page, size);

        Page<ParcelInDto> parcelPage = trnParcelInService.getTodaysParcelsByLocationCodeAndName(recipientLocCode, recipientName, pageable);

        return ResponseEntity.ok(parcelPage);

    }
    
    
    @GetMapping("/my-outgoing-parcels/today")
    public ResponseEntity<?> gettodayOutgoingParcelsForUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String token = jwtUtils.getJwtFromCookies(request);

        String senderLocCode = jwtUtils.getLocCodeFromJwtToken(token);

        String senderName = jwtUtils.getNameFromJwtToken(token);

        Pageable pageable = PageRequest.of(page, size);

        Page<ParcelOutDto> parcelPage = trnParcelOutService.getTodaysParcelsByLocationCodeAndName(senderLocCode, senderName, pageable);
        
        return ResponseEntity.ok(parcelPage);

    }
    
    
    @GetMapping("/totals")
    public Map<String, Object> getDispatchTotals() {
        Map<String, Object> totals = new HashMap<>();

        // Total count of parcels dispatched (parcel out)
        long totalCountParcelOut = trnparcelCountService.totalCountParcelOut();
        totals.put("ParcelOut", totalCountParcelOut);

        // Total count of parcels received (parcel in)
        long totalCountParcelIn = trnparcelCountService.totalCountParcelIn();
        totals.put("ParcelIn", totalCountParcelIn);

        // Total count of parcels in + parcels out
        long totalParcels = totalCountParcelIn + totalCountParcelOut;
        totals.put("totalParcels", totalParcels);

        return totals;
    }
    
    @GetMapping("/totals/by-loccode")
    public ResponseEntity<Map<String, Object>> getDispatchTotalsByLocCode(@RequestParam String locCode) {
        Map<String, Object> totalsByLocCode = trnparcelCountService.getTotalsByLocCode(locCode);
        return ResponseEntity.ok(totalsByLocCode);
    }
    
    
    @GetMapping("/history/employee")
    public ResponseEntity<?> getDispatchHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam String type,
            HttpServletRequest request) {

        // Extract locCode from JWT token
        String token = jwtUtils.getJwtFromCookies(request);
        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
        String employeeName = jwtUtils.getNameFromJwtToken(token); // Assumes a method to get employee name

        if (locCode == null || employeeName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or locCode/employeeName not found.");
        }

        if ("in".equalsIgnoreCase(type)) {
            List<ParcelInDto> parcelsIn = trnParcelInService.findByDateRangeAndRecipientNameAndLocCode(fromDate, toDate, employeeName, locCode);
            return ResponseEntity.ok(parcelsIn);
        } else if ("out".equalsIgnoreCase(type)) {
            List<ParcelOutDto> parcelsOut = trnParcelOutService.findByDateRangeAndSenderNameAndLocCode(fromDate, toDate, employeeName, locCode);
            return ResponseEntity.ok(parcelsOut);
        } else {
            return ResponseEntity.badRequest().body("Invalid parcel type. Must be 'in' or 'out'.");
        }
    }

//    
//    @GetMapping("/employee/daily/report")
//    public ResponseEntity<?> getDailyDispatchHistory(
//            @RequestParam String type,  // in or out
//            HttpServletRequest request) {
//
//        // Extract locCode and employee name from JWT token
//        String token = jwtUtils.getJwtFromCookies(request);
//        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
//        String employeeName = jwtUtils.getNameFromJwtToken(token); // Assumes a method to get employee name
//
//        if (locCode == null || employeeName == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or locCode/employeeName not found.");
//        }
//
//        // Daily report: Use current date for both fromDate and toDate
//        LocalDate fromDate = LocalDate.now();
//        LocalDate toDate = LocalDate.now();
//
//        // Logic for parcel type "in" or "out"
//        if ("in".equalsIgnoreCase(type)) {
//            // Fetch daily "in" parcels for the employee
//            List<TrnParcelIn> parcelsIn = trnParcelInService.findByDateRangeAndRecipientNameAndLocCode(fromDate, toDate, employeeName, locCode);
//            return ResponseEntity.ok(parcelsIn);
//        } else if ("out".equalsIgnoreCase(type)) {
//            // Fetch daily "out" parcels for the employee
//            List<TrnParcelOut> parcelsOut = trnParcelOutService.findByDateRangeAndSenderNameAndLocCode(fromDate, toDate, employeeName, locCode);
//            return ResponseEntity.ok(parcelsOut);
//        } else {
//            return ResponseEntity.badRequest().body("Invalid parcel type. Must be 'in' or 'out'.");
//        }
//    }

//    @GetMapping("/employee/monthly/report")
//    public ResponseEntity<?> getMonthlyDispatchHistory(
//            @RequestParam String type,  // in or out
//            HttpServletRequest request) {
//
//        // Extract locCode and employee name from JWT token
//        String token = jwtUtils.getJwtFromCookies(request);
//        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
//        String employeeName = jwtUtils.getNameFromJwtToken(token); // Assumes a method to get employee name
//
//        if (locCode == null || employeeName == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or locCode/employeeName not found.");
//        }
//
//        // Monthly report: Use the first and last days of the current month
//        LocalDate fromDate = LocalDate.now().withDayOfMonth(1);  // First day of the month
//        LocalDate toDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());  // Last day of the month
//
//        // Logic for parcel type "in" or "out"
//        if ("in".equalsIgnoreCase(type)) {
//            // Fetch monthly "in" parcels for the employee
//            List<TrnParcelIn> parcelsIn = trnParcelInService.findByDateRangeAndRecipientNameAndLocCode(fromDate, toDate, employeeName, locCode);
//            return ResponseEntity.ok(parcelsIn);
//        } else if ("out".equalsIgnoreCase(type)) {
//            // Fetch monthly "out" parcels for the employee
//            List<TrnParcelOut> parcelsOut = trnParcelOutService.findByDateRangeAndSenderNameAndLocCode(fromDate, toDate, employeeName, locCode);
//            return ResponseEntity.ok(parcelsOut);
//        } else {
//            return ResponseEntity.badRequest().body("Invalid parcel type. Must be 'in' or 'out'.");
//        }
//    }

//    @GetMapping("/employee/monthly/report")
//    public ResponseEntity<?> getMonthlyDispatchHistory(
//             
//            @RequestParam(required = false) Integer month,  // Optional, defaults to current month if not provided
//            @RequestParam(required = false) Integer year, 
//            @RequestParam String type,// Optional, defaults to current year if not provided
//            HttpServletRequest request) {
//
//        // Extract locCode and employee name from JWT token
//        String token = jwtUtils.getJwtFromCookies(request);
//        String locCode = jwtUtils.getLocCodeFromJwtToken(token);
//        String employeeName = jwtUtils.getNameFromJwtToken(token);  // Assumes a method to get employee name
//
//        if (locCode == null || employeeName == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or locCode/employeeName not found.");
//        }
//
//        // Get the current month and year if not provided
//        LocalDate now = LocalDate.now();
//        int selectedMonth = (month != null) ? month : now.getMonthValue();
//        int selectedYear = (year != null) ? year : now.getYear();
//
//        // Validate the month range (1-12)
//        if (selectedMonth < 1 || selectedMonth > 12) {
//            return ResponseEntity.badRequest().body("Invalid month. Must be between 1 and 12.");
//        }
//
//        // Validate the year range (adjust as per your application's requirement)
//        // Assuming year should not be less than 2000 and not in the future
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
//            // Fetch monthly "in" parcels for the employee within the selected date range
//            List<TrnParcelIn> parcelsIn = trnParcelInService.findByDateRangeAndRecipientNameAndLocCode(fromDate, toDate, employeeName, locCode);
//            return ResponseEntity.ok(parcelsIn);
//        } else if ("out".equalsIgnoreCase(type)) {
//            // Fetch monthly "out" parcels for the employee within the selected date range
//            List<TrnParcelOut> parcelsOut = trnParcelOutService.findByDateRangeAndSenderNameAndLocCode(fromDate, toDate, employeeName, locCode);
//            return ResponseEntity.ok(parcelsOut);
//        } else {
//            return ResponseEntity.badRequest().body("Invalid parcel type. Must be 'in' or 'out'.");
//        }
//    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAdminsAddedRoles() {
        List<String> roles = mstuserService.getAdminsAddedRoles();
        return ResponseEntity.ok(roles);
    }
}
