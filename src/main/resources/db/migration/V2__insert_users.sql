
INSERT INTO users (username, password,enabled)
SELECT 'admin', '$2y$10$FQ4VsMkf3uw6ABXrfEaRRuzA7JzEWtF7BvTAX7J2qySdskcLMeeW.',true;

INSERT INTO authorities (username, authority)
SELECT 'admin', 'ROLE_ADMIN';

INSERT INTO users (username, password, enabled)
SELECT 'librarian', '$2y$10$YJFhuWlxMQWNlexOqRRn0eoegx5J2nWF5mvhCGtO1ypykz1Q3DDeW',true;

INSERT INTO authorities (username, authority)
SELECT 'librarian', 'ROLE_LIBRARIAN';
