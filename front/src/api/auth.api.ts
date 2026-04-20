import api from "@/api/axios";
import type { LoginDTO } from "@/model/LoginDTO.ts";
import {AxiosResponse} from "axios";

const defaultUrl = "/auth";

export const signIn = async (
    usernameFromForm: string,
    passwordFromForm: string
): Promise<AxiosResponse<LoginDTO>> => {
    return api.post<LoginDTO>(`${defaultUrl}/signin`, {
        username: usernameFromForm,
        password: passwordFromForm,
    });
};

export const updateAccessToken = async (): Promise<AxiosResponse<void>> => {
    return api.post(`${defaultUrl}/refreshtoken`);
};

export const verifyToken = async (): Promise<boolean> => {
    try {
        const response = await api.get(`${defaultUrl}/verify-access-token`);
        return response.status === 200;
    } catch (error) {
        console.error(error)
        return false;
    }
};

export const getAuthedUserData = async () => {
    return await api.get('/user')
}

export const signOut = async () => {
    return await api.post(`${defaultUrl}/signout`);
}
