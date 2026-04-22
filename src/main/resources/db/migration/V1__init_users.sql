
INSERT INTO users (username, password, role)
SELECT 'admin', '$2y$10$FQ4VsMkf3uw6ABXrfEaRRuzA7JzEWtF7BvTAX7J2qySdskcLMeeW.', 'ROLE_ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);
INSERT INTO authorities (user_id, authority)
SELECT id, 'ROLE_ADMIN'
FROM users
WHERE username = 'admin';

INSERT INTO users (username, password, role)
SELECT 'librarian', '$2y$10$YJFhuWlxMQWNlexOqRRn0eoegx5J2nWF5mvhCGtO1ypykz1Q3DDeW', 'ROLE_LIBRARIAN'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'librarian'
);
INSERT INTO authorities (user_id, authority)
SELECT id, 'ROLE_LIBRARIAN'
FROM users
WHERE username = 'librarian';
