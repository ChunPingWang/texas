package com.holdem.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全域異常處理器
 * 統一處理應用程式中的異常，返回格式化的錯誤回應
 * 符合基礎設施層的職責，不依賴其他業務層
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 處理驗證異常
     * @param ex 方法參數驗證異常
     * @return 錯誤回應
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "輸入資料驗證失敗",
            LocalDateTime.now(),
            errors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * 處理業務邏輯異常
     * @param ex 非法參數異常
     * @return 錯誤回應
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "BUSINESS_ERROR",
            ex.getMessage(),
            LocalDateTime.now(),
            null
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * 處理其他未捕獲的異常
     * @param ex 未知異常
     * @return 錯誤回應
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            "系統內部錯誤，請稍後再試",
            LocalDateTime.now(),
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * 錯誤回應類別
     */
    public static class ErrorResponse {
        private String code;
        private String message;
        private LocalDateTime timestamp;
        private Object details;
        
        public ErrorResponse(String code, String message, LocalDateTime timestamp, Object details) {
            this.code = code;
            this.message = message;
            this.timestamp = timestamp;
            this.details = details;
        }
        
        // Getter 和 Setter
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public Object getDetails() {
            return details;
        }
        
        public void setDetails(Object details) {
            this.details = details;
        }
    }
}