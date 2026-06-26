package com.projects.file_loader_service.service;

import com.projects.file_loader_service.model.CallDetailRecord;
import com.projects.file_loader_service.model.CrdLog;
import com.projects.file_loader_service.repository.CdrRepository;
import com.projects.file_loader_service.repository.CrdLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Parses a single CDR file into {@code call_detail_records} rows and records the
 * outcome in {@code cdr_logs}. Bad lines are written to an error file; the source
 * file is moved to the archive folder once processed.
 */

@Service
public class FileProcessorService {

    private static final Logger log = LoggerFactory.getLogger(FileProcessorService.class);

    /** Number of pipe-delimited fields expected per line. */
    private static final int FIELD_COUNT = 33;

    /** RECORD_DATE uses a comma before milliseconds: {@code 2023-08-18 10:00:00,024}. */
    private static final DateTimeFormatter RECORD_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    /** TSTAMP uses a dot and 0-3 fractional digits: {@code 2023-08-18 11:00:00.04}. */
    private static final DateTimeFormatter TSTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Value("${app.loader.archive-folder}")
    private String archiveFolder;

    @Value("${app.loader.error-folder}")
    private String errorFolder;

    private final CdrRepository cdrRepository;
    private final CrdLogRepository cdrLogRepository;


    public FileProcessorService(CdrRepository cdrRepository, CrdLogRepository cdrLogRepository) {
        this.cdrRepository = cdrRepository;
        this.cdrLogRepository = cdrLogRepository;
    }

    public void process(Path file){
        String fileName = file.getFileName().toString();

        //skip files already completed processing.
        boolean alreadyCompleted = cdrLogRepository.findByFileName(fileName)
                .map(l -> "COMPLETED".equals(l.getStatus()))
                .orElse(false);

        if(alreadyCompleted){
            log.info("Skipping already-completed file: {}", fileName);
            return;
        }

        long logId;
        try {
            logId = cdrLogRepository.insert(new CrdLog(fileName, Timestamp.from(Instant.now())));
            log.info("Processing started for file {}...", fileName);
        } catch (DataAccessException e){
            log.warn("could not insert cdr log into db: {}, {}", e, e.getMessage());
            return;
        }

        int successCount = 0;
        int failCount = 0;
        StringBuilder errors = new StringBuilder();

        try {
            List<String> lines = Files.readAllLines(file);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).strip();
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    cdrRepository.insert(parseLine(line));
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.append("Line ").append(i + 1).append(": ")
                            .append(e.getMessage()).append(System.lineSeparator())
                            .append("  > ").append(line).append(System.lineSeparator());
                    log.warn("Failed line {} in {}: {}", i + 1, fileName, e.getMessage());
                }
            }

            if (failCount > 0) {
                writeErrorFile(fileName, errors.toString());
            }

            moveToArchive(file);

            cdrLogRepository.complete(logId, Timestamp.from(Instant.now()),
                    successCount, failCount, "COMPLETED");
            log.info("Finished {}: {} loaded, {} failed", fileName, successCount, failCount);

        } catch (IOException e) {
            cdrLogRepository.complete(logId, Timestamp.from(Instant.now()),
                    successCount, failCount, "ERROR");
            log.error("Could not read file {}: {}, {} ", fileName, e, e.getMessage());
        }
    }

    private CallDetailRecord parseLine(String line) {
        String[] f = line.split("\\|", -1);
        if(f.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + FIELD_COUNT + " fields but found " + f.length);
        }
        return new CallDetailRecord(
                parseTimestamp(f[0], RECORD_DATE_FMT), // RECORD_DATE
                toInt(f[1]),                            // L_SPC
                toInt(f[2]),                            // L_SSN
                toInt(f[3]),                            // L_RI
                toInt(f[4]),                            // L_GT_I
                toStr(f[5]),                            // L_GT_DIGITS
                toInt(f[6]),                            // R_SPC
                toInt(f[7]),                            // R_SSN
                toInt(f[8]),                            // R_RI
                toInt(f[9]),                            // R_GT_I
                toStr(f[10]),                           // R_GT_DIGITS
                toStr(f[11]),                           // SERVICE_CODE
                toInt(f[12]),                           // OR_NATURE
                toInt(f[13]),                           // OR_PLAN
                toStr(f[14]),                           // OR_DIGITS
                toInt(f[15]),                           // DE_NATURE
                toInt(f[16]),                           // DE_PLAN
                toStr(f[17]),                           // DE_DIGITS
                toInt(f[18]),                           // ISDN_NATURE
                toInt(f[19]),                           // ISDN_PLAN
                toStr(f[20]),                           // MSISDN
                toInt(f[21]),                           // VLR_NATURE
                toInt(f[22]),                           // VLR_PLAN
                toStr(f[23]),                           // VLR_DIGITS
                toStr(f[24]),                           // IMSI
                requireNonBlank(f[25], "STATUS"),       // STATUS
                requireNonBlank(f[26], "TYPE"),         // TYPE
                parseTimestamp(normalizeFraction(f[27]), TSTAMP_FMT),      // TSTAMP
                toLong(f[28]),                          // LOCAL_DIALOG_ID
                toLong(f[29]),                          // REMOTE_DIALOG_ID
                toLong(f[30]),                          // DIALOG_DURATION
                toStr(f[31]),                           // USSD_STRING
                requireNonBlank(f[32], "ID")            // ID
        );
    }

    private static Timestamp parseTimestamp(String dateString, DateTimeFormatter dateFormat){
        return Timestamp.valueOf(LocalDateTime.parse(dateString, dateFormat));
    }

    private static Integer toInt(String s) {
        String t = s.strip();
        return t.isEmpty() ? null : Integer.valueOf(t);
    }

    private static String toStr(String s) {
        String t = s.strip();
        return t.isEmpty() ? null : t;
    }

    private static Long toLong(String s) {
        String t = s.strip();
        return t.isEmpty() ? null : Long.valueOf(t);
    }

    private static String requireNonBlank(String s, String field) {
        String t = s.strip();
        if (t.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
        return t;
    }

    /** Pads truncates the dot-fraction of TSTAMP to exactly 3 digits (e.g. {@code .04} -> {@code .040}). */
    private static String normalizeFraction(String raw) {
        String s = raw.strip();
        int dot = s.indexOf('.');
        if (dot < 0) {
            return s + ".000";
        }
        String frac = s.substring(dot + 1);
        if (frac.length() >= 3) {
            frac = frac.substring(0, 3);
        } else {
            frac = (frac + "000").substring(0, 3);
        }
        return s.substring(0, dot + 1) + frac;
    }

    private void moveToArchive(Path file) {
        try {
            Path dir = Path.of(archiveFolder);
            Files.createDirectories(dir);
            log.info("Moving file {} to {}...", file.getFileName(), dir);
            Files.move(file, dir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("Could not archive {}: {}", file.getFileName(), e.getMessage());
        }
    }

    private void writeErrorFile(String fileName, String content) {
        try {
            Path dir = Path.of(errorFolder);
            Files.createDirectories(dir);
            log.info("Logging error to {}", dir.resolve(fileName + ".err"));
            Files.writeString(dir.resolve(fileName + ".err"), content);
        } catch (IOException e) {
            log.warn("Could not write error file for {}: {}", fileName, e.getMessage());
        }
    }

}
