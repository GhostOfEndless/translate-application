package com.example.service;

import com.example.client.YandexCloudRestClient;
import com.example.client.payload.Language;
import com.example.client.payload.Translation;
import com.example.exceptions.InvalidLanguageCodeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

    private final YandexCloudRestClient restClient;
    private final Integer translationPoolThreads;
    private final Integer requestsLimit;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private Semaphore semaphore;
    private Set<String> availableLanguages;

    @PostConstruct
    private void init() {
        this.availableLanguages = new HashSet<>(
                this.restClient.getAvailableLanguages().stream().map(Language::code).toList()
        );
        this.executorService = Executors.newFixedThreadPool(translationPoolThreads);
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.semaphore = new Semaphore(requestsLimit);
        this.scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (semaphore.availablePermits() < requestsLimit) {
                semaphore.release(requestsLimit - semaphore.availablePermits());
            }
        }, 1000L - System.currentTimeMillis() % 1000L, 1000L, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    private void shutdown() throws InterruptedException {
        this.executorService.shutdown();
        boolean threadPoolFinished = executorService.awaitTermination(1, TimeUnit.MINUTES);
        log.info("ThreadPool shutdown completed, success={}", threadPoolFinished);

        this.scheduledExecutorService.shutdown();
        boolean scheduledPoolFinished = scheduledExecutorService.awaitTermination(1, TimeUnit.MINUTES);
        log.info("ScheduledThreadPool shutdown completed, success={}", scheduledPoolFinished);
    }

    @Override
    public Translation translate(String sourceLanguageCode, String targetLanguageCode, String text)
            throws InvalidLanguageCodeException {
        if (!this.availableLanguages.contains(sourceLanguageCode)) {
            throw new InvalidLanguageCodeException(sourceLanguageCode);
        } else if (!this.availableLanguages.contains(targetLanguageCode)) {
            throw new InvalidLanguageCodeException(targetLanguageCode);
        }

        var words = Arrays.stream(text.split("\\s+"))
                .map(String::trim)
                .filter(trim -> !trim.isEmpty())
                .toArray(String[]::new);
        var results = new String[words.length];

        List<CompletableFuture<Void>> futures = IntStream.range(0, words.length)
                .mapToObj(index -> CompletableFuture.runAsync(() -> {
                    try {
                        semaphore.acquire();
                        var result = this.restClient.translateText(
                                sourceLanguageCode, targetLanguageCode, words[index]);
                        synchronized (results) {
                            results[index] = result;
                        }
                    } catch (RestClientException | InterruptedException e) {
                        log.error(e.getMessage());
                    }
                }, executorService))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return new Translation(String.join(" ", results));
    }
}
