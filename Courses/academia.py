from fastapi import status

import config
import httpx

from authorization import Authorization, Role
from exceptions import *

# noinspection HttpUrlsUsage
PROTOCOL = 'http://'

class Academia:
    def __init__(self):
        self.token = Authorization.get_authorization(config.USERNAME, config.PASSWORD)

    def __del__(self):
        Authorization.remove_authorization(self.token)

    async def is_student_enrolled(self, claims: dict, lecture: str) -> bool | None:
        if claims['role'] != Role.STUDENT:
            return None

        return await self.check_user_lecture(claims, lecture)

    async def is_professor_holder(self, claims: dict, lecture: str) -> bool | None:
        if claims['role'] != Role.PROFESSOR:
            return None

        return await self.check_user_lecture(claims, lecture)

    async def get_lecture_examination(self, lecture: str) -> str | None:
        tries = 2

        try:
            while tries > 0:
                tries -= 1

                response = await httpx.AsyncClient().get(
                    f'{PROTOCOL}{config.ACADEMIA_HOST}{config.ACADEMIA_PATH}/lectures/{lecture}',
                    headers={'authorization': f'Bearer {self.token}'}
                )

                if response.status_code == status.HTTP_401_UNAUTHORIZED:
                    self.__init__()
                    continue

                if response.status_code == status.HTTP_404_NOT_FOUND:
                    return None

                if response.status_code != status.HTTP_200_OK:
                    break

                json = response.json()

                try:
                    return json['lecture']['examinationType']
                except AttributeError:
                    break

        except httpx.ConnectError:
            pass

        raise ServiceException()

    async def check_user_lecture(self, claims: dict, lecture: str) -> bool | None:
        tries = 2

        match claims['role']:
            case Role.STUDENT:
                container = 'students'
            case Role.PROFESSOR:
                container = 'professors'
            case _:
                return None

        try:
            while tries > 0:
                tries -= 1

                response = await httpx.AsyncClient().get(
                    f'{PROTOCOL}{config.ACADEMIA_HOST}{config.ACADEMIA_PATH}/{container}?email={claims["email"]}',
                    headers={'authorization': f'Bearer {self.token}'}
                )

                if response.status_code == status.HTTP_401_UNAUTHORIZED:
                    self.__init__()
                    continue

                if response.status_code != status.HTTP_200_OK:
                    break

                json = response.json()

                try:
                    student_list = json[container]['list']
                    if len(student_list) != 1:
                        raise AttributeError()

                    student_id = student_list[0]['id']
                    lectures_link = json[container]['_links']['lectures']
                    lectures_path = lectures_link['href'].replace('{id}', str(student_id))
                    if lectures_link['type'] != 'GET':
                        raise AttributeError()

                except AttributeError:
                    break

                response = await httpx.AsyncClient().get(
                    f'{PROTOCOL}{config.ACADEMIA_HOST}{lectures_path}',
                    headers={'authorization': f'Bearer {self.token}'}
                )

                if response.status_code == status.HTTP_401_UNAUTHORIZED:
                    self.__init__()
                    continue

                if response.status_code != status.HTTP_200_OK:
                    break

                json = response.json()

                try:
                    return lecture in [x['code'] for x in json['lectures']['list']]
                except AttributeError:
                    break

        except httpx.ConnectError:
            pass

        raise ServiceException()

    async def lecture_info_page(self, lecture: str) -> str | None:
        tries = 2

        try:
            while tries > 0:
                tries -= 1

                url = f'{config.ACADEMIA_HOST}{config.ACADEMIA_PATH}/lectures/{lecture}'

                response = await httpx.AsyncClient().get(
                    f'{PROTOCOL}{url}',
                    headers={'authorization': f'Bearer {self.token}'}
                )

                if response.status_code == status.HTTP_401_UNAUTHORIZED:
                    self.__init__()
                    continue

                if response.status_code == status.HTTP_404_NOT_FOUND:
                    return None

                if response.status_code != status.HTTP_200_OK:
                    break

                return url

        except httpx.ConnectError:
            pass

        raise ServiceException()