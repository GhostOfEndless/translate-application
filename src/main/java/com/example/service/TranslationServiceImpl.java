package com.example.service;

import com.example.client.YandexCloudRestClient;
import com.example.client.payload.LanguagePayload;
import com.example.client.payload.TranslationPayload;
import com.example.entity.Translation;
import com.example.exceptions.InvalidLanguageCodeException;
import com.example.exceptions.ProcessedSymbolsLimitException;
import com.example.repository.TranslationRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

    private final TranslationRepository translationRepository;
    private final YandexCloudRestClient restClient;
    private final Integer translationPoolThreadsNum;
    private final Integer requestsLimit;
    private final Integer symbolsLimit;
    private ExecutorService translationPool;
    private ScheduledExecutorService semaphoreReleasingPool;
    private ScheduledExecutorService symbolsLimitReleasingPool;
    private AtomicInteger availableSymbols;
    private Semaphore semaphore;
    private Set<String> availableLanguages;

    @PostConstruct
    private void init() {
        this.availableLanguages = new HashSet<>(
                this.restClient.getAvailableLanguages().stream().map(LanguagePayload::code).toList()
        );

        this.semaphore = new Semaphore(this.requestsLimit);
        this.availableSymbols = new AtomicInteger(this.symbolsLimit);

        this.translationPool = Executors.newFixedThreadPool(this.translationPoolThreadsNum);
        this.semaphoreReleasingPool = Executors.newSingleThreadScheduledExecutor();
        this.symbolsLimitReleasingPool = Executors.newSingleThreadScheduledExecutor();

        runScheduledThreads();
    }

    private void runScheduledThreads() {
        this.semaphoreReleasingPool.scheduleAtFixedRate(() -> {
            if (this.semaphore.availablePermits() < this.requestsLimit) {
                this.semaphore.release(this.requestsLimit - this.semaphore.availablePermits());
            }
        }, 1000L - System.currentTimeMillis() % 1000L, 1000L, TimeUnit.MILLISECONDS);

        this.symbolsLimitReleasingPool.scheduleAtFixedRate(() -> this.availableSymbols.set(this.symbolsLimit),
                1000L - System.currentTimeMillis() % 1000L, 3600000L, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @PreDestroy
    private void shutdown() throws InterruptedException {
        this.translationPool.shutdown();
        this.translationPool.awaitTermination(1, TimeUnit.MINUTES);

        this.semaphoreReleasingPool.shutdown();
        this.semaphoreReleasingPool.awaitTermination(1, TimeUnit.MINUTES);

        this.symbolsLimitReleasingPool.shutdown();
        this.symbolsLimitReleasingPool.awaitTermination(1, TimeUnit.MINUTES);
    }

    @Override
    public TranslationPayload translate(String clientIP, Timestamp requestTimestamp,
                                        String sourceLanguageCode, String targetLanguageCode, String sourceText)
            throws InvalidLanguageCodeException, ProcessedSymbolsLimitException {
        if (availableLanguages == null || availableLanguages.isEmpty()) {
            throw new RestClientException("");
        }
        if (!this.availableLanguages.contains(sourceLanguageCode)) {
            throw new InvalidLanguageCodeException(sourceLanguageCode);
        } else if (!this.availableLanguages.contains(targetLanguageCode)) {
            throw new InvalidLanguageCodeException(targetLanguageCode);
        }

        var words = parseWords(sourceText);
        var results = getTranslations(words, sourceLanguageCode, targetLanguageCode);

        String translatedText = String.join(" ", results);
        saveTranslation(clientIP, requestTimestamp, sourceLanguageCode, targetLanguageCode,
                sourceText, translatedText);

        return new TranslationPayload(translatedText);
    }

    private String[] parseWords(String text) throws ProcessedSymbolsLimitException {
        var words = Arrays.stream(text.split("\\s+"))
                .map(String::trim)
                .filter(trim -> !trim.isEmpty())
                .toArray(String[]::new);

        int wordsLen = Arrays.stream(words)
                .mapToInt(String::length)
                .sum();

        if (this.availableSymbols.get() >= wordsLen) {
            this.availableSymbols.addAndGet(-wordsLen);
        } else {
            throw new ProcessedSymbolsLimitException(this.symbolsLimit);
        }

        return words;
    }

    private String[] getTranslations(String[] words, String sourceLanguageCode, String targetLanguageCode) {
        var results = new String[words.length];

        List<CompletableFuture<Void>> futures = IntStream.range(0, words.length)
                .mapToObj(index -> CompletableFuture.runAsync(() -> {
                    try {
                        this.semaphore.acquire();
                        var result = this.restClient.translateText(
                                sourceLanguageCode, targetLanguageCode, words[index]);
                        synchronized (results) {
                            results[index] = result;
                        }
                    } catch (RestClientException | InterruptedException e) {
                        log.error(e.getMessage());
                    }
                }, this.translationPool))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return results;
    }

    private void saveTranslation(String clientIP, Timestamp requestTimestamp,
                                 String sourceLanguageCode, String targetLanguageCode,
                                 String sourceText, String translatedText) {
        this.translationRepository.save(
                Translation.builder()
                        .clientIP(clientIP)
                        .sourceLanguageCode(sourceLanguageCode)
                        .targetLanguageCode(targetLanguageCode)
                        .sourceText(sourceText)
                        .translatedText(translatedText)
                        .requestTimestamp(requestTimestamp)
                        .responseTimestamp(Timestamp.from(Instant.now()))
                        .build());
    }
}
