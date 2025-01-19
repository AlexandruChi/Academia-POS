import { GrpcWebFetchTransport } from "@protobuf-ts/grpcweb-transport";
import { IDMClient } from "./IDM.client";
import { login, register } from "./IDM";
import { Empty } from "./google/protobuf/empty";
import { jwtDecode } from "jwt-decode";

const transport = new GrpcWebFetchTransport({
    baseUrl: "http://localhost:8050",
});

const client = new IDMClient(transport);

export async function authenticate(username: string, password: string) {
    const request: login = { username, password };
    const response = await client.authenticate(request);
    return response.response.token;
}

export async function registerUser(username: string, password: string, role: string, token: string) {
    const request: register = { username, password, role };
    const headers = { Authorization: `Bearer ${token}` };
    await client.register(request, {meta: headers});
}

export async function deauthenticate(token: string) {
    const headers = { Authorization: `Bearer ${token}` };
    await client.deauthenticate(Empty.create(), {meta: headers});
}

export async function validate(token: string) {
    const headers = { Authorization: `Bearer ${token}` };
    await client.validate(Empty.create(), {meta: headers});
}

export async function getClaims(token: string) {
    try {
        await validate(token);
        const decoded = jwtDecode(token) as { [key: string]: string };
        const email = decoded.sub;
        const role = decoded.role;
        return { email, role };
    } catch {
        return null;
    }
}