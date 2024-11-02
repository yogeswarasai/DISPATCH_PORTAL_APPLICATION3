package com.iocl.Dispatch_Portal_Application.Controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.iocl.Dispatch_Portal_Application.DTO.ParcelInDto;
import com.iocl.Dispatch_Portal_Application.DTO.ParcelOutDto;
import com.iocl.Dispatch_Portal_Application.Entity.MstCourier;
import com.iocl.Dispatch_Portal_Application.Entity.MstDepartment;
import com.iocl.Dispatch_Portal_Application.Entity.MstEmployee;
import com.iocl.Dispatch_Portal_Application.Entity.TrnParcelIn;
import com.iocl.Dispatch_Portal_Application.Entity.TrnParcelOut;
import com.iocl.Dispatch_Portal_Application.Security.JwtUtils;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.DispatchService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.EmployeeService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.MstCourierService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.MstDepartmentService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.MstLocationService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.TrnParcelCountService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.TrnParcelInService;
import com.iocl.Dispatch_Portal_Application.ServiceLayer.TrnParcelOutService;

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
    
    
    
//    @GetMapping("/history")
//    public ResponseEntity<?> getDispatchHistory(
//           
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
//            @RequestParam String type) {
//        
//        if ("in".equalsIgnoreCase(type)) {
//            List<TrnParcelIn> parcelsIn = trnparcelinService.findByDateRange(fromDate, toDate);
//            return ResponseEntity.ok(parcelsIn);
//        } else if ("out".equalsIgnoreCase(type)) {
//            List<TrnParcelOut> parcelsOut = trnparceloutService.findByDateRange(fromDate, toDate);
//            return ResponseEntity.ok(parcelsOut);
//        } else {
//            return ResponseEntity.badRequest().body(List.of("Invalid parcel type. Must be 'in' or 'out'."));
//        }
//    }
    
    @GetMapping("/history/all")
    public ResponseEntity<?> getDispatchHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam String type,
            HttpServletRequest request) {

        // Extract locCode from JWT token
        String token = jwtUtils.getJwtFromCookies(request);
        String locCode = jwtUtils.getLocCodeFromJwtToken(token);

        if (locCode == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or locCode not found.");
        }

        if ("in".equalsIgnoreCase(type)) {
            List<ParcelInDto> parcelsIn = trnparcelinService.findByDateRangeAndLocCode(fromDate, toDate, locCode);
            return ResponseEntity.ok(parcelsIn);
        } else if ("out".equalsIgnoreCase(type)) {
            List<ParcelOutDto> parcelsOut = trnparceloutService.findByDateRangeAndLocCode(fromDate, toDate, locCode);
            return ResponseEntity.ok(parcelsOut);
        } else {
            return ResponseEntity.badRequest().body("Invalid parcel type. Must be 'in' or 'out'.");
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
