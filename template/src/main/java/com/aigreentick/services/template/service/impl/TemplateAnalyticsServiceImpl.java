package com.aigreentick.services.template.service.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.aigreentick.services.template.dto.TemplateStatsDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateAnalyticsServiceImpl {
    private final MongoTemplate mongoTemplate;

    // Give analystical information abouth template of user if userId is not present
    // then give about all
    public TemplateStatsDto getTemplateStats(Long userId) {
        try {

            Criteria baseCriteria = Criteria.where("isDeleted").is(false);

            // if userId is not provided it give stats of all users
            if (userId != null) {
                baseCriteria.and("user_id").is(userId);
            }

            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(baseCriteria),
                    Aggregation.facet(
                            Aggregation.count().as("count") // total
                    ).as("total")
                            .and(Aggregation.group("status").count().as("count")).as("byStatus")
                            .and(Aggregation.group("category").count().as("count")).as("byCategory"));

            AggregationResults<Document> aggResults = mongoTemplate.aggregate(aggregation, "templates", Document.class);

            Document result = aggResults.getUniqueMappedResult();

            // Prepare defaults
            long total = 0L;
            Map<String, Long> statusCounts = new LinkedHashMap<>();
            Map<String, Long> categoryCounts = new LinkedHashMap<>();

            if (result != null) {
                // total
                List<Document> totalList = result.getList("total", Document.class);
                if (totalList != null && !totalList.isEmpty()) {
                    Number n = (Number) totalList.get(0).get("count");
                    total = n == null ? 0L : n.longValue();
                }

                // status breakdown
                List<Document> statusList = result.getList("byStatus", Document.class);
                if (statusList != null) {
                    for (Document d : statusList) {
                        Object id = d.get("_id");
                        String key = id == null ? "UNKNOWN" : id.toString();
                        Number c = (Number) d.get("count");
                        statusCounts.put(key, c == null ? 0L : c.longValue());
                    }
                }

                // category breakdown
                List<Document> categoryList = result.getList("byCategory", Document.class);
                if (categoryList != null) {
                    for (Document d : categoryList) {
                        Object id = d.get("_id");
                        String key = id == null ? "UNKNOWN" : id.toString();
                        Number c = (Number) d.get("count");
                        categoryCounts.put(key, c == null ? 0L : c.longValue());
                    }
                }
            }

            return new TemplateStatsDto(total, statusCounts, categoryCounts);

        } catch (Exception ex) {
            // Be defensive: return zeroed DTO and log the error
            log.error("Error while computing template stats for userId={}: {}", userId, ex.getMessage(), ex);
            return new TemplateStatsDto(0L, Collections.emptyMap(), Collections.emptyMap());
        }
    }

}
