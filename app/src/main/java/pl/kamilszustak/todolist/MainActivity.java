package pl.kamilszustak.todolist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements CompletedTasksRecyclerAdapter.CompletedTasksListener, TasksRecyclerAdapter.TasksListener {

    private FloatingActionButton fab;
    private ImageButton deleteCompletedTasksButton;
    private View bottomSheetDimView;
    private View noCompletedTasksLinearLayout;

    private RecyclerView tasksRecyclerView;
    private RecyclerView completedTasksRecyclerView;
    private TasksRecyclerAdapter tasksRecyclerAdapter;
    private CompletedTasksRecyclerAdapter completedTasksRecyclerAdapter;
    private BottomSheetBehavior bottomSheetBehavior;
    private boolean isBottomSheetOpened = false;
    private ArrayList<Task> tasks;
    private ArrayList<Task> completedTasks;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        findViews();
        setupButtonsListeners();
        initializeArrayLists();
        initializeCompletedTasksRecyclerView(completedTasksRecyclerView, completedTasks);
        initializeTasksRecyclerView(tasksRecyclerView, tasks);
        bottomSheetBehavior = initializeBottomSheetAndGetBehaviour();

        Log.i("todo", "start tasks size: " + tasks.size());
        Log.i("todo", "start completedTasks size: " + completedTasks.size());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_completed_tasks: {
                completedTasksRecyclerView.scrollToPosition(0);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("bottom_sheet_state", bottomSheetBehavior.getState());
        savedInstanceState.putInt("bottom_sheet_dim_view_visibility", bottomSheetDimView.getVisibility());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        bottomSheetBehavior.setState(savedInstanceState.getInt("bottom_sheet_state"));
        bottomSheetDimView.setVisibility(savedInstanceState.getInt("bottom_sheet_dim_view_visibility"));
    }

    @Override
    public void onBackPressed() {
        if(isBottomSheetOpened)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        else
            super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveArrayListsToSharedPreferences(new ArrayList[] {tasks, completedTasks}, sharedPreferences, new String[] {getString(R.string.shared_preferences_tasks_key), getString(R.string.shared_preferences_completed_tasks_key)});
    }

    private void initializeArrayLists() {
        tasks = loadArrayListFromSharedPreferences(sharedPreferences, getString(R.string.shared_preferences_tasks_key));
        completedTasks = loadArrayListFromSharedPreferences(sharedPreferences, getString(R.string.shared_preferences_completed_tasks_key));
        if(tasks == null)
            tasks = new ArrayList<>();
        if(completedTasks == null)
            completedTasks = new ArrayList<>();
    }

    private void findViews() {
        fab = findViewById(R.id.fab);
        deleteCompletedTasksButton = findViewById(R.id.delete_completed_tasks_image_button);
        tasksRecyclerView = findViewById(R.id.todo_recycler_view);
        completedTasksRecyclerView = findViewById(R.id.completed_tasks_recycler_view);
        bottomSheetDimView = findViewById(R.id.bottom_sheet_dim_view);
        noCompletedTasksLinearLayout = findViewById(R.id.no_completed_tasks_linear_layout);
    }

    private void setupButtonsListeners() {
        fab.setOnClickListener(view -> {
            showAddTaskAlertDialog(MainActivity.this);
        });

        deleteCompletedTasksButton.setOnClickListener(v -> {
            int itemCount = completedTasks.size();
            if(itemCount > 0) {
                completedTasks.clear();
                completedTasks.removeAll(completedTasks);
                completedTasksRecyclerAdapter.notifyItemRangeRemoved(0, itemCount);
                noCompletedTasksLinearLayout.setVisibility(View.VISIBLE);
                Toast.makeText(this, getString(R.string.deleted_completed_tasks), Toast.LENGTH_SHORT)
                        .show();
            }
            else
                Toast.makeText(this, getString(R.string.no_tasks_to_delete), Toast.LENGTH_SHORT)
                        .show();
        });

        bottomSheetDimView.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
    }


    private void showAddTaskAlertDialog(Context context) {
        final View customDialogView = LayoutInflater.from(context).inflate(R.layout.add_task_alert_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setPositiveButton(getString(R.string.add), (dialog, which) -> {
                    EditText newTaskEditText = customDialogView.findViewById(R.id.new_task_edit_text);
                    if (!newTaskEditText.getText().toString().isEmpty())
                        addNewTaskAndUpdateList(new Task(State.TODO, newTaskEditText.getText().toString(), new Date()));
                    else
                        Toast.makeText(getApplicationContext(), getString(R.string.empty_task), Toast.LENGTH_SHORT)
                            .show();
                })
        .setTitle(getString(R.string.new_task));

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);
        alertDialog.setView(customDialogView);
        alertDialog.show();
    }

    private void initializeTasksRecyclerView(RecyclerView recyclerView, ArrayList<Task> tasks) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(tasksRecyclerAdapter = new TasksRecyclerAdapter(tasks, new WeakReference<>(this)));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void initializeCompletedTasksRecyclerView(RecyclerView recyclerView, ArrayList<Task> completedTasks) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(completedTasksRecyclerAdapter = new CompletedTasksRecyclerAdapter(completedTasks, new WeakReference<>(this)));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private BottomSheetBehavior initializeBottomSheetAndGetBehaviour() {
        MaterialCardView bottomSheetCardView = findViewById(R.id.bottom_sheet_card_view);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheetCardView);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        behavior.setSkipCollapsed(true);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int state) {
                noCompletedTasksLinearLayout.setVisibility(completedTasks.isEmpty() ? View.VISIBLE : View.GONE);
                isBottomSheetOpened = (state != BottomSheetBehavior.STATE_HIDDEN);
                bottomSheetDimView.setVisibility(isBottomSheetOpened ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onSlide(@NonNull View view, float offset) {
                bottomSheetDimView.setAlpha(offset+1F);
            }
        });

        return behavior;
    }

    private void addNewTaskAndUpdateList(Task task) {
        tasks.add(0, task);
        tasksRecyclerAdapter.notifyItemInserted(0);
        tasksRecyclerView.scrollToPosition(0);
    }

    private void saveArrayListsToSharedPreferences(ArrayList[] arrayLists, SharedPreferences sharedPreferences, String[] sharedPreferencesItemKeys) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            for (int i = 0; i < arrayLists.length; i++) {
                JSONArray jsonArray = new JSONArray(new Gson().toJson(arrayLists[i]));
                editor.putString(sharedPreferencesItemKeys[i], jsonArray.toString());
            }
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ArrayList<Task> loadArrayListFromSharedPreferences(SharedPreferences sharedPreferences, String sharedPreferencesItemKey) {
        ArrayList<Task> arrayList = null;
        try {
            arrayList = new Gson().fromJson(sharedPreferences.getString(sharedPreferencesItemKey, ""), new TypeToken<ArrayList<Task>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    @Override
    public void restoreCompletedTask(Task task) {
        tasks.add(0, task);
        tasksRecyclerAdapter.notifyItemInserted(0);
        if (completedTasks.size() == 0)
            noCompletedTasksLinearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void completeTask(Task task) {
        completedTasks.add(0, task);
        completedTasksRecyclerAdapter.notifyItemInserted(0);
    }
}