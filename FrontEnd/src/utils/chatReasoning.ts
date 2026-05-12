export interface ChatReasoningParts {
  reasoning: string
  content: string
}

const LEGACY_THINK_BLOCK = /^<think>([\s\S]*?)<\/think>\s*/i

export function splitReasoningContent(
  content: string,
  reasoningContent?: string | null
): ChatReasoningParts {
  const rawContent = content || ''
  const legacyThink = rawContent.match(LEGACY_THINK_BLOCK)
  const contentWithoutLegacyThink = legacyThink
    ? rawContent.slice(legacyThink[0].length).trim()
    : rawContent

  if (reasoningContent && reasoningContent.trim()) {
    const normalizedReasoning = reasoningContent.trim()
    const normalizedContent = contentWithoutLegacyThink.trimStart()
    const displayContent = normalizedContent.startsWith(normalizedReasoning)
      ? normalizedContent.slice(normalizedReasoning.length).trim()
      : contentWithoutLegacyThink

    return {
      reasoning: reasoningContent,
      content: displayContent
    }
  }

  if (legacyThink) {
    return {
      reasoning: legacyThink[1].trim(),
      content: contentWithoutLegacyThink
    }
  }

  return {
    reasoning: '',
    content: rawContent
  }
}
