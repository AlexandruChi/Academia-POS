import React, { useEffect, useState } from 'react';
import { ADADEMIA_HOST, ACADEMIA_PATH } from "../config.ts";
import { fetchJsonWithAuth } from '../service.ts';
import { LectureCard, Button, Lecture } from "../cards/LectureCard.tsx";
import {Link, NavPage} from "../types.ts";
import './Page.css';
import {PageProperties} from "./PageProperties.ts";
import LecturePage from "./LecturePage.tsx";

class LectureItem {
    code: string;
    lecture: Lecture;

    constructor(code: string, lecture: Lecture) {
        this.code = code;
        this.lecture = lecture;
    }
}

class LecturesResponse {
    list: LectureItem[];
    _links: Record<string, Link>;

    constructor(list: LectureItem[], _links: Record<string, Link>) {
        this.list = list;
        this._links = _links;
    }
}

class Response {
    lectures: LecturesResponse;

    constructor(lectures: LecturesResponse) {
        this.lectures = lectures;
    }
}

const LecturesList: React.FC<PageProperties> = ({onChange}) => {
    const [lectures, setLectures] = useState<LectureItem[]>([]);
    const [lectureOptions, setLectureOptions] = useState<Button[]>([]);

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

        const selectLecture = async (url: string, method: string, code: string) => {
             const navPage: NavPage = {
                    name: 'Lecture',
                    component: LecturePage as React.FC<PageProperties>,
                    params: {onChange: onChange, code, url, method}
             };

             onChange(navPage);
        }

        const fetchCourses = async () => {
            try {
                const data :Response = await fetchJsonWithAuth(
                    `${ADADEMIA_HOST}${ACADEMIA_PATH}/lectures`, 'GET', undefined, 200
                );

                setLectures(data.lectures.list);

                const buttons: Button[] = []

                if (data.lectures._links["lecture"]) {
                    buttons.push({
                        name: "select", onClick: (code: string) => {
                            const url = data.lectures._links["lecture"].href.replace("{code}", code);
                            selectLecture(url, data.lectures._links["lecture"].type, code).then()
                        }
                    });
                }

                if (data.lectures._links["professor"]) {
                    buttons.push({
                        name: "professor", onClick: (code: string, lecture: Lecture) => {
                            const url = data.lectures._links["professor"].href
                                .replace("{idHolder}", lecture.idHolder);
                            selectLecture(url, data.lectures._links["professor"].type, code).then()
                        }
                    });
                }

                if (data.lectures._links["students"]) {
                    buttons.push({
                        name: "students", onClick: (code: string) => {
                            const url = data.lectures._links["students"].href.replace("{code}", code);
                            selectLecture(url, data.lectures._links["students"].type, code).then()
                        }
                    });
                }

                if (data.lectures._links["delete"]) {
                    buttons.push({
                        name: "delete", onClick: (code: string) => {
                            if (window.confirm(`Delete lecture ${code}?`)) {
                                const url = data.lectures._links["delete"].href.replace("{code}", code);
                                deleteLecture(url, data.lectures._links["delete"].type).then(
                                    () => fetchCourses().then()
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

        fetchCourses().then();
    }, [onChange]);

    return (
        <div className="page">
            <h1>Lectures</h1>
            <ul>
                {lectures && lectures.map((lecture) => (
                    <LectureCard
                        key={lecture.code} code={lecture.code} lecture={lecture.lecture} buttons={lectureOptions}
                    />
                ))}
            </ul>
        </div>
    );
}

export default LecturesList;