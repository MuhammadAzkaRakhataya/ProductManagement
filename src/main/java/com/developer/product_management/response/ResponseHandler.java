package com.developer.product_management.response;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {

    public static ResponseEntity<Object> responseBuilder(
            String message, HttpStatus httpStatus, Map<String, Object> responseObject
    )
    {
        Map<String,Object> response = new HashMap<>();
        response.put("Messagge", message );
        response.put("httpStatus", httpStatus);
        response.put("Data", responseObject);

        return new ResponseEntity<>(response, httpStatus);
    }

    public static ResponseEntity<Object> messageResponse(String message, HttpStatus httpStatus) {
        Map<String, Object> response = new HashMap<>();
        response.put("Message", message);
        response.put("httpStatus", httpStatus);

        return new ResponseEntity<>(response, httpStatus);
    }

}
