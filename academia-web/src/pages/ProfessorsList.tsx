import React, { useEffect, useState } from 'react';
import { ADADEMIA_HOST, ACADEMIA_PATH } from "../config.ts";
import { fetchJsonWithAuth } from '../service.ts';
import { ProfessorCard, Button, Professor } from "../cards/ProfessorCard.tsx";
import { Link} from "../types.ts";
import './Page.css';

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

const ProfessorsList: React.FC = () => {
    const [professors, setProfessors] = useState<ProfessorItem[]>([]);
    const [professorOptions, setProfessorOptions] = useState<Button[]>([]);
    const [update, setUpdate] = useState(false);

    useEffect(() => {
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

        const selectProfessor = async (url: string, method: string) => {
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

        const fetchProfessors = async () => {
            try {
                const data = await fetchJsonWithAuth(
                    `${ADADEMIA_HOST}${ACADEMIA_PATH}/professors`, 'GET', undefined, 200
                );
                if (!isResponse(data)) {
                    // noinspection ExceptionCaughtLocallyJS
                    throw new Error('Service exception');
                }

                setProfessors(data.professors.list);

                const buttons: Button[] = []

                if (data.professors._links["professor"]) {
                    buttons.push({
                        name: "select", onClick: (id: number) => {
                            const url = data.professors._links["professor"].href.replace("{id}", id.toString());
                            selectProfessor(url, data.professors._links["professor"].type).then()
                        }
                    });
                }

                if (data.professors._links["lectures"]) {
                    buttons.push({
                        name: "lectures", onClick: (id: number) => {
                            const url = data.professors._links["lectures"].href.replace("{id}", id.toString());
                            selectProfessor(url, data.professors._links["lectures"].type).then()
                        }
                    });
                }

                if (data.professors._links["delete"]) {
                    buttons.push({
                        name: "delete", onClick: (id: number) => {
                            const url = data.professors._links["delete"].href.replace("{id}", id.toString());
                            deleteProfessor(url, data.professors._links["delete"].type).then(
                                () => setUpdate(!update)
                            )
                        }
                    });
                }

                setProfessorOptions(buttons);

            } catch (error) {
                if (error instanceof Error) {
                    alert(error.message);
                } else {
                    alert('Unknown error');
                }
            }
        };

        fetchProfessors().then();
    }, [update]);

    return (
        <div className="page">
            <h1>Professors</h1>
            <ul>
                {professors && professors.map((professorItem) => (
                    <ProfessorCard
                        key={professorItem.id} id={professorItem.id} professor={professorItem.professor} buttons={professorOptions}
                    />
                ))}
            </ul>
        </div>
    );
}

export default ProfessorsList;