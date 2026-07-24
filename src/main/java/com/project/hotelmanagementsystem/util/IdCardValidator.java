package com.project.hotelmanagementsystem.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class IdCardValidator {

    private static final int[] FACTORS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    public static String validate(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            return null;
        }
        
        idNumber = idNumber.trim().toUpperCase();
        
        if (!idNumber.matches("^\\d{17}[\\dX]$")) {
            return "身份证号格式不正确，必须为18位";
        }
        
        if (!validateCheckCode(idNumber)) {
            return "身份证号校验码不正确";
        }
        
        String birthDateStr = idNumber.substring(6, 14);
        if (!validateBirthDate(birthDateStr)) {
            return "身份证号中的出生日期不合法";
        }
        
        String gender = idNumber.substring(16, 17);
        if (!gender.matches("[0-9]")) {
            return "身份证号中的性别码不合法";
        }
        
        return null;
    }

    private static boolean validateCheckCode(String idNumber) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += Character.getNumericValue(idNumber.charAt(i)) * FACTORS[i];
        }
        int remainder = sum % 11;
        return idNumber.charAt(17) == CHECK_CODES[remainder];
    }

    private static boolean validateBirthDate(String birthDateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate birthDate = LocalDate.parse(birthDateStr, formatter);
            
            LocalDate minDate = LocalDate.of(1900, 1, 1);
            LocalDate maxDate = LocalDate.now();
            
            return !birthDate.isBefore(minDate) && !birthDate.isAfter(maxDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static String extractBirthDate(String idNumber) {
        if (idNumber == null || idNumber.length() < 14) {
            return null;
        }
        return idNumber.substring(6, 14);
    }

    public static String extractGender(String idNumber) {
        if (idNumber == null || idNumber.length() < 17) {
            return null;
        }
        int genderCode = Integer.parseInt(idNumber.substring(16, 17));
        return genderCode % 2 == 0 ? "female" : "male";
    }

    public static boolean isValid(String idNumber) {
        return validate(idNumber) == null;
    }
}
