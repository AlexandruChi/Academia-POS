package pos.alexandruchi.academia.service;

import com.auth0.jwt.JWT;
import com.google.protobuf.Empty;
import pos.alexandruchi.academia.exception.service.ServiceException;
import io.grpc.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pos.alexandruchi.academia.IDM.*;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executor;

@Service
public class IDMService {
    private final ManagedChannel channel;
    private final IDMGrpc.IDMBlockingStub stub;

    public IDMService(@Value("${idm.host}") String host, @Value("${idm.port}") int port) {
        try {
            channel = ManagedChannelBuilder
                    .forAddress(host, port)
                    .usePlaintext()
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        stub = IDMGrpc.newBlockingStub(channel);
    }

    public Boolean Validate(String token) {
        if (token == null) {
            return false;
        }

        CallCredentials callCredentials = new CallCredentials() {

            @Override
            public void applyRequestMetadata(
                    RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier
            ) {
                Metadata metadata = new Metadata();
                Metadata.Key<String> authorization = Metadata.Key.of(
                        "Authorization", Metadata.ASCII_STRING_MARSHALLER
                );
                metadata.put(authorization, "Bearer " + token);
                metadataApplier.apply(metadata);
            }
        };

        try {
            //noinspection ResultOfMethodCallIgnored
            stub.withCallCredentials(callCredentials).validate(Empty.newBuilder().build());
        } catch (Exception e) {
            if (e instanceof StatusRuntimeException) {
                if (((StatusRuntimeException) e).getStatus().getCode() == Status.Code.UNAUTHENTICATED) {
                    return false;
                }
            }

            throw new ServiceException();
        }

        return true;
    }

    public String Authenticate(String email, String password) {
        try {
            return stub.authenticate(IDMOuterClass.login.newBuilder()
                    .setUsername(email)
                    .setPassword(password)
                    .build()
            ).getToken();
        } catch (Exception e) {
            return null;
        }
    }

    public void Deauthenticate(String token) {
        if (token == null) {
            return;
        }

        CallCredentials callCredentials = new CallCredentials() {

            @Override
            public void applyRequestMetadata(
                    RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier
            ) {
                Metadata metadata = new Metadata();
                Metadata.Key<String> authorization = Metadata.Key.of(
                        "Authorization", Metadata.ASCII_STRING_MARSHALLER
                );
                metadata.put(authorization, "Bearer " + token);
                metadataApplier.apply(metadata);
            }
        };

        try {
            //noinspection ResultOfMethodCallIgnored
            stub.withCallCredentials(callCredentials).deauthenticate(Empty.newBuilder().build());
        } catch (Exception ignored) {}
    }

    public AuthorizationService.Claims getClaims(String authorization) {
        String token;

        String email;
        AuthorizationService.Role role;

        if (
            authorization == null || !authorization.startsWith("Bearer ") ||
            !Validate(token = authorization.substring(7)) ||

            (role = switch (JWT.decode(token).getClaim("role").asString()) {
                case "admin" -> AuthorizationService.Role.ADMIN;
                case "service" -> AuthorizationService.Role.SERVICE;
                case "student" -> AuthorizationService.Role.STUDENT;
                case "professor" -> AuthorizationService.Role.PROFESSOR;
                default -> null;
            }) == null ||

            (email = JWT.decode(token).getClaim("sub").asString()) == null ||
            email.isEmpty()
        ) {
            return null;
        }

        return new AuthorizationService.Claims(email, role);
    }

    @PreDestroy
    private void destroy() {
        channel.shutdown();
    }
}
