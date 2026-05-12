package com.gzy.pestdetectionsystem.service.impl.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatStreamContentAccumulatorTest {

    @Test
    void reasoningShouldNotBeCopiedIntoReplyContent() {
        ChatStreamContentAccumulator accumulator = new ChatStreamContentAccumulator();

        accumulator.appendReasoning("先分析病斑形态。");
        accumulator.appendContent("建议补充叶片正反面照片。");

        assertEquals("先分析病斑形态。", accumulator.getReasoningContent());
        assertEquals("建议补充叶片正反面照片。", accumulator.getReplyContent());
    }

    @Test
    void blankChunksShouldBeIgnored() {
        ChatStreamContentAccumulator accumulator = new ChatStreamContentAccumulator();

        accumulator.appendReasoning(" ");
        accumulator.appendContent("");

        assertEquals("", accumulator.getReasoningContent());
        assertEquals("", accumulator.getReplyContent());
    }
}
