import React from "react";
import {PageProperties} from "./pages/PageProperties.ts";

export interface NavPage {
    name: string;
    component: React.FC<PageProperties>;
    params: object;
}

export class Link {
    href: string;
    type: string
    query: string;

    constructor(href: string, type: string, query: string) {
        this.href = href;
        this.type = type;
        this.query = query;
    }
}