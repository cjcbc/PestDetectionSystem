package com.gzy.pestdetectionsystem.service.impl.chat;

class ChatStreamContentAccumulator {
    private final StringBuilder replyContent = new StringBuilder();
    private final StringBuilder reasoningContent = new StringBuilder();

    void appendContent(String content) {
        if (content != null && !content.isBlank()) {
            replyContent.append(content);
        }
    }

    void appendReasoning(String reasoning) {
        if (reasoning != null && !reasoning.isBlank()) {
            reasoningContent.append(reasoning);
        }
    }

    String getReplyContent() {
        return replyContent.toString();
    }

    String getReasoningContent() {
        return reasoningContent.toString();
    }
}
