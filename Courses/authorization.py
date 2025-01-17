import enum
import jwt

import grpc
import IDM_pb2
import IDM_pb2_grpc

from google.protobuf.empty_pb2 import Empty

import config
from exceptions import *


class Role(enum.Enum):
    ADMIN = 'admin'
    STUDENT = 'student'
    PROFESSOR = 'professor'

    @staticmethod
    def of(value: str):
        for role in Role:
            if role.value == value:
                return role

        raise ValueError()


class Authorization:
    @staticmethod
    def get_authorization(username: str, password: str):
        client = grpc.insecure_channel(config.IDM_HOST)
        # noinspection PyUnresolvedReferences
        return IDM_pb2_grpc.IDMStub(client).Authenticate(IDM_pb2.login(username=username, password=password)).token

    @staticmethod
    def remove_authorization(authorization: str):
        client = grpc.insecure_channel(config.IDM_HOST)
        # noinspection PyUnresolvedReferences
        IDM_pb2_grpc.IDMStub(client).Deauthenticate(Empty(), metadata=(('authorization', authorization),))

    @staticmethod
    def check_authorization(authorization: str, roles: list[Role]) -> dict:
        claims = Authorization.get_claims(authorization)

        if claims["role"] not in roles:
            raise Unauthorized

        return claims

    @staticmethod
    def get_claims(authorization: str) -> dict:
        if not authorization or not authorization.startswith("Bearer "):
            raise Unauthenticated

        try:
            client = grpc.insecure_channel(config.IDM_HOST)
            IDM_pb2_grpc.IDMStub(client).Validate(Empty(), metadata=(('authorization', authorization),))
        except grpc.RpcError as ex:
            # noinspection PyUnresolvedReferences
            if ex.code() == grpc.StatusCode.UNAUTHENTICATED:
                raise Unauthenticated
            raise ex

        token = jwt.decode(authorization[7:], options={"verify_signature": False})

        try:
            return {
                "email": token.get("sub"),
                "role": Role.of(token.get("role")),
            }

        except ValueError:
            raise Exception()