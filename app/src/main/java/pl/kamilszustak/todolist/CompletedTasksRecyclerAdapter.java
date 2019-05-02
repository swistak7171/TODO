package pl.kamilszustak.todolist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CompletedTasksRecyclerAdapter extends RecyclerView.Adapter<CompletedTasksRecyclerAdapter.ViewHolder> {

    private ArrayList<Task> completedTasks;
    private CompletedTasksListener completedTasksListener;


    public CompletedTasksRecyclerAdapter(ArrayList<Task> completedTasks, WeakReference<Context> contextWeakReference) {
        this.completedTasks = completedTasks;
        completedTasksListener = (CompletedTasksListener) contextWeakReference.get();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_completed_tasks_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.taskStateImageButton.setImageDrawable(viewHolder.taskDescriptionTextView.getResources().getDrawable(R.drawable.done_icon));
        viewHolder.taskDescriptionTextView.setText(completedTasks.get(viewHolder.getAdapterPosition()).getDescription());

        viewHolder.taskStateImageButton.setOnClickListener(view -> {
            final int adapterPosition = viewHolder.getAdapterPosition();

            Task restoredTask = completedTasks.get(adapterPosition);
            completedTasks.remove(adapterPosition);
            notifyItemRemoved(adapterPosition);
            notifyItemRangeChanged(adapterPosition, completedTasks.size());
            completedTasksListener.restoreCompletedTask(restoredTask);
        });
    }

    @Override
    public int getItemCount() {
        if(completedTasks == null)
            return 0;
        return completedTasks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageButton taskStateImageButton;
        private TextView taskDescriptionTextView;

        public ViewHolder(View view) {
            super(view);
            taskStateImageButton = view.findViewById(R.id.task_state_image_button);
            taskDescriptionTextView = view.findViewById(R.id.task_description_text_view);
        }
    }

    public interface CompletedTasksListener {
        void restoreCompletedTask(Task task);
    }
}
