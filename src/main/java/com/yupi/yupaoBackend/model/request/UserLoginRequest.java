package com.yupi.yupaoBackend.model.request;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 陈君哲
 */
@Data
public class UserLoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -1380982747167751477L;
    
    private String userAccount;
    private String userPassword;


}
