package com.projects.file_loader_service.repository;

import com.projects.file_loader_service.model.CrdLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcCrdLogRepository implements CrdLogRepository {

    private final JdbcTemplate jdbc;

    public JdbcCrdLogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<CrdLog> rowMapper = (rs, rowNum) -> {
        CrdLog log = new CrdLog();
        log.setId(rs.getLong("id"));
        log.setFileName(rs.getString("file_name"));
        log.setStartTime(rs.getTimestamp("start_time"));
        log.setEndTime(rs.getTimestamp("end_time"));
        log.setSuccessCount(rs.getInt("success_count"));
        log.setFailCount(rs.getInt("fail_count"));
        log.setStatus(rs.getString("status"));
        return log;
    };

    @Override
    public Optional<CrdLog> findByFileName(String fileName) {
        List<CrdLog> results = jdbc.query(
                "SELECT * FROM cdr_logs WHERE file_name = ?",
                rowMapper, fileName);
        return results.stream().findFirst();
    }

    @Override
    public long insert(CrdLog log) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO cdr_logs (file_name, start_time, status) VALUES (?, ?, ?)",
                    new String[]{"id"});
            ps.setString(1, log.getFileName());
            ps.setTimestamp(2, log.getStartTime());
            ps.setString(3, log.getStatus());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void complete(long id, Timestamp endTime, int successCount, int failCount, String status) {
        jdbc.update(
                "UPDATE cdr_logs SET end_time = ?, success_count = ?, fail_count = ?, status = ? WHERE id = ?",
                endTime, successCount, failCount, status, id);
    }
}