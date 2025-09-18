package com.pujitha.lpt.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pujitha.lpt.dashboard.model.TestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestResultService {

    @Autowired
    private final DynamoDbClient dynamo;

    private final String tableName;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public TestResultService(DynamoDbClient dynamo, @Value("${app.dynamo.table}") String tableName) {
        this.dynamo = dynamo;
        this.tableName = tableName;
    }

    /**
     * Filters by projectName (equals), testEnv (equals) and testType begins_with prefix.
     * NOTE: this uses Scan + FilterExpression.
     */
    public List<TestResult> findByProjectEnvAndTestTypePrefixUsingScan(String projectName, String testEnv, String testTypePrefix, Integer limit) {
        // Build filter: projectName = :p AND testEnv = :e AND begins_with(testType, :t)
        String filterExpression = "projectName = :p and testEnv = :e and begins_with(testType, :t)";

        Map<String, AttributeValue> exprValues = new HashMap<>();
        exprValues.put(":p", AttributeValue.builder().s(projectName).build());
        exprValues.put(":e", AttributeValue.builder().s(testEnv).build());
        exprValues.put(":t", AttributeValue.builder().s(testTypePrefix).build());

        ScanRequest.Builder scanBuilder = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression(filterExpression)
                .expressionAttributeValues(exprValues);

        if (limit != null && limit > 0) {
            scanBuilder.limit(limit);
        }

        List<TestResult> results = new ArrayList<>();
        ScanRequest request = scanBuilder.build();

        // handle pagination of scan
        ScanResponse response = dynamo.scan(request);
        results.addAll(mapItems(response.items()));

        while (response.lastEvaluatedKey() != null && !response.lastEvaluatedKey().isEmpty()) {
            response = dynamo.scan(ScanRequest.builder()
                    .tableName(tableName)
                    .filterExpression(filterExpression)
                    .expressionAttributeValues(exprValues)
                    .exclusiveStartKey(response.lastEvaluatedKey())
                    .build());
            results.addAll(mapItems(response.items()));
            // optional: stop early if limit reached
        }

        return results;
    }

    public List<TestResult> findByProjectEnvAndTestTypePrefix(
            String projectName, String testEnv, String testTypePrefix, Integer limit) {

        String projectEnvKey = projectName + "#" + testEnv;
        String keyCondition = "projectEnvKey = :pk AND begins_with(#sk, :skPrefix)";

        Map<String,String> exprNames = new HashMap<>();
        exprNames.put("#sk", "testType#testDateTime");

        Map<String, AttributeValue> exprValues = new HashMap<>();
        exprValues.put(":pk", AttributeValue.builder().s(projectEnvKey).build());
        exprValues.put(":skPrefix", AttributeValue.builder().s(testTypePrefix).build());

        QueryRequest.Builder queryBuilder = QueryRequest.builder()
                .tableName(tableName)
                .indexName("GSI_ProjectEnv")
                .keyConditionExpression(keyCondition)
                .expressionAttributeNames(exprNames)
                .expressionAttributeValues(exprValues)
                .scanIndexForward(true); // ascending by testDateTime

        if (limit != null && limit > 0) queryBuilder.limit(limit);

        QueryResponse response = dynamo.query(queryBuilder.build());
        List<TestResult> results = mapItems(response.items());

        while (response.lastEvaluatedKey() != null && !response.lastEvaluatedKey().isEmpty()) {
            response = dynamo.query(queryBuilder
                    .exclusiveStartKey(response.lastEvaluatedKey())
                    .build());
            results.addAll(mapItems(response.items()));
        }

        return results;
    }

    private List<TestResult> mapItems(List<Map<String, AttributeValue>> items) {
        return items.stream().map(this::mapItem).collect(Collectors.toList());
    }

    private TestResult mapItem(Map<String, AttributeValue> item) {
        TestResult tr = new TestResult();
        tr.setTestResultId(getString(item, "testResultId"));
        tr.setProjectName(getString(item, "projectName"));
        tr.setTestEnv(getString(item, "testEnv"));
        tr.setTestType(getString(item, "testType"));
        tr.setTestDateTime(getString(item, "testDateTime"));

        tr.setErrorRate(getDouble(item, "errorRate"));
        tr.setAverageResponseTime(getDouble(item, "averageResponseTime"));
        tr.setThroughput(getDouble(item, "throughput"));

        // if there's a nested JSON attribute named "data" or "jsonData", attempt to parse
        if (item.containsKey("jsonData") && item.get("jsonData").hasM()) {
            try {
                tr.setJsonData(mapper.convertValue(item.get("jsonData").m(), Map.class));
            } catch (Exception ex) {
                // ignore
            }
        } else if (item.containsKey("data") && item.get("data").hasSs()) {
            // perhaps it's stored as a JSON string
            try {
                tr.setJsonData(mapper.readValue(item.get("data").s(), Map.class));
            } catch (Exception ignored) {}
        }

        return tr;
    }

    private String getString(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) && item.get(key).s() != null ? item.get(key).s() : null;
    }

    private Double getDouble(Map<String, AttributeValue> item, String key) {
        if (!item.containsKey(key)) return null;
        AttributeValue av = item.get(key);
        if (av.n() != null && !av.n().isEmpty()) {
            try { return Double.parseDouble(av.n()); } catch (Exception e) { return null; }
        }
        if (av.s() != null && !av.s().isEmpty()) {
            try { return Double.parseDouble(av.s()); } catch (Exception e) { return null; }
        }
        return null;
    }
}
