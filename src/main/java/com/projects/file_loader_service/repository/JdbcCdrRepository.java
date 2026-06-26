package com.projects.file_loader_service.repository;

import com.projects.file_loader_service.model.CallDetailRecord;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCdrRepository implements CdrRepository {

    private static final String INSERT_SQL = """
             INSERT INTO call_detail_records (
                record_date, l_spc, l_ssn, l_ri, l_gt_i, l_gt_digits,
                r_spc, r_ssn, r_ri, r_gt_i, r_gt_digits,
                service_code, or_nature, or_plan, or_digits,
                de_nature, de_plan, de_digits,
                isdn_nature, isdn_plan, msisdn,
                vlr_nature, vlr_plan, vlr_digits, imsi,
                status, type, tstamp,
                local_dialog_id, remote_dialog_id, dialog_duration,
                ussd_string, id
            ) VALUES (
                ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?
            )
            """;

    private final JdbcTemplate jdbc;

    public JdbcCdrRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int insert(CallDetailRecord r) {
        try {
            return jdbc.update(INSERT_SQL,
                    r.recordDate(), r.lSpc(), r.lSsn(), r.lRi(), r.lGtI(), r.lGtDigits(),
                    r.rSpc(), r.rSsn(), r.rRi(), r.rGtI(), r.rGtDigits(),
                    r.serviceCode(), r.orNature(), r.orPlan(), r.orDigits(),
                    r.deNature(), r.dePlan(), r.deDigits(),
                    r.isdnNature(), r.isdnPlan(), r.msisdn(),
                    r.vlrNature(), r.vlrPlan(), r.vlrDigits(), r.imsi(),
                    r.status(), r.type(), r.tstamp(),
                    r.localDialogId(), r.remoteDialogId(), r.dialogDuration(),
                    r.ussdString(), r.id());
        } catch (DuplicateKeyException e) {
            return 0;
        }
    }
}