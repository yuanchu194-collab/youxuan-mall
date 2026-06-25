package com.youxuan.common.result;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装，后续商品、订单、优惠券等分页接口统一复用。
 *
 * @param <T> 分页记录类型
 */
public class PageResult<T> {

    private Long total;
    private Long pages;
    private Long pageNum;
    private Long pageSize;
    private List<T> records;

    public PageResult() {
    }

    private PageResult(Long total, Long pages, Long pageNum, Long pageSize, List<T> records) {
        this.total = total;
        this.pages = pages;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
    }

    /**
     * 创建分页结果，并对空列表做兜底处理。
     */
    public static <T> PageResult<T> of(Long total, Long pageNum, Long pageSize, List<T> records) {
        long safeTotal = total == null ? 0L : total;
        long safePageNum = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 10L : pageSize;
        long pages = safeTotal == 0 ? 0L : (safeTotal + safePageSize - 1) / safePageSize;
        List<T> safeRecords = records == null ? Collections.emptyList() : records;
        return new PageResult<>(safeTotal, pages, safePageNum, safePageSize, safeRecords);
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPages() {
        return pages;
    }

    public void setPages(Long pages) {
        this.pages = pages;
    }

    public Long getPageNum() {
        return pageNum;
    }

    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
