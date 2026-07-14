CREATE TABLE users (
    id          VARCHAR(36)  NOT NULL,
    email       VARCHAR(255) NOT NULL,
    full_name   VARCHAR(255),
    student_id  VARCHAR(36),
    role        VARCHAR(20)  NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_student_id UNIQUE (student_id)
);

CREATE TABLE courses (
    id             VARCHAR(36)    NOT NULL,
    name           VARCHAR(255)   NOT NULL,
    instructor_id  VARCHAR(36)    NOT NULL,
    section        CHAR(1)        NOT NULL,
    duration_hours INT            NOT NULL,
    price          DECIMAL(15, 2) NOT NULL,
    CONSTRAINT pk_courses PRIMARY KEY (id),
    CONSTRAINT fk_courses_instructor FOREIGN KEY (instructor_id) REFERENCES users (id),
    CONSTRAINT uk_courses_name_section UNIQUE (name, section)
);

CREATE TABLE enrollments (
    id           VARCHAR(36)    NOT NULL,
    student_id   VARCHAR(36)    NOT NULL,
    enrolled_at  TIMESTAMP      NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    CONSTRAINT pk_enrollments PRIMARY KEY (id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES users (student_id)
);

CREATE TABLE enrollment_lines (
    id            VARCHAR(36)    NOT NULL,
    enrollment_id VARCHAR(36)    NOT NULL,
    course_id     VARCHAR(36)    NOT NULL,
    course_name   VARCHAR(255)   NOT NULL,
    unit_price    DECIMAL(15, 2) NOT NULL,
    CONSTRAINT pk_enrollment_lines            PRIMARY KEY (id),
    CONSTRAINT fk_enrollment_lines_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments (id),
    CONSTRAINT fk_enrollment_lines_course     FOREIGN KEY (course_id)     REFERENCES courses (id)
);
