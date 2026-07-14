INSERT INTO courses (id, name, instructor, duration_hours, price) VALUES ('c-001', 'Introducción a Java', 'María González', 40, 150000);
INSERT INTO courses (id, name, instructor, duration_hours, price) VALUES ('c-002', 'Bases de datos', 'Carlos Pérez', 30, 120000);
INSERT INTO courses (id, name, instructor, duration_hours, price) VALUES ('c-003', 'Cloud Native', 'Ana Ruiz', 25, 180000);

INSERT INTO users (id, email, full_name, student_id, role, status)
VALUES ('u-admin-001', 'gus.dominguez@duocuc.cl', 'Admin Duoc', NULL, 'ADMIN', 'ACTIVE');

INSERT INTO users (id, email, full_name, student_id, role, status)
VALUES ('u-001', 'juan.soto@duoc.cl', 'Juan Soto', 's-001', 'STUDENT', 'ACTIVE');

INSERT INTO users (id, email, full_name, student_id, role, status)
VALUES ('u-002', 'valentina.munoz@duoc.cl', 'Valentina Muñoz', 's-002', 'STUDENT', 'ACTIVE');
