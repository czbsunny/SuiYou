package com.suiyou.controller;

import com.suiyou.dto.account.AccountTemplateRespDTO; 
import com.suiyou.dto.account.InstRespDTO;
import com.suiyou.dto.account.InstTypeRespDTO;
import com.suiyou.service.SysInstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inst")
public class InstitutionController {

    @Autowired
    private SysInstitutionService institutionService;

    @GetMapping("/types")
    public ResponseEntity<List<InstTypeRespDTO>> getAllInstitutionTypes() {
        return ResponseEntity.ok(institutionService.getAllInstitutionTypes());
    }

    @GetMapping("/all")
    public ResponseEntity<List<InstRespDTO>> getAllInstitutions() {
        return ResponseEntity.ok(institutionService.getAllInstitutions());
    }

    @GetMapping("/hot")
    public ResponseEntity<List<InstRespDTO>> getHotInstitutions() {
        return ResponseEntity.ok(institutionService.getHotInstitutions());
    }

    @GetMapping("/type/{typeCode}")
    public ResponseEntity<List<InstRespDTO>> getInstitutionsByType(@PathVariable String typeCode) {
        return ResponseEntity.ok(institutionService.getInstitutionsByType(typeCode));
    }

    @GetMapping("/{instCode}")
    public ResponseEntity<InstRespDTO> getInstitutionByCode(@PathVariable String instCode) {
        InstRespDTO institution = institutionService.getInstitutionByCode(instCode);
        if (institution == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(institution);
    }

    @GetMapping("/{instCode}/account-types/{accountType}/modules")
    public ResponseEntity<List<AccountTemplateRespDTO>> getAccountModules(
            @PathVariable String instCode, 
            @PathVariable String accountType) {
        List<AccountTemplateRespDTO> templates = institutionService.getAccountTemplates(instCode, accountType);
        return ResponseEntity.ok(templates);
    }
}