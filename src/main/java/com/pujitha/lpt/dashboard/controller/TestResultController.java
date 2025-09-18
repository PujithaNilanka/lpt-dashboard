package com.pujitha.lpt.dashboard.controller;

import com.pujitha.lpt.dashboard.model.TestResult;
import com.pujitha.lpt.dashboard.service.TestResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TestResultController {

    private final TestResultService service;

    public TestResultController(TestResultService service) {
        this.service = service;
    }

    @GetMapping("/results")
    public ResponseEntity<List<TestResult>> getResults(
            @RequestParam String projectName,
            @RequestParam String testEnv,
            @RequestParam String testTypePrefix,
            @RequestParam(required = false) Integer limit
    ) {
        List<TestResult> items = service.findByProjectEnvAndTestTypePrefix(projectName, testEnv, testTypePrefix, limit);

        // default sort by testDateTime ascending; try to parse as ISO-8601 lexicographic sort is OK for ISO strings
        items.sort(Comparator.comparing(TestResult::getTestDateTime, Comparator.nullsLast(Comparator.naturalOrder())));

        return ResponseEntity.ok(items);
    }
}
