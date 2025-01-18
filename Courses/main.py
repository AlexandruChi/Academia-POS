from argparse import ArgumentError
from typing import Annotated

from pymongo.errors import DuplicateKeyError

import os
import signal

import config
from academia import Academia
from authorization import Authorization, Role
from exceptions import *

from fastapi import FastAPI, HTTPException, status, Header, Body, Request
from fastapi.responses import JSONResponse, Response
import pymongo

while True:
    try :
        client = pymongo.MongoClient(f'mongodb://{config.DATABASE_HOST}')
        db = client['discipline']
        courses = db['disciplină']
        courses.create_index([('cod', pymongo.ASCENDING)], unique=True, name='index_cod')

        academia = Academia()
        break

    except Exception as e:
        print(e)


app = FastAPI(root_path='/api/courses')


@app.get('')
async def get_courses(request: Request, authorization: Annotated[str | None, Header()] = None):
    check_authorization(authorization, [Role.ADMIN])

    query = courses.find({}, {'cod': 1})
    course_list = []
    for course in query:
        course_list.append(course['cod'])

    links = {
        'self': {
            'href': f'{str(request.url.path)}{f'?{str(request.url.query)}' if request.url.query else ''}'
        }
    }

    if len(course_list) > 0:
        links['courses'] = {
            'href': f'{str(request.url.path)}/{{cod}}',
            'type': 'GET'
        }

    links['create'] = {
        'href': f'{str(request.url.path)}?{{cod}}',
        'type': 'POST'
    }

    if len(course_list) > 0:
        links['delete'] = {
            'href': f'{str(request.url.path)}/{{cod}}',
            'type': 'DELETE'
        }

    return JSONResponse({
        'lectures': {
            'list': course_list,
            '_links': links
        }
    })


@app.get('/{code}')
async def get_course(code: str, request: Request, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.SERVICE, Role.PROFESSOR, Role.STUDENT])

    try:
        if claims['role'] == Role.STUDENT and not await academia.is_student_enrolled(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)

        course = courses.find_one({'cod': code}, {'fișiere': 0})

        if course is None:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

        url = None
        if claims['role'] != Role.SERVICE:
            url = await academia.lecture_info_page(code)
            if url is None and claims['role'] == Role.PROFESSOR:
                raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)

        links = {
            'self': {
                'href': f'{str(request.url.path)}{f'?{str(request.url.query)}' if request.url.query else ''}'
            },
        }

        if url is not None:
            links['info'] = {
                'href': url,
                'type': 'GET'
            }

        links['sections'] = {
            'href': f'{str(request.url.path)}/{{section}}',
            'type': 'GET'
        }


        content_size = 0
        for x in course['disciplină']:
            if x != 'evaluare':
                content_size += len(course['disciplină'][x])

        if claims['role'] != Role.SERVICE:
            if content_size > 0:
                links['content'] = {
                    'href': f'{str(request.url.path)}/{{section}}/{{content}}',
                    'type': 'GET'
                }

            if (
                    (claims['role'] == Role.PROFESSOR and await academia.is_professor_holder(claims, code)) or
                    claims['role'] == Role.ADMIN
            ):
                links['delete'] = {
                    'href': f'{str(request.url.path)}',
                    'type': 'DELETE'
                }
                links['modify_evaluation']= {
                    'href': f'{str(request.url.path)}/evaluare',
                    'type': 'POST'
                }
                links['add_content'] = {
                    'href': f'{str(request.url.path)}/{{section}}/{{content}}',
                    'type': 'PUT'
                }

                if content_size > 0:
                    links['delete_content'] =  {
                        'href': f'{str(request.url.path)}/{{section}}/{{content}}',
                        'type': 'DELETE'
                    }

        course['disciplină']['_links'] = links
        return JSONResponse({'lecture': course['disciplină']})

    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)


@app.post('', status_code=status.HTTP_201_CREATED)
async def add_course_page(
        request: Request, code: str | None = None, authorization: Annotated[str | None, Header()] = None
):
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

    url = await academia.lecture_info_page(code)

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
                'laborator':{}
            },
            'fișiere': {}
        })

        course = courses.find_one({'cod': code}, {'fișiere': 0})

        links = {
            'self': {
                'href': f'{str(request.url.path)}/{code}'
            }
        }

        if url is not None:
            links['info'] = {
                'href': url,
                'type': 'GET'
            }

        links['sections'] = {
            'href': f'{str(request.url.path)}/{{section}}',
            'type': 'GET'
        }
        links['delete'] = {
            'href': f'{str(request.url.path)}',
            'type': 'DELETE'
        },
        links['modify_evaluation'] = {
            'href': f'{str(request.url.path)}/evaluare',
            'type': 'POST'
        },
        links['add_content'] = {
            'href': f'{str(request.url.path)}/{{section}}/{{content}}',
            'type': 'PUT'
        }

        course['disciplină']['_links'] = links
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
async def get_section(
        code: str, section: str, request: Request, authorization: Annotated[str | None, Header()] = None
):
    claims = check_authorization(authorization, [Role.ADMIN, Role.SERVICE, Role.PROFESSOR, Role.STUDENT])

    try:
        if claims['role'] == Role.STUDENT and not await academia.is_student_enrolled(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)

        course = courses.find_one({'cod': code}, {'fișiere': 0})
        if course is None:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

        if section not in course['disciplină']:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

        links = {
            'self': {
                'href': f'{str(request.url.path)}{f'?{str(request.url.query)}' if request.url.query else ''}'
            }
        }

        if claims['role'] != Role.SERVICE:
            if section != 'evaluare':
                if len(course['disciplină'][section]) > 0:
                    links['content'] = {
                        'href': f'{str(request.url.path)}/{{section}}/{{content}}',
                        'type': 'GET'
                    }

            if (
                    (claims['role'] == Role.PROFESSOR and await academia.is_professor_holder(claims, code)) or
                    claims['role'] == Role.ADMIN
            ):
                if section != 'evaluare':
                    links['add_content'] = {
                        'href': f'{str(request.url.path)}/{{section}}/{{content}}',
                        'type': 'PUT'
                    }

                    if len(course['disciplină'][section]) > 0:
                        links['delete_content'] = {
                            'href': f'{str(request.url.path)}/{{section}}/{{content}}',
                            'type': 'DELETE'
                        }
                else:
                    links['modify'] = {
                        'href': f'{str(request.url.path)}',
                        'type': 'POST'
                    }

        return JSONResponse({"lecture_section": {
            section: course['disciplină'][section],
            "_links": links
        }})

    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

@app.post('/{code}/evaluare', status_code=status.HTTP_204_NO_CONTENT)
async def add_evaluation(code: str, data: dict, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR, Role.STUDENT])

    try:
        if claims['role'] == Role.STUDENT and not await academia.is_student_enrolled(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    course = courses.find_one({'cod': code}, {'fișiere': 0})
    if course is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    try:
        percentage = 0
        for x in data:
            percentage += int(data[x]['pondere'])

        if percentage > 100:
            raise HTTPException(status_code=status.HTTP_409_CONFLICT)

        courses.update_one({'cod': code}, {'$set': {
            f'disciplină.evaluare.parcurs': {},
            f'disciplină.evaluare.finală': {}
        }})

        examination = await academia.get_lecture_examination(code)
        if examination is None:
            raise HTTPException(status_code=status.HTTP_409_CONFLICT)

        for x in data:
            if x == 'finală':
                courses.update_one({'cod': code}, {'$set': {
                    f'disciplină.evaluare.finală.{course['disciplină']['evaluare']['finală'][examination]}': data[x]
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

    course = courses.find_one({'cod': code}, {f'fișiere.{content}': 1, f'disciplină.{section}.{content}': 1})
    if course is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    if section not in course['disciplină']:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    if content not in course['disciplină'][section]:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    return Response(content=course['fișiere'][content], media_type=course['disciplină'][section][content]['type'])


@app.put('/{code}/{section}/{content}', status_code=status.HTTP_204_NO_CONTENT)
async def set_content(
        code: str, section: str, content: str, request: Request, data = Body(default=None),
        file: str | None = None, authorization: Annotated[str | None, Header()] = None
):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR])

    try:
        if claims['role'] == Role.PROFESSOR and not await academia.is_professor_holder(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    course = courses.find_one({'cod': code}, {'fișiere': 0})
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

    courses.update_one({'cod': code}, {'$set': {
        f'disciplină.{section}.{content}': {
            'file': file if file is not None else content,
            'type': request.headers.get('content-type'),
            'size': request.headers.get('content-length')
        },
        f'fișiere.{content}': data
    }})


@app.delete('/{code}/{section}/{content}', status_code=status.HTTP_204_NO_CONTENT)
async def delete_content(code: str, section: str, content: str, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR])

    try:
        if claims['role'] == Role.PROFESSOR and not await academia.is_professor_holder(claims, code):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)
    except ServiceException:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE)

    course = courses.find_one({'cod': code}, {'fișiere': 0})
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

    courses.update_one({'cod': code}, {'$unset': {
        f'disciplină.{section}.{content}': "",
        f'fișiere.{content}': ""
    }})


def check_authorization(authorization: Annotated[str | None, Header()], roles: list[Role]) -> dict:
    try:
        claims = Authorization.check_authorization(authorization, roles)
    except Unauthenticated:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED)
    except Unauthorized:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)

    return claims
