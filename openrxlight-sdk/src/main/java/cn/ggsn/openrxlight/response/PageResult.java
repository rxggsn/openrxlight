package cn.ggsn.openrxlight.response;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
public class PageResult<T> {
    public static final int DEFAULT_PAGE_SIZE = 10;
    private Integer pageNo;
    private Integer pageSize;
    private Long totalCount;
    private Long totalPage;
    private List<T> results;
    private Boolean isLastPage;
    private Object updateParams;

    /**
     * 初始化
     *
     * @param pageNo
     * @param pageSize
     * @param totalCount
     * @param result
     */
    public PageResult(int pageNo, int pageSize, long totalCount, List<T> result) {
        this(pageNo, pageSize, totalCount);
        this.results = result;
    }

    /**
     * 初始化
     *
     * @param pageNo
     * @param pageSize
     * @param totalCount
     */
    public PageResult(int pageNo, int pageSize, long totalCount) {
        this(pageNo, pageSize);
        this.totalCount = totalCount;
        this.totalPage = pageSize == 0 ? 0
                : (totalCount % pageSize == 0 ? (totalCount / pageSize) : (totalCount / pageSize + 1));
        this.results = new ArrayList<>();
        this.isLastPage = this.pageNo >= this.totalPage;
    }

    /**
     * 初始化
     *
     * @param pageNo
     * @param pageSize
     */
    public PageResult(int pageNo, int pageSize) {
        this.pageNo = Math.max(pageNo, 0);
        this.pageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        this.totalCount = 0L;
        this.totalPage = 0L;
        this.results = new ArrayList<>();
    }

    public PageResult(List<T> result, Boolean isLastPage, Object updateParams) {
        this.results = result;
        this.isLastPage = isLastPage;
        this.updateParams = updateParams;
    }

    public PageResult(List<T> result) {
        this.results = result;
    }

    public void computeTotalPage() {
        this.totalPage = this.pageSize == 0 ? 0
                : (this.totalCount % pageSize == 0 ? (this.totalCount / pageSize) : (this.totalCount / pageSize + 1));
    }

    public void computePage(long totalCount, List<T> result) {
        this.totalCount = totalCount;
        this.totalPage = pageSize == 0 ? 0 : (this.totalCount / pageSize + (this.totalCount % pageSize == 0 ? 0 : 1));
        this.isLastPage = this.pageNo >= this.totalPage;
        this.results = result;
    }
}
