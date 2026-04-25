package com.gzy.pestdetectionsystem.mapper.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gzy.pestdetectionsystem.entity.chat.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
