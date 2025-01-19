import React from 'react';
import './CardStyle.css';

export interface Button {
    name: string;
    onClick: (id: number) => void;
}

export interface Professor {
    lastName: string;
    firstName: string;
    email: string;
    teachingDegree: string;
    associationType: string;
    affiliation: string | null;
}

interface ProfessorCardProperties {
    buttons: Button[];
    id: number;
    professor: Professor;
}

export const ProfessorCard: React.FC<ProfessorCardProperties> = ({ buttons, id, professor }) => {
    return (
        <div className="card">
            <div className="card-details">
                <p>
                    <strong>Name:</strong> {`${professor.teachingDegree} ${professor.firstName} ${professor.lastName}`}
                </p>
                <p><strong>Email:</strong> {professor.email}</p>
                <p><strong>Association</strong> {professor.associationType}</p>
                {professor.affiliation && (
                    <p><strong>Affiliation:</strong> {professor.affiliation}</p>
                )}
            </div>
            <div className="card-buttons">
                {buttons.map((button, index) => (
                    <button key={index} onClick={() => button.onClick(id)}>
                        {button.name}
                    </button>
                ))}
            </div>
        </div>
    );
}