
export interface PaginationPage<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}
