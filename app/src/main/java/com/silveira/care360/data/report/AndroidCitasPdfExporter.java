package com.silveira.care360.data.report;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.silveira.care360.R;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Cita;
import com.silveira.care360.domain.report.CitasPdfExporter;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class AndroidCitasPdfExporter implements CitasPdfExporter {

    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 40;
    private static final int CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);

    private final Context context;

    @Inject
    public AndroidCitasPdfExporter(@ApplicationContext Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void export(List<Cita> citas, ResultCallback<Result> callback) {
        if (citas == null || citas.isEmpty()) {
            callback.onError(context.getString(R.string.citas_pdf_no_data));
            return;
        }

        PdfDocument document = new PdfDocument();
        Uri uri = null;
        try {
            PageCursor cursor = new PageCursor(document);
            PdfPaints paints = new PdfPaints();

            cursor.drawWrappedLine(context.getString(R.string.citas_pdf_title), paints.titlePaint, 32, true);
            String fechaGeneracion = context.getString(
                    R.string.citas_pdf_generated_at,
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date())
            );
            cursor.drawWrappedLine(fechaGeneracion, paints.metaPaint, 18, true);

            for (Cita cita : citas) {
                if (cita == null) {
                    continue;
                }

                cursor.ensureBlockSpace(120);
                cursor.drawDivider(paints.dividerPaint);
                cursor.drawWrappedLine(
                        safe(cita.getTitulo(), context.getString(R.string.citas_pdf_default_title)),
                        paints.sectionPaint,
                        24,
                        true
                );
                cursor.drawWrappedLine(
                        context.getString(R.string.citas_pdf_fecha, safe(cita.getFecha(), "—")),
                        paints.bodyBoldPaint,
                        18,
                        false
                );
                cursor.drawWrappedLine(
                        context.getString(R.string.citas_pdf_hora, safe(cita.getHora(), "—")),
                        paints.bodyBoldPaint,
                        18,
                        false
                );
                cursor.drawWrappedLine(
                        context.getString(R.string.citas_pdf_lugar, safe(cita.getLugar(), context.getString(R.string.citas_lugar_pendiente))),
                        paints.bodyPaint,
                        18,
                        false
                );
                cursor.drawWrappedLine(
                        context.getString(R.string.citas_pdf_profesional, safe(cita.getProfesional(), context.getString(R.string.citas_profesional_pendiente))),
                        paints.bodyPaint,
                        18,
                        false
                );
                if (!isBlank(cita.getPersonaEncargada())) {
                    cursor.drawWrappedLine(
                            context.getString(R.string.citas_pdf_persona_encargada, cita.getPersonaEncargada().trim()),
                            paints.bodyPaint,
                            18,
                            false
                    );
                }
                cursor.drawWrappedLine(
                        context.getString(
                                R.string.citas_pdf_recordatorio,
                                context.getString(
                                        cita.isRecordatorioActivo()
                                                ? R.string.citas_pdf_recordatorio_activo
                                                : R.string.citas_pdf_recordatorio_inactivo
                                )
                        ),
                        paints.bodyPaint,
                        18,
                        false
                );

                if (!isBlank(cita.getObservaciones())) {
                    cursor.drawWrappedParagraph(
                            context.getString(R.string.citas_pdf_observaciones, cita.getObservaciones().trim()),
                            paints.bodyPaint,
                            18
                    );
                }

                cursor.addGap(12);
            }

            finishPage(document, cursor);
            String fileName = "care360_citas_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            uri = createDownloadUri(fileName);
            if (uri == null) {
                callback.onError(context.getString(R.string.citas_pdf_create_error));
                return;
            }

            ContentResolver resolver = context.getContentResolver();
            OutputStream outputStream = resolver.openOutputStream(uri, "w");
            if (outputStream == null) {
                callback.onError(context.getString(R.string.citas_pdf_write_error));
                return;
            }

            document.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues completed = new ContentValues();
                completed.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(uri, completed, null, null);
            }

            callback.onSuccess(new Result(uri, fileName));
        } catch (Exception e) {
            if (uri != null) {
                try {
                    context.getContentResolver().delete(uri, null, null);
                } catch (Exception ignored) {
                }
            }
            callback.onError(context.getString(R.string.citas_pdf_generate_error));
        } finally {
            document.close();
        }
    }

    private Uri createDownloadUri(String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Care360");
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        }
        return context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
    }

    private void finishPage(PdfDocument document, PageCursor cursor) {
        if (cursor.page != null) {
            document.finishPage(cursor.page);
            cursor.page = null;
            cursor.canvas = null;
        }
    }

    private String safe(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class PdfPaints {
        final Paint titlePaint = buildPaint(0xFF16324F, 20f, true);
        final Paint sectionPaint = buildPaint(0xFF25313F, 16f, true);
        final Paint bodyBoldPaint = buildPaint(0xFF25313F, 12f, true);
        final Paint bodyPaint = buildPaint(0xFF374151, 12f, false);
        final Paint metaPaint = buildPaint(0xFF6B7280, 10f, false);
        final Paint dividerPaint = buildPaint(0xFFD1D5DB, 1f, false);

        private static Paint buildPaint(int color, float textSize, boolean bold) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTextSize(textSize);
            if (bold) {
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            }
            paint.setAntiAlias(true);
            return paint;
        }
    }

    private static class PageCursor {
        private final PdfDocument document;
        private PdfDocument.Page page;
        private android.graphics.Canvas canvas;
        private int pageNumber = 0;
        private float y = MARGIN;

        PageCursor(PdfDocument document) {
            this.document = document;
            startPage();
        }

        void startPage() {
            if (page != null) {
                document.finishPage(page);
            }
            pageNumber++;
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            y = MARGIN;
        }

        void ensureLineSpace(float extraHeight) {
            if (y + extraHeight > PAGE_HEIGHT - MARGIN) {
                startPage();
            }
        }

        void ensureBlockSpace(float extraHeight) {
            ensureLineSpace(extraHeight);
        }

        void drawDivider(Paint paint) {
            ensureLineSpace(20);
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint);
            y += 18;
        }

        void drawWrappedLine(String text, Paint paint, float spacingAfter, boolean forceGap) {
            if (text == null) {
                return;
            }
            List<String> lines = wrap(text, paint);
            float lineHeight = paint.getTextSize() + 6f;
            ensureLineSpace((lines.size() * lineHeight) + spacingAfter);
            for (String line : lines) {
                canvas.drawText(line, MARGIN, y, paint);
                y += lineHeight;
            }
            if (forceGap) {
                y += spacingAfter;
            } else {
                y += Math.max(4, spacingAfter - lineHeight);
            }
        }

        void drawWrappedParagraph(String text, Paint paint, float spacingAfter) {
            drawWrappedLine(text, paint, spacingAfter, true);
        }

        void addGap(float gap) {
            y += gap;
        }

        private List<String> wrap(String text, Paint paint) {
            java.util.ArrayList<String> result = new java.util.ArrayList<>();
            String[] paragraphs = text.split("\n");
            for (String paragraph : paragraphs) {
                String[] words = paragraph.trim().split("\\s+");
                StringBuilder current = new StringBuilder();
                for (String word : words) {
                    if (word == null || word.trim().isEmpty()) {
                        continue;
                    }
                    String candidate = current.length() == 0 ? word : current + " " + word;
                    if (paint.measureText(candidate) <= CONTENT_WIDTH) {
                        current.setLength(0);
                        current.append(candidate);
                    } else {
                        if (current.length() > 0) {
                            result.add(current.toString());
                        }
                        current.setLength(0);
                        current.append(word);
                    }
                }
                if (current.length() > 0) {
                    result.add(current.toString());
                }
                if (paragraphs.length > 1 && result.isEmpty()) {
                    result.add("");
                }
            }
            if (result.isEmpty()) {
                result.add("");
            }
            return result;
        }
    }
}
