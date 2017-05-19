package br.com.lucasbaiao.minharotina.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import br.com.lucasbaiao.minharotina.R;
import br.com.lucasbaiao.minharotina.persistence.AppDatabaseHelper;
import br.com.lucasbaiao.minharotina.services.ExportDataService;
import br.com.lucasbaiao.minharotina.persistence.model.Category;
import br.com.lucasbaiao.minharotina.persistence.model.Event;
import br.com.lucasbaiao.minharotina.view.adapter.CategoryAdapter;
import br.com.lucasbaiao.minharotina.view.widget.OffsetDecoration;

public class MainActivity extends BaseActivity
        implements CategoryRegisterDialog.CategoryDialogListener {

    private CategoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setupButton();
        setupGridView((RecyclerView) findViewById(R.id.recyclerView));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reset) {
            AppDatabaseHelper.reset(this);
            setupGridView((RecyclerView) findViewById(R.id.recyclerView));
            return true;
        }
        else if (id == R.id.action_export) {
            startService(new Intent(this, ExportDataService.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupGridView(final RecyclerView recyclerView) {
        final int spacing = getResources().getDimensionPixelSize(R.dimen.spacing_nano);
        recyclerView.addItemDecoration(new OffsetDecoration(spacing));
        recyclerView.setHasFixedSize(true);
        mAdapter = new CategoryAdapter(this);
        mAdapter.setOnItemClickListener(
                new CategoryAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(View v, int position, boolean isCounting) {
                        if (isCounting) {
                            stopTimer(mAdapter.getItem(position));
                        } else {
                            startTimer(mAdapter.getItem(position));
                        }
                    }
                });
        recyclerView.setAdapter(mAdapter);
        recyclerView.getViewTreeObserver()
                .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        supportStartPostponedEnterTransition();
                        return true;
                    }
                });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx,int dy){
                super.onScrolled(recyclerView, dx, dy);

                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                if (dy >0) {
                    // Scroll Down
                    if (fab.isShown()) {
                        fab.hide();
                    }
                }
                else if (dy <0) {
                    // Scroll Up
                    if (!fab.isShown()) {
                        fab.show();
                    }
                }
            }
        });
    }

    private void stopTimer(Category item) {
        long timeMillis = System.currentTimeMillis();
        Event event = item.getEvents().valueAt(item.getEvents().size() - 1);
        event.setStop("" + timeMillis);
        AppDatabaseHelper.updateCategory(this, item);
        mAdapter.notifyItemChanged(item.getId());
    }

    private void startTimer(final Category item) {
        long timeMillis = System.currentTimeMillis();
        item.addEvent(new Event(""+timeMillis, null));
        AppDatabaseHelper.updateCategory(this, item);
        mAdapter.notifyItemChanged(item.getId());
    }

    private void setupButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewCategoryDialog();
            }
        });
    }

    public void showNewCategoryDialog() {
        DialogFragment dialog = new CategoryRegisterDialog();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(View dialog) {
        EditText editText = (EditText) dialog.findViewById(R.id.name);
        Category category = new Category(editText.getText().toString());
        AppDatabaseHelper.createNewCategory(getApplicationContext(), category);
        mAdapter.notifyDataChanged();
    }

    @Override
    public void onDialogNegativeClick(View dialog) {
    }
}
