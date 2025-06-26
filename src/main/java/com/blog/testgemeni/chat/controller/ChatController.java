package com.blog.testgemeni.chat.controller;

import com.blog.testgemeni.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "chat", description = "채팅 API")
@RequestMapping("chat")
public class ChatController {
    private final ChatService chatService;

    @GetMapping(value = "/quest", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "질문", description = "질문")
    public String chat(@Schema(description = "content", example = "테스트", name = "content") String content) throws IOException {
        return chatService.chat(content);
    }


    @GetMapping(value = "/streamChat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "질문", description = "질문")
    public Flux<String> streamChat(@Schema(description = "content", example = "테스트", name = "content") String content) {
        return Flux.create(emitter -> {
            try {
                chatService.streamChat(content, emitter::next);
                emitter.complete();
            } catch (Exception e) {
                log.error("Error during chat streaming", e);
                emitter.error(e);
            }
        });
    }
}
