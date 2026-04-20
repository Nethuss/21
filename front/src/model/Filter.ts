export type SortDirection = "ASC" | "DESC" | null;

export interface SortFilter {
  property: string;
  direction: SortDirection;
}

export interface SortBy {
  key: string;
  order: SortDirection;
}

export type Header = {
  title: string;
  key: string;
  columnName: string;
  sortable: boolean;
};
