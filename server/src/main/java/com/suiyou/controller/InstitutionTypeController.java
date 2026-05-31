
package com.suiyou.controller;

import com.suiyou.dto.account.InstitutionTypeRespDTO;
import com.suiyou.service.SysInstitutionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/institution-types")
public class InstitutionTypeController {

    @Autowired
    private SysInstitutionTypeService institutionTypeService;

    @GetMapping
    public ResponseEntity<List<InstitutionTypeRespDTO>> getAllInstitutionTypes() {
        return new ResponseEntity<>(institutionTypeService.getAllInstitutionTypes(), HttpStatus.OK);
    }

    @GetMapping("/{typeCode}")
    public ResponseEntity<InstitutionTypeRespDTO> getInstitutionTypeByCode(@PathVariable String typeCode) {
        InstitutionTypeRespDTO type = institutionTypeService.getInstitutionTypeByCode(typeCode);
        if (type == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(type, HttpStatus.OK);
    }
}
