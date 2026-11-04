package com.workshop.loanservice.controller;

import com.workshop.loanservice.dto.BorrowerDto;
import com.workshop.loanservice.dto.BorrowerExposureDto;
import com.workshop.loanservice.service.ExposureService;
import com.workshop.loanservice.service.LoanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/borrowers")
public class BorrowerController {

    private final LoanService loanService;
    private final ExposureService exposureService;

    public BorrowerController(LoanService loanService, ExposureService exposureService) {
        this.loanService = loanService;
        this.exposureService = exposureService;
    }

    @GetMapping
    public List<BorrowerDto> getAllBorrowers() {
        return loanService.getAllBorrowers();
    }

    @GetMapping("/{id}")
    public BorrowerDto getBorrower(@PathVariable String id) {
        return loanService.getBorrowerById(id);
    }

    @GetMapping("/{id}/exposure")
    public BorrowerExposureDto getExposure(@PathVariable String id) {
        return exposureService.getExposure(id);
    }
}
