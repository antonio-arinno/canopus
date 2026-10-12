package com.arinno.canopus.reporting.contract;

public record ProductMetrics(
        Integer countProjects,
        Integer countContributors,
        Integer time,
        Float avgTime,
        Integer avgDuration) {
}