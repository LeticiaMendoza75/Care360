package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.auth.EmailAccessMethod;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.repository.AuthRepository;

import javax.inject.Inject;

public class ResetPasswordFlowUseCase {

    private final AuthRepository authRepository;

    @Inject
    public ResetPasswordFlowUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void execute(String email, ResultCallback<Result> callback) {
        authRepository.checkEmailAccessMethod(email, new ResultCallback<EmailAccessMethod>() {
            @Override
            public void onSuccess(EmailAccessMethod result) {
                if (result == EmailAccessMethod.NO_ACCOUNT) {
                    callback.onSuccess(Result.noAccount());
                    return;
                }

                if (result == EmailAccessMethod.GOOGLE_ONLY) {
                    callback.onSuccess(Result.googleOnly());
                    return;
                }

                if (result == EmailAccessMethod.PASSWORD) {
                    sendResetEmail(email, callback);
                    return;
                }

                if (result == EmailAccessMethod.UNSUPPORTED) {
                    callback.onSuccess(Result.error("Metodo de acceso no soportado todavia."));
                    return;
                }

                callback.onSuccess(Result.error("No se pudo comprobar el email"));
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(Result.error(message != null ? message : "No se pudo comprobar el email"));
            }
        });
    }

    private void sendResetEmail(String email, ResultCallback<Result> callback) {
        authRepository.sendPasswordResetEmail(email, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(Result.emailSent());
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(Result.error("No se pudo enviar el email. Intentalo de nuevo."));
            }
        });
    }

    public static class Result {
        public enum Type {
            NO_ACCOUNT,
            GOOGLE_ONLY,
            EMAIL_SENT,
            ERROR
        }

        private final Type type;
        private final String message;

        private Result(Type type, String message) {
            this.type = type;
            this.message = message;
        }

        public static Result noAccount() {
            return new Result(Type.NO_ACCOUNT, null);
        }

        public static Result googleOnly() {
            return new Result(Type.GOOGLE_ONLY, null);
        }

        public static Result emailSent() {
            return new Result(Type.EMAIL_SENT, null);
        }

        public static Result error(String message) {
            return new Result(Type.ERROR, message);
        }

        public Type getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }
    }
}
