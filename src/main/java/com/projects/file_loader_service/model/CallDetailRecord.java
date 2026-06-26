package com.projects.file_loader_service.model;

import java.sql.Timestamp;

/**
 * One USSD event row, mapped to the {@code call_detail_records} table.
*/
public record CallDetailRecord(
        Timestamp recordDate,
        Integer lSpc,
        Integer lSsn,
        Integer lRi,
        Integer lGtI,
        String lGtDigits,
        Integer rSpc,
        Integer rSsn,
        Integer rRi,
        Integer rGtI,
        String rGtDigits,
        String serviceCode,
        Integer orNature,
        Integer orPlan,
        String orDigits,
        Integer deNature,
        Integer dePlan,
        String deDigits,
        Integer isdnNature,
        Integer isdnPlan,
        String msisdn,
        Integer vlrNature,
        Integer vlrPlan,
        String vlrDigits,
        String imsi,
        String status,
        String type,
        Timestamp tstamp,
        Long localDialogId,
        Long remoteDialogId,
        Long dialogDuration,
        String ussdString,
        String id
) {}
