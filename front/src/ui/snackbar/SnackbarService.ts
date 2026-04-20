import { useSnackbarStore } from '@/ui/snackbar/useSnackbarStore';
import type { SnackbarOptions, PresetSnackbarOptions } from '@/model/Snackbar';
import {mdiAlertCircleOutline, mdiAlertOutline, mdiCheckBold, mdiInformationOutline} from "@mdi/js";

export const useSnackbarService = () => {
    const snackbarStore = useSnackbarStore();

    const show = (options: SnackbarOptions | string) => {
        const defaultOptions = {
            closeable: false,
            timeout: 3000,
        };

        if (typeof options === 'string') {
            snackbarStore.show({ ...defaultOptions, text: options });
        } else {
            snackbarStore.show({ ...defaultOptions, ...options });
        }
    };

    const success = (text: string, options?: PresetSnackbarOptions) => {
        show({
            text,
            color: 'success',
            icon: options?.icon ?? mdiCheckBold,
            timeout: options?.timeout,
            closeable: options?.closeable,
        });
    };

    const error = (text: string, options?: PresetSnackbarOptions) => {
        show({
            text,
            color: 'error',
            icon: options?.icon ?? mdiAlertCircleOutline,
            timeout: options?.timeout,
            closeable: options?.closeable,
        });
    };

    const info = (text: string, options?: PresetSnackbarOptions) => {
        show({
            text,
            color: 'info',
            icon: options?.icon ?? mdiInformationOutline,
            timeout: options?.timeout,
            closeable: options?.closeable,
        });
    };

    const warning = (text: string, options?: PresetSnackbarOptions) => {
        show({
            text,
            color: 'warning',
            icon: options?.icon ?? mdiAlertOutline,
            timeout: options?.timeout,
            closeable: options?.closeable,
        });
    };

    return {
        show,
        success,
        error,
        info,
        warning,
    };
};
