package com.hydroyura.eta.chatbot.infrastructure.persistence;

import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "chat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatJpa {

    @Id
    @Column(name = "chat_id", nullable = false)
    Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 64)
    ChatState state;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context", nullable = false)
    String context;
}
