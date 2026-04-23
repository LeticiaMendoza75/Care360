package com.silveira.care360.ui;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.silveira.care360.R;

public class HeaderManager {

    public interface HeaderActions {
        void onLogout();

        default boolean shouldShowChangeGroup() {
            return false;
        }

        default void onChangeGroup() {
        }
    }

    /**
     * Configura el header de forma robusta para asegurar visibilidad y clics.
     */
    public static void configurar(Activity activity, String userEmail, HeaderActions actions) {
        View headerView = activity.findViewById(R.id.headerUsuario);
        if (headerView == null) {
            return;
        }

        headerView.bringToFront();

        TextView txtConectadoComo = headerView.findViewById(R.id.txtConectadoComo);
        TextView txtCambiarGrupo = headerView.findViewById(R.id.txtCambiarGrupo);
        TextView txtCerrarSesion = headerView.findViewById(R.id.txtCerrarSesion);

        if (txtConectadoComo != null) {
            String texto = (userEmail != null && !userEmail.isEmpty()) ? userEmail : "Sesion activa";
            txtConectadoComo.setText(activity.getString(R.string.header_connected_as, texto));
            txtConectadoComo.setVisibility(View.VISIBLE);
        }

        if (txtCambiarGrupo != null) {
            boolean showChangeGroup = actions != null && actions.shouldShowChangeGroup();
            txtCambiarGrupo.setText(R.string.header_change_group);
            txtCambiarGrupo.setVisibility(showChangeGroup ? View.VISIBLE : View.GONE);
            if (showChangeGroup) {
                txtCambiarGrupo.setOnClickListener(v -> actions.onChangeGroup());
            } else {
                txtCambiarGrupo.setOnClickListener(null);
            }
        }

        if (txtCerrarSesion != null && actions != null) {
            txtCerrarSesion.setOnClickListener(v -> {
                Log.d("HeaderManager", "Evento Logout disparado");
                actions.onLogout();
            });
        }
    }
}
