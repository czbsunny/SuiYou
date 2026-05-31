
package com.suiyou.controller;

import com.suiyou.dto.account.InstitutionRespDTO;
import com.suiyou.service.SysAssetConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/institutions")
public class InstitutionController {

    @Autowired
    private SysAssetConfigService assetConfigService;

    @GetMapping
    public ResponseEntity<List<InstitutionRespDTO>> getAllInstitutions() {
        return new ResponseEntity<>(assetConfigService.getAllInstitutions(), HttpStatus.OK);
    }

    @GetMapping("/type/{typeCode}")
    public ResponseEntity<List<InstitutionRespDTO>> getInstitutionsByType(@PathVariable String typeCode) {
        return new ResponseEntity<>(assetConfigService.getInstitutionsByType(typeCode), HttpStatus.OK);
    }

    @GetMapping("/hot")
    public ResponseEntity<List<InstitutionRespDTO>> getHotInstitutions() {
        return new ResponseEntity<>(assetConfigService.getHotInstitutions(), HttpStatus.OK);
    }

    @GetMapping("/{instCode}")
    public ResponseEntity<InstitutionRespDTO> getInstitutionByCode(@PathVariable String instCode) {
        InstitutionRespDTO institution = assetConfigService.getInstitutionByCode(instCode);
        if (institution == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(institution, HttpStatus.OK);
    }
}
