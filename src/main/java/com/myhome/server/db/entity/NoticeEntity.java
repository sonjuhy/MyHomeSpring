package com.myhome.server.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@Table(name = "NOTICE_TB")
@ToString
@NoArgsConstructor
public class NoticeEntity {
    @Id
    @Column(name = "NOTICE_PK")
    private long id;
    @Column(name = "TITLE_CHAR")
    private String title;
    @Column(name = "CONTENT_CHAR")
    private String content;
    @Column(name = "WRITER_CHAR")
    private String writer;
    @Column(name = "DATE_CHAR")
    private String date;
}
