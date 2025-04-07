CREATE TABLE IF NOT EXISTS mddsocial.user_topic_subscription
(
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id  UUID NOT NULL,
    topic_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES mddsocial.users (id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES mddsocial.topics (id) ON DELETE CASCADE
);
