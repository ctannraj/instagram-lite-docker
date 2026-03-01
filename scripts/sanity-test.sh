#!/bin/bash

#set -x

USER_ID="user-$(date +%H%M%S)-$((RANDOM % 100))"
echo "Sanity test for X-User-ID : ${USER_ID}"

function get() {
    echo "" >&2
    echo "User timeline ... " >&2
    curl -s -X GET "http://localhost:8080/users/${USER_ID}/timeline"
}

get | jq .

echo "Create post ... "
curl -s -X POST http://localhost:8080/posts \
  -H "Content-Type: application/json" \
	-H "X-User-Id: ${USER_ID}" \
  -d '{
        "text": "Hello World - my first post"
      }'

POST_ID=$(get | jq -r '.[0].postId')
echo "New post : $POST_ID"

echo "Update post ($POST_ID) ... "
curl -s -X PUT "http://localhost:8080/posts/${POST_ID}" \
	-H "Content-Type: application/json" \
	-H "X-User-Id: ${USER_ID}" \
  -d '{
        "text": "Updated Text"
      }'

get | jq .

echo "Delete post ($POST_ID) ... "
curl -s -X DELETE "http://localhost:8080/posts/${POST_ID}" \
	-H "X-User-Id: ${USER_ID}"

get | jq .
