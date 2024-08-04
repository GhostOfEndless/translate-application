package com.example.service;

import com.example.client.YandexCloudRestClient;
import com.example.client.payload.Language;
import com.example.client.payload.Translation;
import com.example.exceptions.InvalidLanguageCodeException;
import com.example.exceptions.ProcessedSymbolsLimitException;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

    private final YandexCloudRestClient restClient;
    private final Integer translationPoolThreads;
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
                this.restClient.getAvailableLanguages().stream().map(Language::code).toList()
        );
        this.translationPool = Executors.newFixedThreadPool(this.translationPoolThreads);
        this.semaphore = new Semaphore(this.requestsLimit);
        availableSymbols = new AtomicInteger(this.symbolsLimit);

        this.semaphoreReleasingPool = Executors.newSingleThreadScheduledExecutor();
        this.semaphoreReleasingPool.scheduleAtFixedRate(() -> {
            if (this.semaphore.availablePermits() < this.requestsLimit) {
                this.semaphore.release(this.requestsLimit - this.semaphore.availablePermits());
            }
        }, 1000L - System.currentTimeMillis() % 1000L, 1000L, TimeUnit.MILLISECONDS);

        this.symbolsLimitReleasingPool = Executors.newSingleThreadScheduledExecutor();
        this.symbolsLimitReleasingPool.scheduleAtFixedRate(() -> {
            this.availableSymbols.set(this.symbolsLimit);
            log.info("Symbols limit was set to: {}", this.symbolsLimit);
        }, 1000L - System.currentTimeMillis() % 1000L, 3600000L, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    private void shutdown() throws InterruptedException {
        this.translationPool.shutdown();
        boolean translationPoolFinished = this.translationPool.awaitTermination(1, TimeUnit.MINUTES);
        log.info("ThreadPool shutdown completed, success={}", translationPoolFinished);

        this.semaphoreReleasingPool.shutdown();
        boolean semaphorePoolFinished = this.semaphoreReleasingPool.awaitTermination(1, TimeUnit.MINUTES);
        log.info("SemaphoreReleasingPool shutdown completed, success={}", semaphorePoolFinished);

        this.symbolsLimitReleasingPool.shutdown();
        boolean symbolsLimitPoolFinished = this.symbolsLimitReleasingPool.awaitTermination(1, TimeUnit.MINUTES);
        log.info("SymbolsLimitReleasingPool shutdown completed, success={}", symbolsLimitPoolFinished);
    }

    @Override
    public Translation translate(String sourceLanguageCode, String targetLanguageCode, String text)
            throws InvalidLanguageCodeException, ProcessedSymbolsLimitException {
        if (!this.availableLanguages.contains(sourceLanguageCode)) {
            throw new InvalidLanguageCodeException(sourceLanguageCode);
        } else if (!this.availableLanguages.contains(targetLanguageCode)) {
            throw new InvalidLanguageCodeException(targetLanguageCode);
        }

        var words = Arrays.stream(text.split("\\s+"))
                .map(String::trim)
                .filter(trim -> !trim.isEmpty())
                .toArray(String[]::new);

        int wordsLen = Arrays.stream(words)
                .mapToInt(String::length)
                .sum();

        if (this.availableSymbols.get() >= wordsLen) {
            this.availableSymbols.addAndGet(-wordsLen);
            log.info("Available symbols: {}", availableSymbols.get());
        } else {
            throw new ProcessedSymbolsLimitException(this.symbolsLimit);
        }

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

        return new Translation(String.join(" ", results));
    }
}
