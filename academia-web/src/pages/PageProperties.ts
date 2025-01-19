import {NavPage} from "../types.ts";

export interface PageProperties {
    onChange: (page: NavPage | null) => void
}