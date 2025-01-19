export const fetchJsonWithAuth = async (
    url: string, method: string, body?: string, expectedStatus?: number
) => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        throw new Error('No token found');
    }

    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: body ? JSON.stringify(body) : undefined
        });

        if (response.status != expectedStatus) {
            // noinspection ExceptionCaughtLocallyJS
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        if (response.status === 204 || response.status === 205) {
            return;
        }

        return await response.json();
    } catch (error) {
        if (error instanceof Error) {
            throw new Error(error.message);
        } else {
            throw new Error('Unknown error');
        }
    }
};