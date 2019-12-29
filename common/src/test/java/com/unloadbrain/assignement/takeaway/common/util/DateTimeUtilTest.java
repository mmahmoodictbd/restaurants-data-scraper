package com.unloadbrain.assignement.takeaway.common.util;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertTrue;

public class DateTimeUtilTest {

    @Test
    public void shouldReturnCurrentTime() {

        // Given

        long timeDiffBetweenCalls = 10;
        DateTimeUtil dateTimeUtil = new DateTimeUtil();

        // When
        long nowInEpochMilli = dateTimeUtil.getCurrentTimeEpochMilli();

        // Then
        assertTrue(Instant.now().getEpochSecond() - nowInEpochMilli < timeDiffBetweenCalls);
    }
}