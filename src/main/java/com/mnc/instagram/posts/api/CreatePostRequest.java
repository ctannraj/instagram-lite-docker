package com.mnc.instagram.posts.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreatePostRequest {

    @NotBlank
    private String text;

}
