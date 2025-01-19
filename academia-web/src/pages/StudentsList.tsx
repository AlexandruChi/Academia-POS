import React, { useEffect, useState } from 'react';
import { ADADEMIA_HOST, ACADEMIA_PATH } from "../config.ts";
import { fetchJsonWithAuth } from '../service.ts';
import { StudentCard, Button, Student } from "../cards/StudentCard.tsx";
import { Link } from "../types.ts";
import './Page.css';

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

const StudentsList: React.FC = () => {
    const [students, setStudents] = useState<StudentItem[]>([]);
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

    const selectStudent = async (url: string, method: string) => {
        try {
            if (url[0] == '/') {
                url = ADADEMIA_HOST + url;
            }

            await fetchJsonWithAuth(
                url, method, undefined, 200
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

                setStudents(data.students.list);

                const buttons: Button[] = []

                if (data.students._links["student"]) {
                    buttons.push({
                        name: "select", onClick: (id: number) => {
                            const url = data.students._links["student"].href.replace("{id}", id.toString());
                            selectStudent(url, data.students._links["student"].type).then()
                        }
                    });
                }

                if (data.students._links["lectures"]) {
                    buttons.push({
                        name: "lectures", onClick: (id: number) => {
                            const url = data.students._links["lectures"].href.replace("{id}", id.toString());
                            selectStudent(url, data.students._links["lectures"].type).then()
                        }
                    });
                }

                if (data.students._links["delete"]) {
                    buttons.push({
                        name: "delete", onClick: (id: number) => {
                            const url = data.students._links["delete"].href.replace("{id}", id.toString());
                            deleteStudent(url, data.students._links["delete"].type).then()
                        }
                    });
                }

                setStudentOptions(buttons);

            } catch (error) {
                if (error instanceof Error) {
                    alert(error.message);
                } else {
                    alert('Unknown error');
                }
            }
        };

        fetchStudents().then(r => console.log(r));
    }, []);

    return (
        <div className="page">
            <h1>Students</h1>
            <ul>
                {students && students.map((studentItem) => (
                    <StudentCard
                        key={studentItem.id} id={studentItem.id} student={studentItem.student} buttons={studentOptions}
                    />
                ))}
            </ul>
        </div>
    );
}

export default StudentsList;