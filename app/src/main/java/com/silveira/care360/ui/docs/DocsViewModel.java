package com.silveira.care360.ui.docs;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Documento;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.storage.DocumentFileStorage;
import com.silveira.care360.domain.usecase.DeleteDocumentoUseCase;
import com.silveira.care360.domain.usecase.GetCurrentUserUseCase;
import com.silveira.care360.domain.usecase.LoadDocumentosDataUseCase;
import com.silveira.care360.domain.usecase.SaveDocumentoUseCase;
import com.silveira.care360.domain.usecase.UpdateDocumentoUseCase;
import com.silveira.care360.domain.usecase.UploadDocumentoFileUseCase;
import com.silveira.care360.domain.usecase.UploadDocumentoPhotoUseCase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DocsViewModel extends ViewModel {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final LoadDocumentosDataUseCase loadDocumentosDataUseCase;
    private final UploadDocumentoFileUseCase uploadDocumentoFileUseCase;
    private final UploadDocumentoPhotoUseCase uploadDocumentoPhotoUseCase;
    private final SaveDocumentoUseCase saveDocumentoUseCase;
    private final UpdateDocumentoUseCase updateDocumentoUseCase;
    private final DeleteDocumentoUseCase deleteDocumentoUseCase;

    private final MutableLiveData<DocsState> _state = new MutableLiveData<>(new DocsState());
    public LiveData<DocsState> state = _state;

    private final MutableLiveData<DocsAction> _action = new MutableLiveData<>();
    public LiveData<DocsAction> action = _action;

    @Inject
    public DocsViewModel(GetCurrentUserUseCase getCurrentUserUseCase,
                         LoadDocumentosDataUseCase loadDocumentosDataUseCase,
                         UploadDocumentoFileUseCase uploadDocumentoFileUseCase,
                         UploadDocumentoPhotoUseCase uploadDocumentoPhotoUseCase,
                         SaveDocumentoUseCase saveDocumentoUseCase,
                         UpdateDocumentoUseCase updateDocumentoUseCase,
                         DeleteDocumentoUseCase deleteDocumentoUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.loadDocumentosDataUseCase = loadDocumentosDataUseCase;
        this.uploadDocumentoFileUseCase = uploadDocumentoFileUseCase;
        this.uploadDocumentoPhotoUseCase = uploadDocumentoPhotoUseCase;
        this.saveDocumentoUseCase = saveDocumentoUseCase;
        this.updateDocumentoUseCase = updateDocumentoUseCase;
        this.deleteDocumentoUseCase = deleteDocumentoUseCase;
    }

    public void loadDocumentos() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }
        DocsState current = _state.getValue();
        _state.setValue(new DocsState(true, current != null ? current.activeGroupId : null,
                current != null ? current.documentos : new ArrayList<>(), null));
        loadDocumentosDataUseCase.execute(currentUser.getId(), new ResultCallback<LoadDocumentosDataUseCase.Result>() {
            @Override
            public void onSuccess(LoadDocumentosDataUseCase.Result result) {
                _state.postValue(new DocsState(false, result.getActiveGroupId(), result.getDocumentos(), null));
            }

            @Override
            public void onError(String message) {
                emitError(message != null ? message : "No se pudieron cargar los documentos");
            }
        });
    }

    public void onAddDocumentoClicked() {
        _action.setValue(new ShowSourcePickerAction());
    }

    public void onVerMasClicked(Documento documento) {
        if (documento != null) _action.setValue(new ShowDocumentoDetailAction(documento));
    }

    public void onGestionarClicked(Documento documento) {
        if (documento != null) _action.setValue(new ShowDocumentoEditorAction(documento));
    }

    public void onDeleteDocumentoClicked(Documento documento) {
        if (documento != null) _action.setValue(new ConfirmDeleteDocumentoAction(documento));
    }

    public void onDocumentoUriConfirmed(String titulo,
                                        String tipo,
                                        String fechaDocumento,
                                        String notas,
                                        Uri fileUri) {
        User currentUser = getCurrentUserUseCase.execute();
        DocsState current = _state.getValue();
        String activeGroupId = current != null ? current.activeGroupId : null;
        if (currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }
        if (isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No hay grupo activo"));
            return;
        }
        if (isBlank(titulo) || fileUri == null) {
            _action.setValue(new ShowMessageAction("Indica el título del documento y selecciona el archivo"));
            return;
        }

        _state.setValue(new DocsState(true, activeGroupId, current != null ? current.documentos : new ArrayList<>(), null));
        uploadDocumentoFileUseCase.execute(activeGroupId, fileUri, new ResultCallback<DocumentFileStorage.UploadResult>() {
            @Override
            public void onSuccess(DocumentFileStorage.UploadResult uploadResult) {
                saveDocumentoUseCase.execute(activeGroupId, currentUser.getId(), titulo, tipo, fechaDocumento, notas,
                        uploadResult.getFileUrl(), uploadResult.getFileName(), uploadResult.getMimeType(),
                        new ResultCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                _action.postValue(new ShowMessageAction("Documento guardado"));
                                loadDocumentos();
                            }

                            @Override
                            public void onError(String message) {
                                emitError("No se pudo guardar el documento");
                            }
                        });
            }

            @Override
            public void onError(String message) {
                emitError(message != null ? message : "No se pudo subir el documento");
            }
        });
    }

    public void onDocumentoPhotoConfirmed(String titulo,
                                          String tipo,
                                          String fechaDocumento,
                                          String notas,
                                          byte[] jpegBytes) {
        User currentUser = getCurrentUserUseCase.execute();
        DocsState current = _state.getValue();
        String activeGroupId = current != null ? current.activeGroupId : null;
        if (currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }
        if (isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No hay grupo activo"));
            return;
        }
        if (isBlank(titulo) || jpegBytes == null || jpegBytes.length == 0) {
            _action.setValue(new ShowMessageAction("Indica el título del documento y la foto"));
            return;
        }

        _state.setValue(new DocsState(true, activeGroupId, current != null ? current.documentos : new ArrayList<>(), null));
        uploadDocumentoPhotoUseCase.execute(activeGroupId, jpegBytes, new ResultCallback<DocumentFileStorage.UploadResult>() {
            @Override
            public void onSuccess(DocumentFileStorage.UploadResult uploadResult) {
                saveDocumentoUseCase.execute(activeGroupId, currentUser.getId(), titulo, tipo, fechaDocumento, notas,
                        uploadResult.getFileUrl(), uploadResult.getFileName(), uploadResult.getMimeType(),
                        new ResultCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                _action.postValue(new ShowMessageAction("Documento guardado"));
                                loadDocumentos();
                            }

                            @Override
                            public void onError(String message) {
                                emitError("No se pudo guardar el documento");
                            }
                        });
            }

            @Override
            public void onError(String message) {
                emitError(message != null ? message : "No se pudo subir la foto");
            }
        });
    }

    public void onDocumentoUpdateConfirmed(Documento documento) {
        DocsState current = _state.getValue();
        String activeGroupId = current != null ? current.activeGroupId : null;
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }
        if (documento == null || isBlank(documento.getTitulo())) {
            _action.setValue(new ShowMessageAction("El titulo es obligatorio"));
            return;
        }
        if (isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No hay grupo activo"));
            return;
        }
        _state.setValue(new DocsState(true, activeGroupId, current != null ? current.documentos : new ArrayList<>(), null));
        updateDocumentoUseCase.execute(activeGroupId, currentUser.getId(), documento, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _action.postValue(new ShowMessageAction("Documento actualizado"));
                loadDocumentos();
            }

            @Override
            public void onError(String message) {
                emitError("No se pudo actualizar el documento");
            }
        });
    }

    public void confirmDeleteDocumento(Documento documento) {
        DocsState current = _state.getValue();
        String activeGroupId = current != null ? current.activeGroupId : null;
        if (documento == null || isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No se pudo eliminar el documento"));
            return;
        }
        deleteDocumentoUseCase.execute(activeGroupId, documento, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _action.postValue(new ShowMessageAction("Documento eliminado"));
                loadDocumentos();
            }

            @Override
            public void onError(String message) {
                emitError("No se pudo eliminar el documento");
            }
        });
    }

    public void onOpenDocumentoClicked(Documento documento) {
        if (documento == null || isBlank(documento.getFileUrl())) {
            _action.setValue(new ShowMessageAction("No se pudo abrir el documento"));
            return;
        }
        _action.setValue(new OpenDocumentoAction(documento.getFileUrl(), documento.getMimeType()));
    }

    public void onActionHandled() {
        _action.setValue(null);
    }

    private void emitError(String message) {
        DocsState current = _state.getValue();
        _state.postValue(new DocsState(false,
                current != null ? current.activeGroupId : null,
                current != null ? current.documentos : new ArrayList<>(),
                message));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class DocsState {
        public final boolean isLoading;
        public final String activeGroupId;
        public final List<Documento> documentos;
        public final String errorMessage;

        public DocsState() {
            this(false, null, new ArrayList<>(), null);
        }

        public DocsState(boolean isLoading, String activeGroupId, List<Documento> documentos, String errorMessage) {
            this.isLoading = isLoading;
            this.activeGroupId = activeGroupId;
            this.documentos = documentos != null ? documentos : new ArrayList<>();
            this.errorMessage = errorMessage;
        }
    }

    public static abstract class DocsAction { }
    public static class ShowSourcePickerAction extends DocsAction { }
    public static class ShowDocumentoDetailAction extends DocsAction {
        public final Documento documento;
        public ShowDocumentoDetailAction(Documento documento) { this.documento = documento; }
    }
    public static class ShowDocumentoEditorAction extends DocsAction {
        public final Documento documento;
        public ShowDocumentoEditorAction(Documento documento) { this.documento = documento; }
    }
    public static class ConfirmDeleteDocumentoAction extends DocsAction {
        public final Documento documento;
        public ConfirmDeleteDocumentoAction(Documento documento) { this.documento = documento; }
    }
    public static class ShowMessageAction extends DocsAction {
        public final String message;
        public ShowMessageAction(String message) { this.message = message; }
    }
    public static class OpenDocumentoAction extends DocsAction {
        public final String fileUrl;
        public final String mimeType;

        public OpenDocumentoAction(String fileUrl, String mimeType) {
            this.fileUrl = fileUrl;
            this.mimeType = mimeType;
        }
    }
}
