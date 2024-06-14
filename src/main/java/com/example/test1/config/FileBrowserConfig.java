package com.example.test1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@ConfigurationProperties(prefix = "filebrowser.scan")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileBrowserConfig {
  private String directory;
}
