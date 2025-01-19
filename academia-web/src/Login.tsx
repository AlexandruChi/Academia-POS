import React, { useState } from 'react';
import {authenticate, getClaims} from './authorization';
import './Login.css';

interface LoginProperties {
    onLogin: () => void;
}

const Login: React.FC<LoginProperties> = ({onLogin}) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errorStr, setErrorStr] = useState('');


    // TODO: Move logic to App.tsx
    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        try {
            const response = await authenticate(username, password);
            if (response === '') {
                // noinspection ExceptionCaughtLocallyJS
                throw new Error('wrong email or password');
            }

            const claims = await getClaims(response);
            if (claims === null) {
                // noinspection ExceptionCaughtLocallyJS
                throw new Error('invalid response');
            }

            localStorage.setItem('jwtToken', response);
            localStorage.setItem('email', claims.email);
            localStorage.setItem('role', claims.role);

            setErrorStr('');
            onLogin();
        } catch (error) {
            if (error instanceof Error) {
                setErrorStr(error.message);
            } else {
                setErrorStr('unknown error');
            }
        }
    };

    return (
        <div className="centered">
            <form onSubmit={handleSubmit}>
                <h1>Login</h1>
                    <input type="email" value={username} placeholder="username" onChange={
                        (e) => setUsername(e.target.value)
                    }/>
                    <input type="password" value={password} placeholder="password" onChange={
                        (e) => setPassword(e.target.value)
                    }/>
                <button type="submit">Login</button>
                {errorStr && <p style={{ color: 'red' }}>{errorStr}</p>}
            </form>
        </div>
    );
};

export default Login;