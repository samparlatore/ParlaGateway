package com.parlAquatics.gateway.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MicrosecondFormatter extends Formatter {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record) {
        LocalDateTime timestamp = LocalDateTime.ofInstant(
                record.getInstant(), java.time.ZoneId.systemDefault());

        return String.format("[%s] [%s] %s%n",
                formatter.format(timestamp),
                record.getLevel(),
                formatMessage(record));
    }
}
