import { defineStore } from 'pinia';
import { ref } from 'vue';
import {SnackbarOptions} from "@/model/Snackbar";

export const useSnackbarStore = defineStore('snackbar', () => {
    const messages = ref<SnackbarOptions[]>([]);
    const currentMessage = ref<SnackbarOptions | null>(null);
    const isActive = ref(false);

    function show(message: SnackbarOptions) {
        messages.value.push(message);
        if (!isActive.value) {
            processNextMessage();
        }
    }

    function processNextMessage() {
        if (messages.value.length === 0) {
            isActive.value = false;
            return;
        }

        currentMessage.value = messages.value.shift() || null;
        isActive.value = true;
    }

    function close() {
        isActive.value = false;
        setTimeout(processNextMessage, 300); // небольшая задержка для анимации закрытия
    }

    return {
        messages,
        currentMessage,
        isActive,
        show,
        close,
    };
});