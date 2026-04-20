export interface EnumGroupDTO {
    groupName: string;
    groupNameTranslated: string;
    enums: EnumDTO[];
}

export interface EnumDTO {
    enumName: string;
    enumNameTranslated: string;
    description?: string;
    values: EnumValueDTO[];
}

export interface EnumValueDTO {
    valueName: string;
    valueNameTranslated: string;
    label: string;
}
