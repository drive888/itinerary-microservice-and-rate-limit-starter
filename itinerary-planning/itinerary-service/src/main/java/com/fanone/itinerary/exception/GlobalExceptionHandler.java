package com.fanone.itinerary.exception;

import com.fanone.itinerary.controller.ItineraryController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ItineraryController.Result<?> handleRuntimeException(RuntimeException e) {
        return ItineraryController.Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ItineraryController.Result<?> handleException(Exception e) {
        return ItineraryController.Result.error("服务器内部错误");
    }
}
