import React, { useEffect, useState } from 'react';
import { ADADEMIA_HOST, ACADEMIA_PATH } from "../config.ts";
import { fetchJsonWithAuth } from '../service.ts';
import { LectureCard, Button } from "../cards/LectureCard.tsx";
import { Link } from "../types.ts";
import './Page.css';

class Lecture {
    idHolder: string;
    lectureName: string;
    studyYear: string;
    lectureType: string;
    lectureCategory: string;
    examinationType: string;

    constructor(
        idHolder: string,
        lectureName: string,
        studyYear: string,
        lectureType: string,
        lectureCategory: string,
        examinationType: string
    ) {
        this.idHolder = idHolder;
        this.lectureName = lectureName;
        this.studyYear = studyYear;
        this.lectureType = lectureType;
        this.lectureCategory = lectureCategory;
        this.examinationType = examinationType;
    }
}

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

// eslint-disable-next-line
function isResponse(data: any): data is Response {
    return data && data.lectures && Array.isArray(data.lectures.list);
}

const Lectures: React.FC = () => {
    const [response, setResponse] = useState<Response>();
    const [lectureOptions, setLectureOptions] = useState<Button[]>([]);

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

    useEffect(() => {
        const fetchCourses = async () => {
            try {
                const data = await fetchJsonWithAuth(
                    `${ADADEMIA_HOST}${ACADEMIA_PATH}/lectures`, 'GET', undefined, 200
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

        fetchCourses().then(r => console.log(r));

        if (response) {
            const buttons: Button[] = []

            if (response.lectures._links["delete"]) {
                buttons.push({
                    name: "Delete", onClick: (code: string) => {
                        const url = response.lectures._links["delete"].href.replace("{code}", code);
                        deleteLecture(url, response.lectures._links["delete"].type).then()
                    }
                });
            }

            setLectureOptions(buttons);
        }

    }, [response]);

    return (
        <div className="page">
            <h1>Lectures</h1>
            <ul>
                {response && response.lectures.list.map((course) => (
                    <LectureCard
                        key={course.code} code={course.code} lecture={course.lecture} buttons={lectureOptions}
                    />
                ))}
            </ul>
        </div>
    );
}

export default Lectures;