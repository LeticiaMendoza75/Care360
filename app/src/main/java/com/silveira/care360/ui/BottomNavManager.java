package com.silveira.care360.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.silveira.care360.R;
import com.silveira.care360.ui.medicacion.MedicacionActivity;

public final class BottomNavManager {

    public enum Tab {
        HOME,
        MEDICACION,
        CITAS,
        DOCS,
        FAMILIA
    }

    private BottomNavManager() {
    }

    public static void bind(Activity activity, Tab selectedTab) {
        if (activity == null) {
            return;
        }

        View navInicio = activity.findViewById(R.id.navInicio);
        View navMedicacion = activity.findViewById(R.id.navMedicacion);
        View navCitas = activity.findViewById(R.id.navCitas);
        View navDocumentos = activity.findViewById(R.id.navDocumentos);
        View navFamilia = activity.findViewById(R.id.navFamilia);

        if (navInicio != null) {
            navInicio.setOnClickListener(v -> {
                if (selectedTab == Tab.HOME) {
                    return;
                }
                openTopLevel(activity, HomeActivity.class);
            });
            navInicio.setAlpha(selectedTab == Tab.HOME ? 1f : 0.72f);
        }

        if (navMedicacion != null) {
            navMedicacion.setOnClickListener(v -> {
                if (selectedTab == Tab.MEDICACION) {
                    return;
                }
                openTopLevel(activity, MedicacionActivity.class);
            });
            navMedicacion.setAlpha(selectedTab == Tab.MEDICACION ? 1f : 0.72f);
        }

        if (navCitas != null) {
            navCitas.setOnClickListener(v -> {
                if (selectedTab == Tab.CITAS) {
                    return;
                }
                openTopLevel(activity, CitasActivity.class);
            });
            navCitas.setAlpha(selectedTab == Tab.CITAS ? 1f : 0.72f);
        }

        if (navDocumentos != null) {
            navDocumentos.setOnClickListener(v -> {
                if (selectedTab == Tab.DOCS) {
                    return;
                }
                openTopLevel(activity, DocsActivity.class);
            });
            navDocumentos.setAlpha(selectedTab == Tab.DOCS ? 1f : 0.72f);
        }

        if (navFamilia != null) {
            navFamilia.setOnClickListener(v -> {
                if (selectedTab == Tab.FAMILIA) {
                    return;
                }
                openTopLevel(activity, FamiliaActivity.class);
            });
            navFamilia.setAlpha(selectedTab == Tab.FAMILIA ? 1f : 0.72f);
        }
    }

    private static void openTopLevel(Activity activity, Class<?> target) {
        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }
}
