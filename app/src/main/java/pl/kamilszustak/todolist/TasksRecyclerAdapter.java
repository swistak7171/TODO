package pl.kamilszustak.todolist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TasksRecyclerAdapter extends RecyclerView.Adapter<TasksRecyclerAdapter.ViewHolder> {

    private ArrayList<Task> tasks;
    private WeakReference<Context> contextWeakReference;
    private TasksListener tasksListener;

    public TasksRecyclerAdapter(ArrayList<Task> tasks, WeakReference<Context> contextWeakReference) {
        this.tasks = tasks;
        this.contextWeakReference = contextWeakReference;
        tasksListener = (TasksListener) contextWeakReference.get();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_tasks_list_item, parent, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        viewHolder.taskStateImageButton.setImageDrawable(viewHolder.taskStateImageButton.getResources().getDrawable(R.drawable.todo_icon));
        viewHolder.taskDescriptionTextView.setPaintFlags(Paint.LINEAR_TEXT_FLAG);
        viewHolder.taskDescriptionTextView.setText(tasks.get(viewHolder.getAdapterPosition()).getDescription());

        viewHolder.taskStateImageButton.setOnClickListener(view -> {
            final int adapterPosition = viewHolder.getAdapterPosition();

            ((ImageButton)view).setImageDrawable(view.getResources().getDrawable(R.drawable.done_icon));
            viewHolder.taskDescriptionTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            Task completedTask = tasks.get(adapterPosition);
            completedTask.setCompleteDate(new Date());
            tasks.remove(adapterPosition);
            notifyItemRemoved(adapterPosition);
            notifyItemRangeChanged(adapterPosition, tasks.size());
            tasksListener.completeTask(completedTask);
        });


        viewHolder.taskDescriptionTextView.setOnClickListener(view -> {
            final int adapterPosition = viewHolder.getAdapterPosition();

            final View customDialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.add_task_alert_dialog, null);
            final EditText newTaskEditText = customDialogView.findViewById(R.id.new_task_edit_text);
            newTaskEditText.setText(tasks.get(adapterPosition).getDescription());

            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                    .setPositiveButton(view.getResources().getString(R.string.modify), (dialog, which) -> {
                        if (!newTaskEditText.getText().toString().isEmpty()) {
                            tasks.get(adapterPosition).setDescription(newTaskEditText.getText().toString());
                            notifyItemChanged(adapterPosition);
                        }
                        else
                            Toast.makeText(view.getContext(), view.getResources().getString(R.string.empty_task), Toast.LENGTH_SHORT)
                                    .show();
                    })
                    .setTitle(view.getResources().getString(R.string.modify_task));

            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);
            alertDialog.setView(customDialogView);
            alertDialog.show();
        });



        viewHolder.taskDescriptionTextView.setOnTouchListener(new View.OnTouchListener() {
            final int adapterPosition = viewHolder.getAdapterPosition();
            boolean isDialogDisplayed = false;
            AlertDialog alertDialog;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final View customDialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.preview_task_dialog, null);
                final TextView previewTaskDescriptionTextView = customDialogView.findViewById(R.id.preview_task_description_text_view);
                final TextView previewTaskCreateDateTextView = customDialogView.findViewById(R.id.preview_task_create_date_text_view);

                if ((event.getAction() == MotionEvent.ACTION_DOWN) && !isDialogDisplayed) {
                    isDialogDisplayed = true;
                    previewTaskDescriptionTextView.setText(tasks.get(adapterPosition).getDescription());
                    String createDate = tasks.get(adapterPosition).getCreateDate().getDate() + " " + tasks.get(adapterPosition).getCreateDate().getMonth() + " " + tasks.get(adapterPosition).getCreateDate().getYear();
                    previewTaskCreateDateTextView.setText(view.getResources().getString(R.string.created) + " " + new SimpleDateFormat("dd/MM/yyyy").format(tasks.get(adapterPosition).getCreateDate()));

                    alertDialog = new AlertDialog.Builder(view.getContext()).create();
                    alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);
                    alertDialog.setView(customDialogView);
                    alertDialog.show();

                    //Log.i("tag", "down " + String.valueOf(isDialogDisplayed));
                } else if ((event.getAction() == MotionEvent.ACTION_UP) && isDialogDisplayed) {
                    alertDialog.dismiss();
                    isDialogDisplayed = false;
                    //Log.i("tag", "up " + String.valueOf(isDialogDisplayed));

                } else if ((event.getAction() == MotionEvent.ACTION_CANCEL) && isDialogDisplayed) {
                    alertDialog.dismiss();
                    isDialogDisplayed = false;
                    //Log.i("tag", "move " + String.valueOf(isDialogDisplayed));
                }

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if(tasks == null)
            return 0;
        return tasks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageButton taskStateImageButton;
        private TextView taskDescriptionTextView;
        //private ImageButton dragTaskImageButton;

        public ViewHolder(View view) {
            super(view);
            taskStateImageButton = view.findViewById(R.id.task_state_image_button);
            taskDescriptionTextView = view.findViewById(R.id.task_description_text_view);
            //dragTaskImageButton = view.findViewById(R.id.drag_task_image_button);
        }
    }

    public interface TasksListener {
        void completeTask(Task task);
    }
}
