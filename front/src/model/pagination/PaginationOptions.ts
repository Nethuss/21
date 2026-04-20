export interface PaginationOptions {
  page: number;
  size: number;
  search?: string;
  sortBy?: SortOption[];
}

export interface SortOption {
  key: string;
  order: "asc" | "desc";
}
