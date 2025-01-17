from typing import Annotated

import config
from academia import Academia
from authorization import Authorization, Role
from exceptions import *

from fastapi import FastAPI, HTTPException, status, Header
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

    if claims['role'] == Role.STUDENT and not await academia.is_student_enrolled(claims, code):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)

    course = courses.find_one({'cod': code})

    if course is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    return JSONResponse(course['date'])


@app.put('/{code}', status_code=status.HTTP_204_NO_CONTENT)
async def set_course(code: str, data: dict, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR])

    if claims['role'] == Role.PROFESSOR and not await academia.is_professor_holder(claims, code):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)

    courses.replace_one({'cod': code}, {'cod': str(code), 'date': data}, True)


@app.delete('/{code}', status_code=status.HTTP_204_NO_CONTENT)
async def delete_course(code: str, authorization: Annotated[str | None, Header()] = None):
    claims = check_authorization(authorization, [Role.ADMIN, Role.PROFESSOR])

    if claims['role'] == Role.PROFESSOR and not await academia.is_professor_holder(claims, code):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)

    courses.delete_one({'cod': code})


def check_authorization(authorization: Annotated[str | None, Header()], roles: list[Role]) -> dict:
    try:
        claims = Authorization.check_authorization(authorization, roles)
    except Unauthenticated:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED)
    except Unauthorized:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN)

    return claims