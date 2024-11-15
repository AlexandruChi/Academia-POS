from typing import Annotated

from fastapi import FastAPI, HTTPException, status, Header
from fastapi.responses import JSONResponse
import pymongo

client = pymongo.MongoClient("mongodb://localhost:27017")
db = client["discipline"]
courses = db["disciplină"]

app = FastAPI()


@app.get("/api/courses/{code}")
async def get_course(code: str, authorization: Annotated[str | None, Header()] = None):
    if authorization is None or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED)

    course = courses.find_one({"cod": code})

    if course is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)

    return JSONResponse(course["date"])


@app.put("/api/courses/{code}", status_code=status.HTTP_204_NO_CONTENT)
async def set_course(code: str, data: dict, authorization: Annotated[str | None, Header()] = None):
    if authorization is None or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED)

    courses.replace_one({"cod": code}, {"cod": str(code), "date": data}, True)


@app.delete("/api/courses/{code}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_course(code: str, authorization: Annotated[str | None, Header()] = None):
    if authorization is None or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED)

    courses.delete_one({"cod": code})