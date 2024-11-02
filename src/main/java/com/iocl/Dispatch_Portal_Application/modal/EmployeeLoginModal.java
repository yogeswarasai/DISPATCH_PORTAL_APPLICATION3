package com.iocl.Dispatch_Portal_Application.modal;

import java.util.UUID;

import lombok.Data;

@Data
public class EmployeeLoginModal {

	int id;

    String password;
  //  UUID captcha_id;

    String captcha_value;

}
