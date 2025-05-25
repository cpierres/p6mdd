package com.mdd.back.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImportDto {
    private String username;
    private String topicTitle;
    private String title;
    private String content;
}
