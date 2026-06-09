package com.suiyou.controller;

import com.suiyou.dto.account.InstitutionModuleRespDTO;
import com.suiyou.dto.account.InstitutionRespDTO;
import com.suiyou.dto.account.InstitutionTypeRespDTO;
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

    @GetMapping("")
    public ResponseEntity<List<InstitutionRespDTO>> getAllInstitutions() {
        return new ResponseEntity<>(institutionService.getAllInstitutions(), HttpStatus.OK);
    }

    @GetMapping("/type/{typeCode}")
    public ResponseEntity<List<InstitutionRespDTO>> getInstitutionsByType(@PathVariable String typeCode) {
        return new ResponseEntity<>(institutionService.getInstitutionsByType(typeCode), HttpStatus.OK);
    }

    @GetMapping("/hot")
    public ResponseEntity<List<InstitutionRespDTO>> getHotInstitutions() {
        return new ResponseEntity<>(institutionService.getHotInstitutions(), HttpStatus.OK);
    }

    @GetMapping("/{instCode}")
    public ResponseEntity<InstitutionRespDTO> getInstitutionByCode(@PathVariable String instCode) {
        InstitutionRespDTO institution = institutionService.getInstitutionByCode(instCode);
        if (institution == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(institution, HttpStatus.OK);
    }

    @GetMapping("/{instCode}/modules")
    public ResponseEntity<InstitutionModuleRespDTO> getInstitutionModules(@PathVariable String instCode) {
        InstitutionModuleRespDTO modules = institutionService.getInstitutionModules(instCode);
        if (modules == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(modules, HttpStatus.OK);
    }

    @GetMapping("/{instCode}/account-types")
    public ResponseEntity<List<String>> getAccountTypes(@PathVariable String instCode) {
        List<String> types = institutionService.getAccountTypesByInstCode(instCode);
        if (types == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(types, HttpStatus.OK);
    }

    @GetMapping("/types")
    public ResponseEntity<List<InstitutionTypeRespDTO>> getAllInstitutionTypes() {
        return new ResponseEntity<>(institutionService.getAllInstitutionTypes(), HttpStatus.OK);
    }

    @GetMapping("/types/{typeCode}")
    public ResponseEntity<InstitutionTypeRespDTO> getInstitutionTypeByCode(@PathVariable String typeCode) {
        InstitutionTypeRespDTO type = institutionService.getInstitutionTypeByCode(typeCode);
        if (type == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(type, HttpStatus.OK);
    }
}