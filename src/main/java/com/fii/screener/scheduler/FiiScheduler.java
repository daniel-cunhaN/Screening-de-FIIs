package com.fii.screener.scheduler;

import com.fii.screener.service.BrapiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FiiScheduler {

    private final BrapiService brapiService;

    // Executa todo dia às 18:00 (Segunda a Sexta)
    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void scheduleFiiUpdate() {
        log.info("Starting scheduled FII update...");
        brapiService.updateFiiData();
        log.info("Scheduled FII update finished.");
    }
}
