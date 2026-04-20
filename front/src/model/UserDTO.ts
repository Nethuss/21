import {Role} from "@/enum/UserRole";
import {LocationDTO} from "@/model/LocationDTO";

export interface UserDTO {
    id?: number,
    username: string,
    firstname: string,
    middlename: string,
    lastname: string,
    roles: Role[],
    locationIds: number[],
    locations: LocationDTO[],
    password: string,
    isActive: boolean
}
