package com.blog.testgemeni.chat.service;

import com.blog.testgemeni.ext.service.VertexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {
    private final VertexService vertexService;
    public String chat(String content) throws IOException {
        vertexService.quest(content);
        return "TEST";
    }

    public void streamChat(String content, Consumer<String> onText) {
        vertexService.streamQuest(content, onText);
    }
}
