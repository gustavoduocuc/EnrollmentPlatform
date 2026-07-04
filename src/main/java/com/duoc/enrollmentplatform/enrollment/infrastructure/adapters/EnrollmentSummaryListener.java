package com.duoc.enrollmentplatform.enrollment.infrastructure.adapters;

import com.duoc.enrollmentplatform.factory.RabbitMQConfiguration;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class EnrollmentSummaryListener {

    private final JpaEnrollmentSummaryRecordRepository repository;

    
    public EnrollmentSummaryListener(JpaEnrollmentSummaryRecordRepository repository) {
        this.repository = repository;
    }

    
    @RabbitListener(queues = RabbitMQConfiguration.QUEUE_NAME)
    public void receiveMessage(Map<String, String> payload) {
        System.out.println("Mensaje asíncrono recibido desde RabbitMQ: " + payload);
        
        
        String enrollmentId = payload.get("enrollmentId");
        String studentId = payload.get("studentId");
        String status = payload.get("status");
        
        
        String id = UUID.randomUUID().toString(); 
        
        
        EnrollmentSummaryRecord record = new EnrollmentSummaryRecord(id, enrollmentId, studentId, status);
        
        
        repository.save(record);
        
        System.out.println("Resumen guardado satisfactoriamente en la nueva tabla de la BD.");
    }
}