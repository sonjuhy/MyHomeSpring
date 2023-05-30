package com.myhome.server.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class NoticeDto {
    private long id;
    private String title;
    private String content;
    private String writer;
    private String date;
}
