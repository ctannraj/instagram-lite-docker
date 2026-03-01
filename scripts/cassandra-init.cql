CREATE KEYSPACE IF NOT EXISTS instagram
    WITH replication = {
        'class': 'SimpleStrategy',
        'replication_factor': 1
        };

USE instagram;

CREATE TABLE IF NOT EXISTS posts_by_id (
                                           post_id text PRIMARY KEY,
                                           user_id text,
                                           text text,
                                           created_at timestamp
);

CREATE TABLE IF NOT EXISTS posts_by_user (
                                             user_id text,
                                             created_at timestamp,
                                             post_id text,
                                             text text,
                                             PRIMARY KEY (user_id, created_at)
) WITH CLUSTERING ORDER BY (created_at DESC);