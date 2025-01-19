import React, {useEffect, useState} from 'react';
import Login from './Login';
import TopBar from "./TopBar.tsx";
import Dashboard from "./pages/Dashboard.tsx";
import LecturesList from "./pages/LecturesList.tsx";
import ProfessorsList from "./pages/ProfessorsList.tsx";
import StudentsList from "./pages/StudentsList.tsx";
import {NavPage} from "./types.ts";

const App: React.FC = () => {
    const componentsArray: NavPage[] = [
        { name: 'Dashboard', component: Dashboard, params: {} },
        { name: 'Lectures', component: LecturesList, params: {} },
        { name: 'Professors', component: ProfessorsList, params: {} },
        { name: 'Students', component: StudentsList, params: {} }
    ];

    const [connected, setConnected] = useState(false);
    const [currentPage, setCurrentPage] = useState<NavPage>(componentsArray[0]);

    const handleConnect = () => {
        setConnected(true);
    }

    const handleDisconnect = () => {
        setConnected(false);
    }

    const handlePageChange = (page: NavPage | null) => {
        if (page == null) {
            setCurrentPage(componentsArray[0]);
        } else {
            setCurrentPage(page);
        }
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
            {connected && currentPage && <currentPage.component onChange={handlePageChange} {...currentPage.params} />}
            {!connected && <Login onLogin={handleConnect} />}
        </div>
    );
};

export default App;