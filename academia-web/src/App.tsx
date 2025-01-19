import React, {useEffect, useState} from 'react';
import Login from './Login';
import TopBar from "./TopBar.tsx";
import Dashboard from "./pages/Dashboard.tsx";
import Lectures from "./pages/Lectures.tsx";
import Professors from "./pages/Professors.tsx";
import Students from "./pages/Students.tsx";
import {NavPage} from "./types.ts";

const App: React.FC = () => {
    const componentsArray: NavPage[] = [
        { name: 'Dashboard', component: Dashboard },
        { name: 'Lectures', component: Lectures },
        { name: 'Professors', component: Professors },
        { name: 'Students', component: Students }
    ];

    const [connected, setConnected] = useState(false);
    const [currentPage, setCurrentPage] = useState<NavPage>(componentsArray[0]);

    const handleConnect = () => {
        setConnected(true);
    }

    const handleDisconnect = () => {
        setConnected(false);
    }

    const handlePageChange = (page: NavPage) => {
        setCurrentPage(page);
    }

    useEffect(() => {
        const token = localStorage.getItem('jwtToken');
        if (token !== null) {
            setConnected(true);
        }
    }, []);

    return (
        <div>
            {connected && <TopBar onLogOut={handleDisconnect} onChange={handlePageChange} pages={componentsArray} />}
            {connected && currentPage && <currentPage.component />}
            {!connected && <Login onLogin={handleConnect} />}
        </div>
    );
};

export default App;