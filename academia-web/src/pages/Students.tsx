import React, { useEffect, useState } from 'react';
import { ADADEMIA_HOST, ACADEMIA_PATH } from "../config.ts";
import { fetchJsonWithAuth } from '../service.ts';
import { StudentCard, Button } from "../cards/StudentCard.tsx";
import { Link } from "../types.ts";
import './Page.css';

class Student {
    lastName: string;
    firstName: string;
    email: string;
    studyCycle: string;
    studyYear: string;
    group: string;

    constructor(
        lastName: string,
        firstName: string,
        email: string,
        studyCycle: string,
        studyYear: string,
        group: string
    ) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.studyCycle = studyCycle;
        this.studyYear = studyYear;
        this.group = group;
    }
}

class StudentItem {
    id: number;
    student: Student;

    constructor(id: number, student: Student) {
        this.id = id;
        this.student = student;
    }
}

class StudentsResponse {
    list: StudentItem[];
    _links: Record<string, Link>;

    constructor(list: StudentItem[], _links: Record<string, Link>) {
        this.list = list;
        this._links = _links;
    }
}

class Response {
    students: StudentsResponse;

    constructor(students: StudentsResponse) {
        this.students = students;
    }
}

// eslint-disable-next-line
function isResponse(data: any): data is Response {
    return data && data.students && Array.isArray(data.students.list);
}

const Students: React.FC = () => {
    const [response, setResponse] = useState<Response>();
    const [studentOptions, setStudentOptions] = useState<Button[]>([]);

    const deleteStudent = async (url: string, method: string) => {
        try {
            if (url[0] == '/') {
                url = ADADEMIA_HOST + url;
            }

            await fetchJsonWithAuth(
                url, method, undefined, 204
            );

        } catch (error) {
            if (error instanceof Error) {
                alert(error.message);
            } else {
                alert('Unknown error');
            }
        }
    }

    useEffect(() => {
        const fetchStudents = async () => {
            try {
                const data = await fetchJsonWithAuth(
                    `${ADADEMIA_HOST}${ACADEMIA_PATH}/students`, 'GET', undefined, 200
                );
                if (!isResponse(data)) {
                    // noinspection ExceptionCaughtLocallyJS
                    throw new Error('Service exception');
                }
                setResponse(data);
            } catch (error) {
                if (error instanceof Error) {
                    alert(error.message);
                } else {
                    alert('Unknown error');
                }
            }
        };

        fetchStudents().then(r => console.log(r));

        if (response) {
            const buttons: Button[] = []

            if (response.students._links["delete"]) {
                buttons.push({
                    name: "Delete", onClick: (id: number) => {
                        const url = response.students._links["delete"].href.replace("{id}", id.toString());
                        deleteStudent(url, response.students._links["delete"].type).then()
                    }
                });
            }

            setStudentOptions(buttons);
        }

    }, [response]);

    return (
        <div className="page">
            <h1>Students</h1>
            <ul>
                {response && response.students.list.map((studentItem) => (
                    <StudentCard
                        key={studentItem.id} id={studentItem.id} student={studentItem.student} buttons={studentOptions}
                    />
                ))}
            </ul>
        </div>
    );
}

export default Students;