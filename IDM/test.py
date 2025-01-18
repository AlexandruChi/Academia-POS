import grpc
import IDM_pb2
import IDM_pb2_grpc

client = grpc.insecure_channel('localhost:50000')
stub = IDM_pb2_grpc.IDMStub(client)

print(stub.Authenticate(IDM_pb2.login(username='admin', password='admin')).token)