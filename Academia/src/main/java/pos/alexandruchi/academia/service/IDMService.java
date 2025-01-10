package pos.alexandruchi.academia.service;

import com.auth0.jwt.JWT;
import com.google.protobuf.Empty;
import io.grpc.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import pos.alexandruchi.academia.IDM.*;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executor;

@Service
public class IDMService {
    private final ManagedChannel channel;
    private final IDMGrpc.IDMBlockingStub stub;

    public IDMService() {
        try {
            channel = ManagedChannelBuilder
                    .forAddress("::", 50000)
                    .usePlaintext()
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        stub = IDMGrpc.newBlockingStub(channel);
    }

    private @NotNull Boolean Validate(String token) {
        if (token == null) {
            return false;
        }

        CallCredentials callCredentials = new CallCredentials() {

            @Override
            public void applyRequestMetadata(
                    RequestInfo requestInfo, Executor executor, @NotNull MetadataApplier metadataApplier
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
            return false;
        }

        return true;
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
