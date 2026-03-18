package com.ahsmart.campusmarket.payloadDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIExceptionResponse {  // this will be returned in exception Global exception class instead of some string
    public String message;
    public boolean status;
}
