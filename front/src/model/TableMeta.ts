export interface TableMeta {
    tableName: string;
    columns: ColumnMeta[];
}

export interface ColumnMeta {
    aliasName: string;
    columnName: string;
    sortable: boolean;
}