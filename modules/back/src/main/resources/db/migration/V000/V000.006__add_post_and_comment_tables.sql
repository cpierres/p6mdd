CREATE TABLE IF NOT EXISTS mddsocial.post
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by UUID NOT NULL,
    FOREIGN KEY (topic_id) REFERENCES mddsocial.topics (id),
    FOREIGN KEY (created_by) REFERENCES mddsocial.users (id)
);

CREATE TABLE IF NOT EXISTS mddsocial.post_comment
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by UUID NOT NULL,
    FOREIGN KEY (post_id) REFERENCES mddsocial.post (id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES mddsocial.users (id)
);