import React from 'react';
import './CardStyle.css';

export interface Button {
    name: string;
    onClick: (code: string) => void;
}

interface LectureCardProperties {
    buttons: Button[];
    code: string;
    lecture: {
        lectureName: string;
        studyYear: string;
        lectureType: string;
        lectureCategory: string;
        examinationType: string;
    };
}

export const LectureCard: React.FC<LectureCardProperties> = ({ buttons, code, lecture }) => {
    return (
        <div className="card">
            <div className="card-details">
                <p><strong>Study Year:</strong> {lecture.studyYear}</p>
                <p><strong>Type:</strong> {lecture.lectureType}</p>
                <p><strong>Category:</strong> {lecture.lectureCategory}</p>
                <p><strong>Examination:</strong> {lecture.examinationType}</p>
            </div>
            <h2 className="card-title">{`${lecture.lectureName} (${code})`}</h2>
            <div className="card-buttons">
                {buttons.map((button, index) => (
                    <button key={index} onClick={() => button.onClick(code)}>
                        {button.name}
                    </button>
                ))}
            </div>
        </div>
    );
}