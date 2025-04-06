CREATE TABLE IF NOT EXISTS topics
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL
);

-- Insertion de 5 enregistrements d'exemple (la gestion des thèmes n'est pas demandée
INSERT INTO topics (title, description)
VALUES ('Thème 1', 'Description thème 1. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam sed.'),
       ('Thème 2', 'Description thème 2. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam sed.'),
       ('Thème 3', 'Description thème 3. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam sed.'),
       ('Thème 4', 'Description thème 4. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam sed.'),
       ('Thème 5', 'Description thème 5. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam sed.');

COMMIT;