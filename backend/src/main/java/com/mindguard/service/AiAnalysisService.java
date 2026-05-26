package com.mindguard.service;

import com.mindguard.entity.JournalEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class AiAnalysisService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JournalService journalService;

    @Value("${huggingface.api-key:}")
    private String huggingFaceApiKey;

    @Value("${huggingface.api-url:https://api-inference.huggingface.co/models/}")
    private String huggingFaceApiUrl;

    private static final String SENTIMENT_MODEL = "distilbert-base-uncased-finetuned-sst-2-english";
    private static final String ZERO_SHOT_MODEL = "facebook/bart-large-mnli";
    private static final String TEXT_GEN_MODEL = "gpt2";

    public void analyzeEntry(JournalEntry entry) {
        if (entry.getContent() == null || entry.getContent().isEmpty()) {
            log.warn("Journal entry has empty content, skipping analysis");
            return;
        }

        try {
            log.info("[LLM] Starting AI analysis for journal entry: {}", entry.getId());

            log.info("[LLM Call #1] Analyzing sentiment with DistilBERT...");
            Double sentimentScore = analyzeSentiment(entry.getContent());
            log.info("[LLM Call #1] ✅ Sentiment score: {}", sentimentScore);

            log.info("[LLM Call #2] Analyzing distress with Zero-Shot Classification...");
            Double distressLevel = analyzeDistress(entry.getContent());
            log.info("[LLM Call #2] ✅ Distress level: {}", distressLevel);

            entry.setSentimentScore(sentimentScore);
            entry.setDistressLevel(distressLevel);

            if (distressLevel > 5.0) {
                entry.setIsFlagged(true);
                log.info("[LLM Call #3] Distress > 5.0, generating AI analysis with GPT-2...");
                String analysis = generateAiAnalysis(entry.getContent(), sentimentScore, distressLevel);
                log.info("[LLM Call #3] ✅ AI analysis generated");
                entry.setAiAnalysis(analysis);
                log.warn("[ALERT] High distress detected in journal entry: {} (distress: {})", entry.getId(), distressLevel);
            } else {
                log.info("[INFO] Distress level {} below threshold, skipping AI generation", distressLevel);
            }

        } catch (Exception e) {
            log.error("Error performing AI analysis", e);
        }
    }

    private Double analyzeSentiment(String text) {
        try {
            String url = ensureTrailingSlash(huggingFaceApiUrl) + SENTIMENT_MODEL;
            log.info("[LLM Call #1] POST {}", url);

            HttpHeaders headers = createHeaders();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", text);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> response = restTemplate.postForObject(url, request, List.class);

            if (response != null && !response.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> scores = (List<Map<String, Object>>) response.get(0);

                for (Map<String, Object> score : scores) {
                    String label = (String) score.get("label");
                    Double labelScore = ((Number) score.get("score")).doubleValue();

                    if ("POSITIVE".equals(label)) {
                        log.debug("[HF Response] Sentiment label: {}, score: {}", label, labelScore);
                        return labelScore;
                    }
                }
            }

            log.warn("[HF] No sentiment response, returning default 0.5");
            return 0.5;

        } catch (Exception e) {
            log.error("[HF Error] Sentiment analysis failed", e);
            return 0.5;
        }
    }

    private Double analyzeDistress(String text) {
        try {
            String url = ensureTrailingSlash(huggingFaceApiUrl) + ZERO_SHOT_MODEL;
            log.info("[LLM Call #2] POST {}", url);

            String contentPreview = text.length() > 512 ? text.substring(0, 512) : text;

            HttpHeaders headers = createHeaders();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", contentPreview);
            requestBody.put("parameters", Map.of(
                "candidate_labels", "high distress, moderate distress, low distress, no distress"
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null) {
                @SuppressWarnings("unchecked")
                List<String> labels = (List<String>) response.get("labels");
                @SuppressWarnings("unchecked")
                List<Double> scores = (List<Double>) response.get("scores");

                if (labels != null && scores != null && !labels.isEmpty()) {
                    String topLabel = labels.get(0);
                    Double topScore = scores.get(0);

                    log.debug("[HF Response] Distress classification: {} (confidence: {})", topLabel, topScore);

                    if ("high distress".equals(topLabel)) {
                        return 8.0 * topScore;
                    } else if ("moderate distress".equals(topLabel)) {
                        return 5.0 * topScore;
                    } else if ("low distress".equals(topLabel)) {
                        return 2.0 * topScore;
                    }
                }
            }

            log.warn("[HF] No distress classification response, returning default 3.0");
            return 3.0;

        } catch (Exception e) {
            log.error("[HF Error] Distress analysis failed", e);
            return 3.0;
        }
    }

    private String generateAiAnalysis(String content, Double sentimentScore, Double distressLevel) {
        try {
            String url = ensureTrailingSlash(huggingFaceApiUrl) + TEXT_GEN_MODEL;
            log.info("[LLM Call #3] POST {}", url);

            String contentPreview = content.length() > 256 ? content.substring(0, 256) : content;

            String prompt = String.format(
                "Patient journal analysis - Sentiment: %.2f, Distress: %.1f. Analysis: %s Summary: ",
                sentimentScore, distressLevel, contentPreview
            );

            HttpHeaders headers = createHeaders();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", prompt);
            requestBody.put("parameters", Map.of(
                "max_length", 150,
                "num_return_sequences", 1,
                "temperature", 0.7
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> response = restTemplate.postForObject(url, request, List.class);

            if (response != null && !response.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.get(0);
                String generatedText = (String) result.get("generated_text");

                if (generatedText != null) {
                    String analysis = generatedText.replace(prompt, "").trim();
                    log.debug("[HF Response] Generated analysis: {}", analysis);
                    return analysis;
                }
            }

            return buildFallbackAnalysis(sentimentScore, distressLevel);

        } catch (Exception e) {
            log.error("[HF Error] AI analysis generation failed", e);
            return buildFallbackAnalysis(sentimentScore, distressLevel);
        }
    }

    private String buildFallbackAnalysis(Double sentimentScore, Double distressLevel) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("AI Clinical Summary: ");

        if (sentimentScore < 0.3) {
            analysis.append("Negative sentiment detected. ");
        } else if (sentimentScore < 0.7) {
            analysis.append("Neutral to mixed sentiment. ");
        } else {
            analysis.append("Positive sentiment noted. ");
        }

        if (distressLevel > 7.0) {
            analysis.append("CRITICAL DISTRESS LEVEL. Immediate therapist intervention recommended. ");
            analysis.append("Patient requires urgent mental health support and crisis assessment.");
        } else if (distressLevel > 5.0) {
            analysis.append("HIGH DISTRESS INDICATORS. Therapist review recommended. ");
            analysis.append("Monitor for acute symptoms and provide supportive intervention.");
        } else if (distressLevel > 3.0) {
            analysis.append("Moderate distress present. Schedule therapist consultation. ");
            analysis.append("Continue regular monitoring and supportive care.");
        } else {
            analysis.append("Low distress indicators. ");
            analysis.append("Continue regular wellness tracking and preventive care.");
        }

        return analysis.toString();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (huggingFaceApiKey != null && !huggingFaceApiKey.isEmpty()) {
            headers.set("Authorization", "Bearer " + huggingFaceApiKey);
            log.debug("[Auth] Hugging Face API key configured");
        } else {
            log.warn("[Auth] No Hugging Face API key found!");
        }
        return headers;
    }

    private String ensureTrailingSlash(String url) {
        return url != null && !url.endsWith("/") ? url + "/" : url;
    }
}
