import React from 'react';
import './CardStyle.css';

export interface Button {
    name: string;
    onClick: (id: number) => void;
}

interface StudentCardProperties {
    buttons: Button[];
    id: number;
    student: {
        lastName: string;
        firstName: string;
        email: string;
        studyCycle: string;
        studyYear: string;
        group: string;
    };
}

export const StudentCard: React.FC<StudentCardProperties> = ({ buttons, id, student }) => {
    return (
        <div className="card">
            <div className="card-details">
                <p><strong>Name:</strong> {`${student.firstName} ${student.lastName}`}</p>
                <p><strong>Email:</strong> {student.email}</p>
                <p><strong>Study Cycle:</strong> {student.studyCycle}</p>
                <p><strong>Study Year:</strong> {student.studyYear}</p>
                <p><strong>Group:</strong> {student.group}</p>
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