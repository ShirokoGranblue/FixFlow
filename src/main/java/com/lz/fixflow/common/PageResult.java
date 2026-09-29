package com.lz.fixflow.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;

    private long total;

    private long current;

    private long pageSize;

    private long pages;
}
