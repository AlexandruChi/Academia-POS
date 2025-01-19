import React from 'react';
import {deauthenticate} from './authorization';
import {NavPage} from "./types.ts";
import './TopBar.css';

interface TopBarProperties {
    onLogOut: () => void,
    onChange: (page: NavPage) => void,
    pages: NavPage[]
}

const TopBar: React.FC<TopBarProperties> = ({onLogOut, onChange, pages}) => {
    const handleLogOut = async () => {
        try {
            const token = localStorage.getItem('jwtToken');
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('email');
            localStorage.removeItem('role');
            onLogOut();

            if (token !== null) {
                await deauthenticate(token);
            }

        } catch (error) {
            if (error instanceof Error) {
                alert(error.message);
            } else {
                alert('unknown error');
            }
        }
    }

    return (
        <div className="top-bar">
            <nav>
                {pages.map((page) => (
                    <a onClick={() => onChange(page)} key={page.name}>{page.name}</a>
                ))}
            </nav>
            <div>
                <span>{localStorage.getItem('email')}</span>
                <button onClick={handleLogOut}>Log out</button>
            </div>
        </div>
    )
}

export default TopBar;