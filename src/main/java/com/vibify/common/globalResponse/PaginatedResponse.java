package com.vibify.common.globalResponse;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {

    private List<T> content;

    private int page;

    private int size;

    private int totalPages;

    private long totalElements;

}
