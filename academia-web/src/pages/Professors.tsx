import React, { useEffect, useState } from 'react';
import { ADADEMIA_HOST, ACADEMIA_PATH } from "../config.ts";
import { fetchJsonWithAuth } from '../service.ts';
import { ProfessorCard, Button } from "../cards/ProfessorCard.tsx";
import { Link} from "../types.ts";
import './Page.css';

class Professor {
    lastName: string;
    firstName: string;
    email: string;
    teachingDegree: string;
    associationType: string;
    affiliation: string | null;

    constructor(
        lastName: string,
        firstName: string,
        email: string,
        teachingDegree: string,
        associationType: string,
        affiliation: string | null
    ) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.teachingDegree = teachingDegree;
        this.associationType = associationType;
        this.affiliation = affiliation;
    }
}

class ProfessorItem {
    id: number;
    professor: Professor;

    constructor(id: number, professor: Professor) {
        this.id = id;
        this.professor = professor;
    }
}

class ProfessorsResponse {
    list: ProfessorItem[];
    _links: Record<string, Link>;

    constructor(list: ProfessorItem[], _links: Record<string, Link>) {
        this.list = list;
        this._links = _links;
    }
}

class Response {
    professors: ProfessorsResponse;

    constructor(professors: ProfessorsResponse) {
        this.professors = professors;
    }
}

// eslint-disable-next-line
function isResponse(data: any): data is Response {
    return data && data.professors && Array.isArray(data.professors.list);
}

const Professors: React.FC = () => {
    const [response, setResponse] = useState<Response>();
    const [professorOptions, setProfessorOptions] = useState<Button[]>([]);

    const deleteProfessor = async (url: string, method: string) => {
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
        const fetchProfessors = async () => {
            try {
                const data = await fetchJsonWithAuth(
                    `${ADADEMIA_HOST}${ACADEMIA_PATH}/professors`, 'GET', undefined, 200
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

        fetchProfessors().then(r => console.log(r));

        if (response) {
            const buttons: Button[] = []

            if (response.professors._links["delete"]) {
                buttons.push({
                    name: "Delete", onClick: (id: number) => {
                        const url = response.professors._links["delete"].href.replace("{id}", id.toString());
                        deleteProfessor(url, response.professors._links["delete"].type).then()
                    }
                });
            }

            setProfessorOptions(buttons);
        }

    }, [response]);

    return (
        <div className="page">
            <h1>Professors</h1>
            <ul>
                {response && response.professors.list.map((professorItem) => (
                    <ProfessorCard
                        key={professorItem.id} id={professorItem.id} professor={professorItem.professor} buttons={professorOptions}
                    />
                ))}
            </ul>
        </div>
    );
}

export default Professors;