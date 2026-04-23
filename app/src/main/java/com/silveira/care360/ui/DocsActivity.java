package com.silveira.care360.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silveira.care360.R;
import com.silveira.care360.ui.adapter.ActividadAdapter;
import com.silveira.care360.ui.docs.ActividadViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DocsActivity extends AppCompatActivity {

    private ActividadViewModel viewModel;
    private RecyclerView rvActividad;
    private TextView txtEmpty;
    private ActividadAdapter actividadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docs);

        viewModel = new ViewModelProvider(this).get(ActividadViewModel.class);

        initDynamicViews();
        BottomNavManager.bind(this, BottomNavManager.Tab.DOCS);
        observeViewModel();
        viewModel.loadActividad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadActividad();
        }
    }

    private void initDynamicViews() {
        TextView title = findViewById(R.id.txtDocsHeaderTitle);
        if (title != null) {
            title.setText(R.string.activity_title);
        }

        txtEmpty = findViewById(R.id.txtDocsPlaceholder);
        if (txtEmpty != null) {
            txtEmpty.setText(R.string.activity_empty);
        }

        View contentRoot = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        if (!(contentRoot instanceof ConstraintLayout)) {
            throw new IllegalStateException("La pantalla de actividad necesita un ConstraintLayout como raiz");
        }
        ConstraintLayout root = (ConstraintLayout) contentRoot;

        rvActividad = new RecyclerView(this);
        rvActividad.setId(View.generateViewId());
        rvActividad.setLayoutManager(new LinearLayoutManager(this));
        rvActividad.setClipToPadding(false);
        rvActividad.setPadding(dp(16), dp(16), dp(16), dp(16));
        actividadAdapter = new ActividadAdapter();
        rvActividad.setAdapter(actividadAdapter);
        root.addView(rvActividad);

        ConstraintSet set = new ConstraintSet();
        set.clone(root);
        set.connect(rvActividad.getId(), ConstraintSet.TOP, R.id.layoutHeaderDocs, ConstraintSet.BOTTOM, dp(4));
        set.connect(rvActividad.getId(), ConstraintSet.BOTTOM, R.id.bottomNavDocs, ConstraintSet.TOP, dp(8));
        set.connect(rvActividad.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
        set.connect(rvActividad.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
        set.constrainHeight(rvActividad.getId(), 0);
        set.constrainWidth(rvActividad.getId(), 0);
        set.applyTo(root);
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> {
            if (state == null) return;
            actividadAdapter.submitList(state.activities);
            boolean hasItems = state.activities != null && !state.activities.isEmpty();
            if (txtEmpty != null) {
                txtEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
            }
            if (rvActividad != null) {
                rvActividad.setVisibility(hasItems ? View.VISIBLE : View.GONE);
            }
            if (state.errorMessage != null && !state.errorMessage.trim().isEmpty()) {
                showUiMessage(state.errorMessage);
            }
        });

        viewModel.action.observe(this, action -> {
            if (action == null) return;
            if (action instanceof ActividadViewModel.ShowMessageAction) {
                showUiMessage(((ActividadViewModel.ShowMessageAction) action).message);
            }
            viewModel.onActionHandled();
        });
    }

    private void showUiMessage(String message) {
        if (message != null && !message.trim().isEmpty()) {
            UiMessageUtils.show(this, message);
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}
