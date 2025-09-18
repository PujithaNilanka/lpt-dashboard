package com.pujitha.lpt.dashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Autowired;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestResult {
    private String testResultId;
    private String projectName;
    private String testEnv;
    private String testType;
    private String testDateTime; // ISO-8601 string expected
    private Double errorRate;
    private Double averageResponseTime;
    private Double throughput;
    private Object jsonData; // original JSON object, optional

    // getters / setters

    public String getTestResultId() { return testResultId; }
    public void setTestResultId(String testResultId) { this.testResultId = testResultId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getTestEnv() { return testEnv; }
    public void setTestEnv(String testEnv) { this.testEnv = testEnv; }

    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }

    public String getTestDateTime() { return testDateTime; }
    public void setTestDateTime(String testDateTime) { this.testDateTime = testDateTime; }

    public Double getErrorRate() { return errorRate; }
    public void setErrorRate(Double errorRate) { this.errorRate = errorRate; }

    public Double getAverageResponseTime() { return averageResponseTime; }
    public void setAverageResponseTime(Double averageResponseTime) { this.averageResponseTime = averageResponseTime; }

    public Double getThroughput() { return throughput; }
    public void setThroughput(Double throughput) { this.throughput = throughput; }

    public Object getJsonData() { return jsonData; }
    public void setJsonData(Object jsonData) { this.jsonData = jsonData; }
}
