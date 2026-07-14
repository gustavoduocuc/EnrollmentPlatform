package com.duoc.enrollmentplatform.courses.infrastructure.http;

import com.duoc.enrollmentplatform.courses.application.CourseDTO;
import com.duoc.enrollmentplatform.courses.application.CreateCourseRequest;
import com.duoc.enrollmentplatform.courses.application.CreateCourseUseCase;
import com.duoc.enrollmentplatform.courses.application.DeleteCourseUseCase;
import com.duoc.enrollmentplatform.courses.application.ListCoursesUseCase;
import com.duoc.enrollmentplatform.courses.application.UpdateCourseUseCase;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/courses")
public class CourseController {
    private final ListCoursesUseCase listCoursesUseCase;
    private final CreateCourseUseCase createCourseUseCase;
    private final UpdateCourseUseCase updateCourseUseCase;
    private final DeleteCourseUseCase deleteCourseUseCase;

    public CourseController(
            ListCoursesUseCase listCoursesUseCase,
            CreateCourseUseCase createCourseUseCase,
            UpdateCourseUseCase updateCourseUseCase,
            DeleteCourseUseCase deleteCourseUseCase) {
        this.listCoursesUseCase = listCoursesUseCase;
        this.createCourseUseCase = createCourseUseCase;
        this.updateCourseUseCase = updateCourseUseCase;
        this.deleteCourseUseCase = deleteCourseUseCase;
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> list() {
        return ResponseEntity.ok(listCoursesUseCase.execute());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        if (!(body.get("name") instanceof String name)) {
            return ResponseEntity.badRequest().body(Map.of("error", "name is required and must be a string"));
        }
        if (!(body.get("section") instanceof String section)) {
            return ResponseEntity.badRequest().body(Map.of("error", "section is required and must be a string"));
        }
        if (!(body.get("instructorId") instanceof String instructorId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "instructorId is required and must be a string"));
        }
        if (!(body.get("durationHours") instanceof Number durationHoursNum)) {
            return ResponseEntity.badRequest().body(Map.of("error", "durationHours is required and must be a number"));
        }
        if (!(body.get("price") instanceof Number priceNum)) {
            return ResponseEntity.badRequest().body(Map.of("error", "price is required and must be a number"));
        }

        CreateCourseRequest request = new CreateCourseRequest();
        request.name = name;
        request.section = section;
        request.instructorId = instructorId;
        request.durationHours = durationHoursNum.intValue();
        request.price = priceNum.doubleValue();

        try {
            return ResponseEntity.status(201).body(createCourseUseCase.execute(request));
        } catch (Exception error) {
            return handleError(error);
        }
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<?> update(@PathVariable String courseId, @RequestBody Map<String, Object> body) {
        String name = null;
        String instructorId = null;
        if (body.containsKey("name")) {
            if (!(body.get("name") instanceof String nameValue)) {
                return ResponseEntity.badRequest().body(Map.of("error", "name must be a string"));
            }
            name = nameValue;
        }
        if (body.containsKey("instructorId")) {
            if (!(body.get("instructorId") instanceof String instructorIdValue)) {
                return ResponseEntity.badRequest().body(Map.of("error", "instructorId must be a string"));
            }
            instructorId = instructorIdValue;
        }
        try {
            return ResponseEntity.ok(updateCourseUseCase.execute(courseId, name, instructorId));
        } catch (Exception error) {
            return handleError(error);
        }
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> delete(@PathVariable String courseId) {
        try {
            deleteCourseUseCase.execute(courseId);
            return ResponseEntity.noContent().build();
        } catch (Exception error) {
            return handleError(error);
        }
    }

    private ResponseEntity<?> handleError(Exception error) {
        if (error instanceof DomainError domainError) {
            int status = switch (domainError.getType()) {
                case NOT_FOUND -> 404;
                case VALIDATION -> 422;
                default -> 400;
            };
            return ResponseEntity.status(status).body(Map.of("error", domainError.getMessage()));
        }
        return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
    }
}
