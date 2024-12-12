import grpc
import IDM_pb2
import IDM_pb2_grpc

from google.protobuf.empty_pb2 import Empty

client = grpc.insecure_channel('localhost:50000')
stub = IDM_pb2_grpc.IDMStub(client)

username = 'vicvb'
password = 'password'
role = 'admin'

# stub.Register(IDM_pb2.register(username=username, password=password, role=role))

token = stub.Authenticate(IDM_pb2.login(username=username, password=password)).token

metadata = [('authorization', 'Bearer ' + token)]

stub.Validate(Empty(), metadata=metadata)