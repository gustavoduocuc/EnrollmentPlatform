INSERT INTO users (id, email, full_name, student_id, role, status)
VALUES ('u-admin-001', 'gus.dominguez@duocuc.cl', 'Admin Duoc', NULL, 'ADMIN', 'ACTIVE');

INSERT INTO users (id, email, full_name, student_id, role, status)
VALUES ('u-teacher-001', 'maria.gonzalez@duoc.cl', 'María González', NULL, 'TEACHER', 'ACTIVE');

INSERT INTO users (id, email, full_name, student_id, role, status)
VALUES ('u-001', 'juan.soto@duoc.cl', 'Juan Soto', 's-001', 'STUDENT', 'ACTIVE');

INSERT INTO users (id, email, full_name, student_id, role, status)
VALUES ('u-002', 'valentina.munoz@duoc.cl', 'Valentina Muñoz', 's-002', 'STUDENT', 'ACTIVE');

INSERT INTO courses (id, name, instructor_id, section, duration_hours, price)
VALUES ('c-001', 'Introducción a Java', 'u-teacher-001', 'A', 40, 150000);

INSERT INTO courses (id, name, instructor_id, section, duration_hours, price)
VALUES ('c-002', 'Bases de datos', 'u-teacher-001', 'B', 30, 120000);

INSERT INTO courses (id, name, instructor_id, section, duration_hours, price)
VALUES ('c-003', 'Cloud Native', 'u-teacher-001', 'C', 25, 180000);
