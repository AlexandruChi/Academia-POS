import React from "react";

export interface NavPage {
    name: string;
    component: React.FC;
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