package cn.ggsn.openrxlight.errorx;

import com.alibaba.dashscope.utils.JsonUtils;

import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

        @Override
        public Response toResponse(Exception exception) {
                log.error("Unhandled exception caught: ", exception);
                if (exception instanceof BizException) {
                        BizException bizException = (BizException) exception;
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .type(MediaType.APPLICATION_JSON)
                                        .entity(new cn.ggsn.openrxlight.errorx.ErrorResponse(bizException.getCode(),
                                                        bizException.getMessage()).toString())
                                        .build();
                } else if (exception instanceof IllegalArgumentException) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .type(MediaType.APPLICATION_JSON)
                                        .entity(new cn.ggsn.openrxlight.errorx.ErrorResponse(400,
                                                        exception.getMessage()).toString())
                                        .build();
                } else if (exception instanceof NoResultException) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .type(MediaType.APPLICATION_JSON)
                                        .entity(new cn.ggsn.openrxlight.errorx.ErrorResponse(400, "Resource Not Found")
                                                        .toString())
                                        .build();
                } else if (exception instanceof UnsupportedOperationException) {
                        return Response.status(Response.Status.NOT_IMPLEMENTED)
                                        .type(MediaType.APPLICATION_JSON)
                                        .entity(new cn.ggsn.openrxlight.errorx.ErrorResponse(501,
                                                        exception.getMessage()).toString())
                                        .build();
                } else if (exception instanceof RuntimeException) {
                        if (exception.getCause() instanceof BizException) {
                                BizException bizException = (BizException) exception.getCause();
                                return Response.status(Response.Status.BAD_REQUEST)
                                                .type(MediaType.APPLICATION_JSON)
                                                .entity(new cn.ggsn.openrxlight.errorx.ErrorResponse(
                                                                bizException.getCode(),
                                                                bizException.getMessage()).toString())
                                                .build();
                        } else if (exception.getCause() instanceof StatusException) {
                                StatusException statusException = (StatusException) exception.getCause();
                                switch (statusException.getStatus().getCode()) {
                                        case DEADLINE_EXCEEDED:
                                                return Response.status(Response.Status.REQUEST_TIMEOUT)
                                                                .type(MediaType.APPLICATION_JSON)
                                                                .entity(new cn.ggsn.openrxlight.errorx.ErrorResponse(
                                                                                500,
                                                                                String.format("gRPC request timeout Error: %s",
                                                                                                statusException.getStatus()
                                                                                                                .getDescription()))
                                                                                .toString())
                                                                .build();
                                        case INVALID_ARGUMENT:
                                        case ALREADY_EXISTS:
                                        case PERMISSION_DENIED:
                                        case UNAUTHENTICATED:
                                        case NOT_FOUND:
                                                return Response.status(Response.Status.BAD_REQUEST)
                                                                .type(MediaType.APPLICATION_JSON)
                                                                .entity(JsonUtils.fromJson(
                                                                                statusException.getStatus()
                                                                                                .getDescription(),
                                                                                cn.ggsn.openrxlight.errorx.ErrorResponse.class)
                                                                                .toString())
                                                                .build();
                                        default:
                                                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                                .type(MediaType.APPLICATION_JSON)
                                                                .entity(new cn.ggsn.openrxlight.errorx.ErrorResponse(
                                                                                500,
                                                                                String.format("internal Error: code -- %s, cause -- %s",
                                                                                                statusException.getStatus()
                                                                                                                .getCode(),
                                                                                                statusException.getStatus()
                                                                                                                .getDescription()))
                                                                                .toString())
                                                                .build();
                                }

                        } else if (exception.getCause() instanceof StatusRuntimeException) {
                                StatusRuntimeException statusException = (StatusRuntimeException) exception.getCause();
                                switch (statusException.getStatus().getCode()) {
                                        case DEADLINE_EXCEEDED:
                                                return Response.status(Response.Status.REQUEST_TIMEOUT)
                                                                .type(MediaType.APPLICATION_JSON)
                                                                .entity(new cn.ggsn.openrxlight.errorx.ErrorResponse(
                                                                                500,
                                                                                String.format("gRPC request timeout Error: %s",
                                                                                                statusException.getStatus()
                                                                                                                .getDescription()))
                                                                                .toString())
                                                                .build();
                                        case INVALID_ARGUMENT:
                                        case ALREADY_EXISTS:
                                        case PERMISSION_DENIED:
                                        case UNAUTHENTICATED:
                                        case NOT_FOUND:
                                                return Response.status(Response.Status.BAD_REQUEST)
                                                                .type(MediaType.APPLICATION_JSON)
                                                                .entity(JsonUtils.fromJson(
                                                                                statusException.getStatus()
                                                                                                .getDescription(),
                                                                                cn.ggsn.openrxlight.errorx.ErrorResponse.class)
                                                                                .toString())
                                                                .build();
                                        default:
                                                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                                .type(MediaType.APPLICATION_JSON)
                                                                .entity(new cn.ggsn.openrxlight.errorx.ErrorResponse(
                                                                                500,
                                                                                String.format("internal Error: code -- %s, cause -- %s",
                                                                                                statusException.getStatus()
                                                                                                                .getCode(),
                                                                                                statusException.getStatus()
                                                                                                                .getDescription()))
                                                                                .toString())
                                                                .build();
                                }

                        } else {
                                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                .type(MediaType.APPLICATION_JSON)
                                                .entity(new cn.ggsn.openrxlight.errorx.ErrorResponse(500,
                                                                String.format("Internal Server Error %s",
                                                                                exception.getMessage()))
                                                                .toString())
                                                .build();
                        }
                } else {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .type(MediaType.APPLICATION_JSON)
                                        .entity(new cn.ggsn.openrxlight.errorx.ErrorResponse(500,
                                                        String.format("Internal Server Error %s",
                                                                        exception.getMessage()))
                                                        .toString())
                                        .build();
                }
        }

}
