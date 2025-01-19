import React, {useEffect, useState} from "react";
import {PageProperties} from "./PageProperties.ts";
import {Button, Lecture, LectureCard} from "../cards/LectureCard.tsx";
import './Page.css';
import {Link} from "../types.ts";
import {ADADEMIA_HOST, COURSES_HOST} from "../config.ts";
import {fetchJsonWithAuth} from "../service.ts";

interface LecturePageProperties extends PageProperties {
    code: string;
    url: string;
    method: string;
}

class SelectedLecture implements Lecture {
    examinationType: string;
    idHolder: string;
    lectureCategory: string;
    lectureName: string;
    lectureType: string;
    studyYear: string;
    _links: Record<string, Link>;

    constructor(
        examinationType: string,
        idHolder: string,
        lectureCategory: string,
        lectureName: string,
        lectureType: string,
        studyYear: string,
        _links: Record<string, Link>
    ) {
        this.examinationType = examinationType;
        this.idHolder = idHolder;
        this.lectureCategory = lectureCategory;
        this.lectureName = lectureName;
        this.lectureType = lectureType;
        this.studyYear = studyYear;
        this._links = _links;
    }
}

class CoursesItem {
    file: string;
    type: string;
    size: string;

    constructor(file: string, type: string, size: string) {
        this.file = file;
        this.type = type;
        this.size = size;
    }
}

class CoursesInfo {
    evaluare: Record<string, object>
    curs: Record<string, CoursesItem>;
    laborator: Record<string, CoursesItem>;
    _links: Record<string, Link>;

    constructor(
        evaluare: Record<string, object>,
        curs: Record<string, CoursesItem>,
        laborator: Record<string, CoursesItem>,
        _links: Record<string, Link>
    ) {
        this.evaluare = evaluare;
        this.curs = curs;
        this.laborator = laborator;
        this._links = _links;
    }
}

class Response {
    lecture: SelectedLecture;

    constructor(lecture: SelectedLecture) {
        this.lecture = lecture;
    }
}

class CoursesResponse {
    lecture: CoursesInfo;

    constructor(lecture: CoursesInfo) {
        this.lecture = lecture;
    }
}

const LecturePage: React.FC<LecturePageProperties> = ({onChange, code, url, method}) => {
    const [lecture, setLecture] = useState<Lecture>();
    const [course, setCourse] = useState<CoursesInfo>();
    const [itemDownload, setItemDownload] = useState<Link>();
    const [itemDelete, setItemDelete] = useState<Link>();
    const [lectureOptions, setLectureOptions] = useState<Button[]>([]);
    const [update, setUpdate] = useState(false);

    useEffect(() => {
        const deleteLecture = async (url: string, method: string) => {
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

        const createPage = async (url: string, method: string) => {
            try {
                if (url[0] == '/') {
                    url = ADADEMIA_HOST + url;
                }

                await fetchJsonWithAuth(
                    url, method, undefined, 201
                );

            } catch (error) {
                if (error instanceof Error) {
                    alert(error.message);
                } else {
                    alert('Unknown error');
                }
            }
        }

        const fetchLecturePage = async (url: string, method: string) => {
            try {
                if (url[0] == '/') {
                    url = ADADEMIA_HOST + url;
                }

                const data :CoursesResponse = await fetchJsonWithAuth(
                    url, method, undefined, 200
                );

                setCourse(data.lecture);

                if (data.lecture._links["content"]) {
                    setItemDownload(data.lecture._links["content"]);
                }

                if (data.lecture._links["delete_content"]) {
                    setItemDelete(data.lecture._links["delete_content"]);
                }

            } catch (error) {
                if (error instanceof Error) {
                    alert(error.message);
                } else {
                    alert('Unknown error');
                }
            }
        }

        const fetchLecture = async (url_f: string, method_f: string) => {
            try {
                if (url_f[0] == '/') {
                    url_f = ADADEMIA_HOST + url_f;
                }

                const data :Response = await fetchJsonWithAuth(
                    url_f, method_f, undefined, 200
                );

                setLecture(data.lecture);

                const buttons: Button[] = []

                if (data.lecture._links["create_lecture_page"]) {
                    buttons.push({
                        name: "create page", onClick: () => {
                            const url_f = data.lecture._links["create_lecture_page"].href
                            createPage(url_f, data.lecture._links["create_lecture_page"].type).then(
                                () => fetchLecture(url, method)
                            )
                        }
                    });
                }

                if (data.lecture._links["lecture_page"]) {
                    fetchLecturePage(data.lecture._links["lecture_page"].href, data.lecture._links["lecture_page"].type).then();
                }

                if (data.lecture._links["professor"]) {
                    buttons.push({
                        name: "professor", onClick: () => {
                            //const url_f = data.lecture._links["professor"].href
                            //selectLecture(url_f, data.lectures._links["professor"].type, code).then()
                        }
                    });
                }

                if (data.lecture._links["students"]) {
                    buttons.push({
                        name: "students", onClick: () => {
                            //const url_f = data.lecture._links["students"].href;
                            //selectLecture(url_f, data.lecture._links["students"].type, code).then()
                        }
                    });
                }

                if (data.lecture._links["delete"]) {
                    buttons.push({
                        name: "delete", onClick: (code: string) => {
                            if (window.confirm(`Delete lecture ${code}?`)) {
                                const url_f = data.lecture._links["delete"].href;
                                deleteLecture(url_f, data.lecture._links["delete"].type).then(
                                    () => onChange(null)
                                )
                            }
                        }
                    });
                }

                setLectureOptions(buttons);

            } catch (error) {
                if (error instanceof Error) {
                    alert(error.message);
                } else {
                    alert('Unknown error');
                }
            }
        };

        fetchLecture(url, method).then();
    }, [update, code, method, onChange, url]);

    const handleDownload = async (file: string, name: string, type: string) => {
        if (itemDownload === undefined) {
            alert('Download link not found');
            return;
        }

        const token = localStorage.getItem('jwtToken');
        if (!token) {
            alert('No token found');
            return;
        }

        const downloadUrl = COURSES_HOST + itemDownload.href?.replace('{section}', type).replace('{content}', name);
        if (!downloadUrl) {
            alert('Download URL not found');
            return;
        }

        try {
            const response = await fetch('http://' + downloadUrl, {
                method: itemDownload.type,
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                // noinspection ExceptionCaughtLocallyJS
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = file;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        } catch (error) {
            if (error instanceof Error) {
                alert(error.message);
            } else {
                alert(error);
            }
        }
    };

    const handleDelete = async (name: string, type: string) => {
        if (itemDelete === undefined) {
            alert('Delete link not found');
            return
        }

        try {
            let url_f = itemDelete.href;

            if (url_f[0] == '/') {
                url_f = COURSES_HOST + url_f;
            }

            url_f = url_f.replace('{section}', type).replace('{content}', name);

            await fetchJsonWithAuth(
                url_f, itemDelete.type, undefined, 204
            );

            setUpdate(!update);

        } catch (error) {
            if (error instanceof Error) {
                alert(error.message);
            } else {
                alert('Unknown error');
            }
        }
    };

    return (
        <div className="page">
            <h1>Lecture</h1>
            {lecture && <LectureCard key={code} code={code} lecture={lecture} buttons={lectureOptions} />}
            <h2>Examinare</h2>
            {course && (
                <>
                    <h2>Curs</h2>
                    <ul>
                        {Object.entries(course.curs).map(([name, item], index) => (
                            <li key={index}>
                                {item.file} - {item.type} - {item.size}
                                <button onClick={() => handleDownload(item.file, name, "curs")}>Download</button>
                                <button onClick={() => handleDelete(name, "curs")}>Delete</button>
                            </li>
                        ))}
                    </ul>
                    <h2>Laborator</h2>
                    <ul>
                        {Object.entries(course.laborator).map(([name, item], index) => (
                            <li key={index}>
                                {item.file} - {item.type} - {item.size}
                                <button onClick={() => handleDownload(item.file, name, "laborator")}>Download</button>
                                <button onClick={() => handleDelete(name, "laborator")}>Delete</button>
                            </li>
                        ))}
                    </ul>
                </>
            )}
        </div>
    );
}

export default LecturePage;