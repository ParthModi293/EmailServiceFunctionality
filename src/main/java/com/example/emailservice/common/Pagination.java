package com.example.emailservice.common;


import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class Pagination {

    private int totalPage;
    private int totalRecords;
    private int currentPage;
    private int recordsPerPage;

    public Pagination(int totalRecords, int currentPage, int recordsPerPage) {
        this.totalPage = (int) (totalRecords < recordsPerPage ? 1 : Math.ceil((double) totalRecords / (double) recordsPerPage));
        this.totalRecords = totalRecords;
        this.currentPage = currentPage;
        this.recordsPerPage = recordsPerPage;
    }

    public Pagination() {

    }

}
