package com.projects.file_loader_service.model;


import java.sql.Timestamp;

public class CrdLog {

    private Long id;
    private String fileName;
    private Timestamp startTime;
    private Timestamp endTime;
    private int successCount;
    private int failCount;
    private String status;

    public CrdLog(String fileName, Timestamp startTime){
        this.fileName = fileName;
        this.startTime = startTime;
        this.status = "IN_PROGRESS";
    }

    public CrdLog(){};

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public String getFileName() {
        return fileName;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public String getStatus() {
        return status;
    }
}
