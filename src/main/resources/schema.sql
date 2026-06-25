-- USSD Call Detail Records, one row per event line in a CDR file.
CREATE TABLE IF NOT EXISTS call_detail_records (
    record_date       TIMESTAMP        NOT NULL,
    l_spc             INT,
    l_ssn             INT,
    l_ri              INT,
    l_gt_i            INT,
    l_gt_digits       VARCHAR(18),
    r_spc             INT,
    r_ssn             INT,
    r_ri              INT,
    r_gt_i            INT,
    r_gt_digits       VARCHAR(18),
    service_code      VARCHAR(50),
    or_nature         INT,
    or_plan           INT,
    or_digits         VARCHAR(18),
    de_nature         INT,
    de_plan           INT,
    de_digits         VARCHAR(18),
    isdn_nature       INT,
    isdn_plan         INT,
    msisdn            VARCHAR(18),
    vlr_nature        INT,
    vlr_plan          INT,
    vlr_digits        VARCHAR(18),
    imsi              VARCHAR(100),
    status            VARCHAR(30)      NOT NULL,
    type              VARCHAR(30)      NOT NULL,
    tstamp            TIMESTAMP        NOT NULL,
    local_dialog_id   BIGINT,
    remote_dialog_id  BIGINT,
    dialog_duration   BIGINT,
    ussd_string       VARCHAR(255),
    id                VARCHAR(150)     NOT NULL,
    CONSTRAINT pk_cdr PRIMARY KEY (id)
);

-- Control table: one row per processed file.
CREATE TABLE IF NOT EXISTS cdr_logs (
    id              BIGSERIAL        PRIMARY KEY,
    file_name       VARCHAR(255)     NOT NULL UNIQUE,
    start_time      TIMESTAMP        NOT NULL,
    end_time        TIMESTAMP,
    success_count   INT              NOT NULL DEFAULT 0,
    fail_count      INT              NOT NULL DEFAULT 0,
    status          VARCHAR(20)      NOT NULL DEFAULT 'IN_PROGRESS'
);