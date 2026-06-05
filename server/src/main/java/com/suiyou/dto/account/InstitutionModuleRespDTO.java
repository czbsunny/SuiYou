package com.suiyou.dto.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionModuleRespDTO {
    @Builder.Default
    private List<AssetCategoryRespDTO> required = new ArrayList<>();
    
    @Builder.Default
    private List<AssetCategoryRespDTO> defaultList = new ArrayList<>();
    
    @Builder.Default
    private List<AssetCategoryRespDTO> optional = new ArrayList<>();
}