export interface SnackbarBaseOptions {
    timeout?: number;
    icon?: string;
    closeable?: boolean;
}

export interface SnackbarOptions extends SnackbarBaseOptions {
    text: string;
    color?: string;
}

export type PresetSnackbarOptions = SnackbarBaseOptions;