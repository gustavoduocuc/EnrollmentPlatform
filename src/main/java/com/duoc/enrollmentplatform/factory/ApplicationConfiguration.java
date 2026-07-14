package com.duoc.enrollmentplatform.factory;

import com.duoc.enrollmentplatform.courses.domain.repositories.CourseRepository;
import com.duoc.enrollmentplatform.courses.infrastructure.adapters.CourseStore;
import com.duoc.enrollmentplatform.courses.infrastructure.http.CourseController;
import com.duoc.enrollmentplatform.enrollment.application.summary.EnrollmentSummaryGenerator;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentSummaryPdfRenderer;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentSummaryStorage;
import com.duoc.enrollmentplatform.enrollment.domain.repositories.EnrollmentRepository;
import com.duoc.enrollmentplatform.enrollment.infrastructure.adapters.EnrollmentStore;
import com.duoc.enrollmentplatform.enrollment.infrastructure.http.EnrollmentController;
import com.duoc.enrollmentplatform.enrollment.infrastructure.http.EnrollmentSummaryController;
import com.duoc.enrollmentplatform.users.application.ports.IdentityTenantRegister;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;
import com.duoc.enrollmentplatform.users.infrastructure.adapters.UserStore;
import com.duoc.enrollmentplatform.users.infrastructure.http.UserController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public CourseRepository courseRepository(CourseStore store) {
        return EnrollmentPlatformFactory.getCourseRepository(store);
    }

    @Bean
    public UserRepository userRepository(UserStore store) {
        return EnrollmentPlatformFactory.getUserRepository(store);
    }

    @Bean
    public IdentityTenantRegister identityTenantRegister() {
        return EnrollmentPlatformFactory.getIdentityTenantRegister();
    }

    @Bean
    public EnrollmentRepository enrollmentRepository(EnrollmentStore store) {
        return EnrollmentPlatformFactory.getEnrollmentRepository(store);
    }

    @Bean
    public EnrollmentSummaryGenerator enrollmentSummaryGenerator() {
        return EnrollmentPlatformFactory.enrollmentSummaryGenerator();
    }

    @Bean
    public EnrollmentSummaryPdfRenderer enrollmentSummaryPdfRenderer() {
        return EnrollmentPlatformFactory.enrollmentSummaryPdfRenderer();
    }

    @Bean
    public CourseController courseController(
            CourseRepository courseRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository) {
        return EnrollmentPlatformFactory.createCourseController(
                courseRepository, userRepository, enrollmentRepository);
    }

    @Bean
    public EnrollmentController enrollmentController(
            CourseRepository courseRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository,
            EnrollmentSummaryGenerator enrollmentSummaryGenerator,
            EnrollmentSummaryStorage enrollmentSummaryStorage,
            com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentMessagePublisher enrollmentMessagePublisher) {

        return EnrollmentPlatformFactory.createEnrollmentController(
                courseRepository, userRepository, enrollmentRepository,
                enrollmentSummaryGenerator, enrollmentSummaryStorage,
                enrollmentMessagePublisher);
    }

    @Bean
    public EnrollmentSummaryController enrollmentSummaryController(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            EnrollmentSummaryGenerator enrollmentSummaryGenerator,
            EnrollmentSummaryStorage enrollmentSummaryStorage,
            EnrollmentSummaryPdfRenderer enrollmentSummaryPdfRenderer) {
        return EnrollmentPlatformFactory.createEnrollmentSummaryController(
                enrollmentRepository, userRepository, enrollmentSummaryGenerator,
                enrollmentSummaryStorage, enrollmentSummaryPdfRenderer);
    }

    @Bean
    public UserController userController(
            UserRepository userRepository,
            IdentityTenantRegister identityTenantRegister) {
        return EnrollmentPlatformFactory.createUserController(userRepository, identityTenantRegister);
    }
}
