package com.example.test1.entity;

import java.util.List;

import lombok.Data;

@Data
public class CopyOrMoveFileSystemItemDTO {
  private List<String> sourceUrlList;
  private String destinationUrl;
}
