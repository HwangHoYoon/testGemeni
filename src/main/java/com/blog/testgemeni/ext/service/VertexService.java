package com.blog.testgemeni.ext.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Service
@Slf4j
public class VertexService {
    // Vertex AI와의 통신을 위한 서비스 메소드들을 구현합니다.
    // 예: 질문을 보내고 응답을 받는 메소드 등

    public String quest(String content) throws IOException {
        // Vertex AI에 질문을 보내고 응답을 받는 로직을 구현합니다.
        // 현재는 테스트용으로 간단한 문자열을 반환합니다.

        String projectId = "named-totality-462512-j1";
        String location = "us-central1"; // 예: "us-central1"

        //String modelName = "gemini-2.5-pro-preview-06-05"; // 텍스트 모델
        String modelName = "gemini-2.0-flash-001"; // 텍스트 모델

        // VertexAiGeminiChatClient
        // String modelName = "gemini-1.0-pro-vision"; // 멀티모달(텍스트+이미지) 모델

        //String promptText = "인공지능이 세상을 어떻게 바꿀지에 대해 3문단으로 설명해줘.";

        System.out.println("스트리밍 응답을 시작합니다...");
        System.out.println("========================================");

        generateContentStreaming(projectId, location, modelName, content);

        System.out.println("\n========================================");
        System.out.println("스트리밍 응답이 완료되었습니다.");

        return "Vertex AI 응답: " + content;
    }

    public void generateContentStreaming(
            String projectId, String location, String modelName, String content) throws IOException {

        try (VertexAI vertexAi = new VertexAI(projectId, location)) {

            // 1. 시스템 안내 (System Instruction) 정의
            // 모델의 역할과 출력 규칙을 명시합니다.
            Content systemInstruction = Content.newBuilder()
                    .addParts(Part.newBuilder().setText(
                            "너는 음식 추천 전문가야. 사용자가 재료를 말하면, 그 재료로 만들 수 있는 창의적인 요리 이름과 간단한 1줄 설명을 JSON 형식으로 응답해야 해. JSON 키는 'recipe_name'과 'description'을 사용해줘."))
                    .build();

            // 2. 퓨샷(Few-Shot) 예시 정의
            // 모델에게 원하는 답변의 스타일과 구조를 보여주기 위한 질문/답변 쌍입니다.
            List<Content> fewShotExamples = Arrays.asList(
                    // 예시 1
                    Content.newBuilder()
                            .setRole("user")
                            .addParts(Part.newBuilder().setText("돼지고기, 김치"))
                            .build(),
                    Content.newBuilder()
                            .setRole("model")
                            .addParts(Part.newBuilder().setText("```json\n{\"recipe_name\": \"김치 짜글이\", \"description\": \"돼지고기와 김치를 자작하게 끓여낸 매콤한 밥도둑 찌개\"}\n```"))
                            .build(),
                    // 예시 2
                    Content.newBuilder()
                            .setRole("user")
                            .addParts(Part.newBuilder().setText("계란, 아보카도"))
                            .build(),
                    Content.newBuilder()
                            .setRole("model")
                            .addParts(Part.newBuilder().setText("```json\n{\"recipe_name\": \"아보카도 에그인헬\", \"description\": \"부드러운 아보카도와 계란을 토마토 소스와 함께 즐기는 지중해식 브런치\"}\n```"))
                            .build()
            );

            // 3. 최종 사용자 프롬프트(User Prompt) 정의
            // 모델에게 실제로 답변을 요청할 질문입니다.
            Content finalUserPrompt = Content.newBuilder()
                    .setRole("user")
                    .addParts(Part.newBuilder().setText(content))
                    .build();

            // 4. 대화 기록(Chat History) 통합
            // 퓨샷 예시와 최종 프롬프트를 하나의 리스트로 결합합니다.
            List<Content> chatHistory = new ArrayList<>(fewShotExamples);
            chatHistory.add(finalUserPrompt);

            // 5. 모델 초기화 및 시스템 안내 설정
            // GenerativeModel.Builder를 사용하여 모델을 생성할 때 시스템 안내를 주입합니다.
            GenerativeModel model = new GenerativeModel.Builder()
                    .setModelName(modelName)
                    .setVertexAi(vertexAi)
                    .setSystemInstruction(systemInstruction) // 여기에 시스템 안내를 설정합니다.
                    .build();

            // 6. 통합된 대화 기록으로 스트리밍 API 호출
            ResponseStream<GenerateContentResponse> responseStream = model.generateContentStream(chatHistory);

            // 응답 스트림 처리
            for (GenerateContentResponse response : responseStream) {
                log.info(ResponseHandler.getText(response));
            }
        }
    }

    public void streamQuest(String content, Consumer<String> onText) {
        String projectId = "named-totality-462512-j1";
        String location = "us-central1"; // 예: "us-central1"

        //String modelName = "gemini-2.5-pro-preview-06-05"; // 텍스트 모델
        String modelName = "gemini-2.0-flash-001";

        try (VertexAI vertexAi = new VertexAI(projectId, location)) {

            GenerativeModel model = new GenerativeModel.Builder()
                    .setModelName(modelName)
                    .setVertexAi(vertexAi)
                    .build();

            Content prompt = Content.newBuilder()
                    .setRole("user")
                    .addParts(Part.newBuilder().setText(content))
                    .build();

            GenerationConfig config = GenerationConfig.newBuilder()
                    .setTemperature(0.7f)
                    .setTopP(0.8f)
                    .build();

            model.withGenerationConfig(config);

            ResponseStream<GenerateContentResponse> stream = model.generateContentStream(prompt);
            for (GenerateContentResponse response : stream) {
                onText.accept(ResponseHandler.getText(response));
            }
        }
        catch (IOException e) {
            log.error("Vertex AI 스트리밍 요청 중 오류 발생: {}", e.getMessage());
        }
        catch (Exception e) {
            log.error("Vertex AI 스트리밍 요청 중 예외 발생: {}", e.getMessage());
        }
        finally {
            log.info("스트리밍 요청이 완료되었습니다.");
        }
    }
}
