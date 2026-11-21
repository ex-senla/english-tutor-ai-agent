package com.hydroyura.eta.chatbot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ChatDataRepository extends JpaRepository<ChatJpa, Long> {
}
