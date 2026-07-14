package com.duoc.enrollmentplatform.factory;

import com.duoc.enrollmentplatform.courses.application.CreateCourseUseCase;
import com.duoc.enrollmentplatform.courses.application.ListCoursesUseCase;
import com.duoc.enrollmentplatform.courses.domain.repositories.CourseRepository;
import com.duoc.enrollmentplatform.courses.infrastructure.adapters.CourseStore;
import com.duoc.enrollmentplatform.courses.infrastructure.adapters.JpaCourseRepository;
import com.duoc.enrollmentplatform.courses.infrastructure.http.CourseController;
import com.duoc.enrollmentplatform.enrollment.application.CreateEnrollmentUseCase;
import com.duoc.enrollmentplatform.enrollment.application.DeleteEnrollmentUseCase;
import com.duoc.enrollmentplatform.enrollment.application.GetEnrollmentUseCase;
import com.duoc.enrollmentplatform.enrollment.application.ListEnrollmentsUseCase;
import com.duoc.enrollmentplatform.enrollment.application.UpdateEnrollmentUseCase;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentMessagePublisher;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentSummaryPdfRenderer;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentSummaryStorage;
import com.duoc.enrollmentplatform.enrollment.application.summary.DeleteEnrollmentSummaryUseCase;
import com.duoc.enrollmentplatform.enrollment.application.summary.DownloadEnrollmentSummaryUseCase;
import com.duoc.enrollmentplatform.enrollment.application.summary.EnrollmentSummaryGenerator;
import com.duoc.enrollmentplatform.enrollment.application.summary.GenerateEnrollmentSummaryFileUseCase;
import com.duoc.enrollmentplatform.enrollment.application.summary.ListEnrollmentSummariesUseCase;
import com.duoc.enrollmentplatform.enrollment.application.summary.ReplaceEnrollmentSummaryUseCase;
import com.duoc.enrollmentplatform.enrollment.application.summary.UploadEnrollmentSummaryUseCase;
import com.duoc.enrollmentplatform.enrollment.domain.repositories.EnrollmentRepository;
import com.duoc.enrollmentplatform.enrollment.infrastructure.adapters.EnrollmentStore;
import com.duoc.enrollmentplatform.enrollment.infrastructure.adapters.JpaEnrollmentRepository;
import com.duoc.enrollmentplatform.enrollment.infrastructure.adapters.OpenPdfEnrollmentSummaryRenderer;
import com.duoc.enrollmentplatform.enrollment.infrastructure.http.EnrollmentController;
import com.duoc.enrollmentplatform.enrollment.infrastructure.http.EnrollmentSummaryController;
import com.duoc.enrollmentplatform.users.application.DeleteUserUseCase;
import com.duoc.enrollmentplatform.users.application.GetUserUseCase;
import com.duoc.enrollmentplatform.users.application.ListUsersUseCase;
import com.duoc.enrollmentplatform.users.application.LoginUserUseCase;
import com.duoc.enrollmentplatform.users.application.PreRegisterUserUseCase;
import com.duoc.enrollmentplatform.users.application.RegisterUserUseCase;
import com.duoc.enrollmentplatform.users.application.UpdateUserUseCase;
import com.duoc.enrollmentplatform.users.application.ports.IdentityTenantRegister;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;
import com.duoc.enrollmentplatform.users.infrastructure.adapters.JpaUserRepository;
import com.duoc.enrollmentplatform.users.infrastructure.adapters.NoOpIdentityTenantRegister;
import com.duoc.enrollmentplatform.users.infrastructure.adapters.UserStore;
import com.duoc.enrollmentplatform.users.infrastructure.http.UserController;

public class EnrollmentPlatformFactory {

    public static CourseRepository getCourseRepository(CourseStore store) {
        return new JpaCourseRepository(store);
    }

    public static UserRepository getUserRepository(UserStore store) {
        return new JpaUserRepository(store);
    }

    public static IdentityTenantRegister getIdentityTenantRegister() {
        return new NoOpIdentityTenantRegister();
    }

    public static EnrollmentRepository getEnrollmentRepository(EnrollmentStore store) {
        return new JpaEnrollmentRepository(store);
    }

    public static EnrollmentSummaryGenerator enrollmentSummaryGenerator() {
        return new EnrollmentSummaryGenerator();
    }

    public static EnrollmentSummaryPdfRenderer enrollmentSummaryPdfRenderer() {
        return new OpenPdfEnrollmentSummaryRenderer();
    }

    public static CourseController createCourseController(CourseRepository courseRepository) {
        return new CourseController(
                new ListCoursesUseCase(courseRepository),
                new CreateCourseUseCase(courseRepository)
        );
    }

    public static EnrollmentController createEnrollmentController(
            CourseRepository courseRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository,
            EnrollmentSummaryGenerator summaryGenerator,
            EnrollmentSummaryStorage summaryStorage,
            EnrollmentMessagePublisher messagePublisher) {
        return new EnrollmentController(
                new CreateEnrollmentUseCase(courseRepository, userRepository, enrollmentRepository, summaryGenerator, summaryStorage, messagePublisher),
                new ListEnrollmentsUseCase(enrollmentRepository),
                new GetEnrollmentUseCase(enrollmentRepository),
                new UpdateEnrollmentUseCase(courseRepository, enrollmentRepository, userRepository, summaryGenerator, summaryStorage),
                new DeleteEnrollmentUseCase(enrollmentRepository, summaryStorage)
        );
    }

    public static EnrollmentSummaryController createEnrollmentSummaryController(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            EnrollmentSummaryGenerator summaryGenerator,
            EnrollmentSummaryStorage summaryStorage,
            EnrollmentSummaryPdfRenderer pdfRenderer) {
        return new EnrollmentSummaryController(
                new GenerateEnrollmentSummaryFileUseCase(enrollmentRepository, userRepository, summaryGenerator),
                new UploadEnrollmentSummaryUseCase(enrollmentRepository, userRepository, summaryGenerator, summaryStorage),
                new DownloadEnrollmentSummaryUseCase(summaryStorage, pdfRenderer),
                new ReplaceEnrollmentSummaryUseCase(summaryStorage),
                new DeleteEnrollmentSummaryUseCase(summaryStorage),
                new ListEnrollmentSummariesUseCase(summaryStorage)
        );
    }

    public static UserController createUserController(
            UserRepository userRepository,
            IdentityTenantRegister identityTenantRegister) {
        return new UserController(
                new PreRegisterUserUseCase(userRepository),
                new RegisterUserUseCase(userRepository, identityTenantRegister),
                new LoginUserUseCase(userRepository),
                new ListUsersUseCase(userRepository),
                new GetUserUseCase(userRepository),
                new UpdateUserUseCase(userRepository),
                new DeleteUserUseCase(userRepository)
        );
    }
}
