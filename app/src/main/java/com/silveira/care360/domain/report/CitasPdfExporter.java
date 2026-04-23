package com.silveira.care360.domain.report;

import android.net.Uri;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Cita;

import java.util.List;

public interface CitasPdfExporter {

    void export(List<Cita> citas, ResultCallback<Result> callback);

    class Result {
        private final Uri uri;
        private final String fileName;

        public Result(Uri uri, String fileName) {
            this.uri = uri;
            this.fileName = fileName;
        }

        public Uri getUri() {
            return uri;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
