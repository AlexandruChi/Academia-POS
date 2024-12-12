import datetime
import uuid
from concurrent import futures
import grpc
import jwt
from jwt import InvalidSignatureError, DecodeError, ExpiredSignatureError
from peewee import IntegrityError

import IDM_pb2
import IDM_pb2_grpc
import hashlib

from google.protobuf.empty_pb2 import Empty

from database import db
from model import User, InvalidToken

# noinspection SpellCheckingInspection
IDM_KEY = 'vYvAwpVXsEwtxyUbzWoqiTxPGUzZ4Qqn'

PORT = 50000

ROLES = ('admin', 'teacher', 'student')

class InvalidTokenError(RuntimeError):
    pass

JWT_ERRORS = (InvalidTokenError, ExpiredSignatureError, InvalidSignatureError, DecodeError)

class IDM(IDM_pb2_grpc.IDMServicer):
    def Authenticate(self, request, context):

        if not (request.username and request.password):
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            return IDM_pb2.token()

        passhash = hashlib.md5(request.password.encode()).hexdigest()
        user = User.select(
            User.email, User.role
        ).where(
            (User.email == request.username) & (User.password == passhash)
        ).first()

        if user is None:
            return IDM_pb2.token(token=None)

        payload = {
            'sub': user.email,
            'exp': datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(hours=1),
            'jti': str(uuid.uuid4()),
            'role': user.role,
        }

        token = jwt.encode(payload, IDM_KEY, 'HS256')

        return IDM_pb2.token(token=token)

    def Deauthenticate(self, request, context):
        exceptions = JWT_ERRORS + (IntegrityError,)

        authorization = dict(context.invocation_metadata()).get('authorization')

        if not authorization or authorization[:7] != 'Bearer ':
            context.set_code(grpc.StatusCode.UNAUTHENTICATED)
            return Empty()

        token = authorization[7:]

        try:
            decoded_jwt = jwt.decode(token, IDM_KEY, ['HS256'])
            InvalidToken.create(
                UUID=decoded_jwt['jti'],
                expiration=datetime.datetime.fromtimestamp(
                    decoded_jwt['exp'], datetime.timezone.utc
                )
            )

        except exceptions + (Exception,) as ex:
            context.set_code(grpc.StatusCode.UNAUTHENTICATED)
            if not isinstance(ex, exceptions):
                context.set_code(grpc.StatusCode.INTERNAL)

            return Empty()

        else:
            return Empty()

    def Register(self, request, context):
        exceptions = (IntegrityError,)

        if not (request.username and request.password) or request.role not in ROLES:
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            return Empty()

        try:
            passhash = hashlib.md5(request.password.encode()).hexdigest()
            User.create(
                email=request.username,
                password=passhash,
                role=request.role,
            )

        except exceptions + (Exception,) as ex:
            context.set_code(grpc.StatusCode.ALREADY_EXISTS)
            if not isinstance(ex, exceptions):
                context.set_code(grpc.StatusCode.INTERNAL)

            return Empty()

        else:
            return Empty()

    def Validate(self, request, context):
        exceptions = JWT_ERRORS

        authorization = dict(context.invocation_metadata()).get('authorization')

        if not authorization or authorization[:7] != 'Bearer ':
            context.set_code(grpc.StatusCode.UNAUTHENTICATED)
            return Empty()

        token = authorization[7:]

        try:
            decoded_jwt = jwt.decode(token, IDM_KEY, ['HS256'])
            if InvalidToken.select().where(InvalidToken.UUID == decoded_jwt['jti']):
                raise InvalidTokenError

        except exceptions + (Exception,) as ex:
            context.set_code(grpc.StatusCode.UNAUTHENTICATED)
            if not isinstance(ex, exceptions):
                context.set_code(grpc.StatusCode.INTERNAL)

            return Empty()

        else:
            return Empty()


def start():
    if not db.connect():
        raise RuntimeError('Database connection failed')

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    IDM_pb2_grpc.add_IDMServicer_to_server(IDM(), server)
    server.add_insecure_port(f'[::]:{PORT}')
    server.start()
    print(f'IDM server started on port {PORT}')
    return server


if __name__ == '__main__':
    try:
        gRPC = start()
    except Exception as e:
        print(e)
        exit(1)

    try:
        gRPC.wait_for_termination()
    except KeyboardInterrupt:
        gRPC.stop(0)
