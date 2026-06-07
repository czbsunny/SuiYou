package com.suiyou.dto;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigManifestRespDTO {
    private String globalHash;

    private Map<String, String> modules;
}
