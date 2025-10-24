package com.example.aigreentick.services.messaging.flow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aigreentick.services.messaging.flow.model.FlowSubmission;



public interface FlowSubmissionRepository extends JpaRepository<FlowSubmission, Long> {
// add custom queries if needed
}
