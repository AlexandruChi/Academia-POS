from argparse import ArgumentError
from typing import Annotated

from pymongo.errors import DuplicateKeyError

import config
from academia import Academia
from authorization import Authorization, Role
from exceptions import *

from fastapi import FastAPI, HTTPException, status, Header, Body
from fastapi.responses import JSONResponse
import pymongo

client = pymongo.MongoClient(f'mongodb://{config.DATABASE_HOST}')
db = client['discipline']
courses = db['disciplină']

app = FastAPI(root_path='/api/courses')

academia = Academia()

@app.get('/{code}')
async def get_course(code: str, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR, Role.STUDENT])

    try:
        if claims['role'] == Role.STUDENT and not await academia.is_student_enrolled(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    course = courses.find_one({'cod': code})

    if course is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    return JSONResponse({'lecture': course['disciplină']})


@app.post('', status_code=status.HTTP_201_CREATED)
async def add_course_page(code: str | None = None, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR])

    if code is None:
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY)

    try:
        if claims['role'] == Role.PROFESSOR and not await academia.is_professor_holder(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    examination = await academia.get_lecture_examination(code)
    if examination is None:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT)

    try :
        courses.insert_one({
            'cod': code,
            'disciplină': {
                'evaluare': {
                    'finală': {
                        examination: {
                            "pondere": 100,
                            "detalii": {}
                        }
                    },
                    'parcurs': {}
                },
                'curs': {},
                'laborator': {}
            }
        })

        course = courses.find_one({'cod': code})
        return JSONResponse({'lecture': course['disciplină']})
    except DuplicateKeyError:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT)


@app.delete('/{code}', status_code=status.HTTP_204_NO_CONTENT)
async def delete_course_page(code: str, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR])

    try:
        if claims['role'] == Role.PROFESSOR and not await academia.is_professor_holder(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    courses.delete_one({'cod': code})


@app.get('/{code}/{section}')
async def get_section(code: str, section: str, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR, Role.STUDENT])

    try:
        if claims['role'] == Role.STUDENT and not await academia.is_student_enrolled(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    course = courses.find_one({'cod': code})
    if course is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    if section not in course['disciplină']:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    return JSONResponse({section: course['disciplină'][section]})

@app.post('/{code}/evaluare', status_code=status.HTTP_204_NO_CONTENT)
async def add_evaluation(code: str, data: dict, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR, Role.STUDENT])

    try:
        if claims['role'] == Role.STUDENT and not await academia.is_student_enrolled(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    course = courses.find_one({'cod': code})
    if course is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    try:
        percentage = 0
        for x in data:
            percentage += int(data[x]['pondere'])

        if percentage > 100:
            raise HTTPException(status_code=status.HTTP_409_CONFLICT)

        courses.update_one({'cod': code}, {'$set': {f'disciplină.evaluare.parcurs': {}}})

        for x in data:
            if x == 'finală':
                courses.update_one({'cod': code}, {'$set': {
                    f'disciplină.evaluare.finală.{list(course['disciplină']['evaluare']['finală'].keys())[0]}': data[x]
                }})
            else:
                courses.update_one({'cod': code}, {'$set': {f'disciplină.evaluare.parcurs.{x}': data[x]}})

    except ArgumentError:
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY)


@app.get('/{code}/{section}/{content}')
async def get_content(code: str, section: str, content: str, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR, Role.STUDENT])

    try:
        if claims['role'] == Role.STUDENT and not await academia.is_student_enrolled(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    course = courses.find_one({'cod': code})
    if course is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    if section not in course['disciplină']:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    if content not in course['disciplină'][section]:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    return JSONResponse({content: course['disciplină'][section][content]})


@app.put('/{code}/{section}/{content}', status_code=status.HTTP_204_NO_CONTENT)
async def set_content(
        code: str, section: str, content: str, data: str | None = Body(default=None),
        authorization: Annotated[str | None, Header()] = None
):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR])

    try:
        if claims['role'] == Role.PROFESSOR and not await academia.is_professor_holder(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    course = courses.find_one({'cod': code})
    if course is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    if section not in course['disciplină']:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    if section == 'evaluare':
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)

    try:
        if claims['role'] == Role.PROFESSOR and not await academia.is_professor_holder(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    courses.update_one({'cod': code}, {'$set': {f'disciplină.{section}.{content}': data}})


@app.delete('/{code}/{section}/{content}', status_code=status.HTTP_204_NO_CONTENT)
async def delete_content(code: str, section: str, content: str, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR])

    try:
        if claims['role'] == Role.PROFESSOR and not await academia.is_professor_holder(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    course = courses.find_one({'cod': code})
    if course is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    if section not in course['disciplină']:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    if section == 'evaluare':
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)

    try:
        if claims['role'] == Role.PROFESSOR and not await academia.is_professor_holder(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    courses.update_one({'cod': code}, {'$unset': {f'disciplină.{section}.{content}': ""}})


def check_authorization(authorization: Annotated[str | None, Header()], roles: list[Role]) -> dict:
    try:
        claims = Authorization.check_authorization(authorization, roles)
    except Unauthenticated:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED)
    except Unauthorized:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)

    return claims
