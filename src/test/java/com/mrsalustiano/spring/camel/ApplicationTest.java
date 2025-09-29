package com.mrsalustiano.spring.camel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class ApplicationTest {



    @Test
    void mainMethodRuns() {
        try (var mocked = mockStatic(SpringApplication.class)) {
            Application.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(Application.class, new String[]{}));
        }
    }
}