package vn.edu.fpt.com.projectandroid;

import android.content.Intent;
import android.view.*;
import android.widget.TextView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.List;
import java.util.ArrayList;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import android.view.View;
import android.content.Context;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> noteList;
    private OnNoteClickListener listener;
    private Context context;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteStatusChanged(Note note, boolean isCompleted);
        void onNoteDelete(Note note);
    }

    public NoteAdapter(Context context) {
        this.context = context;
        this.noteList = new ArrayList<>();
    }

    public NoteAdapter(Context context, List<Note> noteList) {
        this.context = context;
        this.noteList = noteList != null ? noteList : new ArrayList<>();
    }

    public void setNotes(List<Note> notes) {
        this.noteList = notes != null ? notes : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        try {
            if (noteList == null || position >= noteList.size()) {
                return;
            }

            Note note = noteList.get(position);
            if (note == null) {
                return;
            }
            
            // Set text content
            if (holder.tvTitle != null) {
                holder.tvTitle.setText(note.title != null ? note.title : "");
            }
            if (holder.tvContent != null) {
                holder.tvContent.setText(note.content != null ? note.content : "");
            }
            if (holder.tvDate != null) {
                holder.tvDate.setText(note.date != null ? note.date : "");
            }
            if (holder.chipTag != null) {
                holder.chipTag.setText(note.tag != null ? note.tag : "");
            }
            
            // Set checkbox state
            if (holder.checkboxDone != null) {
                holder.checkboxDone.setChecked(note.isCompleted);
            }
            
            // Set color indicator based on note color
            setColorIndicator(holder, note.color);
            
            // Set tag chip color based on tag
            setTagChipColor(holder, note.tag);
            
            // Show/hide reminder indicator
            if (holder.reminderIndicator != null && holder.tvReminder != null) {
                if (note.reminderDate != null && !note.reminderDate.isEmpty()) {
                    holder.reminderIndicator.setVisibility(View.VISIBLE);
                    holder.tvReminder.setText("⏰ " + note.reminderDate);
                } else {
                    holder.reminderIndicator.setVisibility(View.GONE);
                }
            }
            
            // Set click listeners
            if (holder.itemView != null) {
                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onNoteClick(note);
                    } else {
                        // Default behavior: open EditNoteActivity
                        Intent intent = new Intent(context, EditNoteActivity.class);
                        intent.putExtra("note_id", note.id);
                        context.startActivity(intent);
                    }
                });
            }
            
            if (holder.checkboxDone != null) {
                holder.checkboxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (listener != null) {
                        listener.onNoteStatusChanged(note, isChecked);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setColorIndicator(NoteViewHolder holder, String color) {
        try {
            if (holder.colorIndicator == null || color == null) {
                return;
            }

            int colorRes;
            switch (color.toLowerCase()) {
                case "red":
                    colorRes = Color.parseColor("#FF6B6B");
                    break;
                case "blue":
                    colorRes = Color.parseColor("#4ECDC4");
                    break;
                case "green":
                    colorRes = Color.parseColor("#4CAF50");
                    break;
                case "yellow":
                    colorRes = Color.parseColor("#FFC107");
                    break;
                case "purple":
                    colorRes = Color.parseColor("#9C27B0");
                    break;
                case "orange":
                    colorRes = Color.parseColor("#FF9800");
                    break;
                default:
                    colorRes = Color.parseColor("#FF6B6B");
                    break;
            }
            holder.colorIndicator.setBackgroundColor(colorRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTagChipColor(NoteViewHolder holder, String tag) {
        try {
            if (holder.chipTag == null || tag == null) {
                return;
            }

            boolean isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
            int backgroundColor;
            int textColor;
            
            if (isDarkMode) {
                // Dark mode colors
                switch (tag.toLowerCase()) {
                    case "work":
                        backgroundColor = Color.parseColor("#1E3A5F");
                        textColor = Color.parseColor("#64B5F6");
                        break;
                    case "personal":
                        backgroundColor = Color.parseColor("#4A2C4A");
                        textColor = Color.parseColor("#CE93D8");
                        break;
                    case "important":
                        backgroundColor = Color.parseColor("#4A2C2C");
                        textColor = Color.parseColor("#EF5350");
                        break;
                    default:
                        backgroundColor = Color.parseColor("#424242");
                        textColor = Color.parseColor("#BDBDBD");
                        break;
                }
            } else {
                // Light mode colors
                switch (tag.toLowerCase()) {
                    case "work":
                        backgroundColor = Color.parseColor("#E3F2FD");
                        textColor = Color.parseColor("#1976D2");
                        break;
                    case "personal":
                        backgroundColor = Color.parseColor("#F3E5F5");
                        textColor = Color.parseColor("#7B1FA2");
                        break;
                    case "important":
                        backgroundColor = Color.parseColor("#FFEBEE");
                        textColor = Color.parseColor("#D32F2F");
                        break;
                    default:
                        backgroundColor = Color.parseColor("#F5F5F5");
                        textColor = Color.parseColor("#757575");
                        break;
                }
            }
            
            holder.chipTag.setChipBackgroundColorResource(android.R.color.transparent);
            holder.chipTag.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(backgroundColor));
            holder.chipTag.setTextColor(textColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return noteList != null ? noteList.size() : 0;
    }

    public List<Note> getCurrentNotes() {
        return noteList != null ? new ArrayList<>(noteList) : new ArrayList<>();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate, tvReminder;
        MaterialCheckBox checkboxDone;
        View colorIndicator;
        Chip chipTag;
        LinearLayout reminderIndicator;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvReminder = itemView.findViewById(R.id.tvReminder);
            checkboxDone = itemView.findViewById(R.id.checkboxDone);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            chipTag = itemView.findViewById(R.id.chipTag);
            reminderIndicator = itemView.findViewById(R.id.reminderIndicator);
        }
    }

    public static class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private NoteAdapter adapter;
        private NoteDatabase noteDatabase;

        public SwipeToDeleteCallback(NoteAdapter adapter, NoteDatabase noteDatabase) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.adapter = adapter;
            this.noteDatabase = noteDatabase;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            List<Note> notes = adapter.getCurrentNotes();
            
            if (position >= 0 && position < notes.size()) {
                Note noteToDelete = notes.get(position);
                
                // Delete from database
                try {
                    noteDatabase.noteDao().deleteNote(noteToDelete.id);
                    
                    // Remove from adapter
                    notes.remove(position);
                    adapter.setNotes(notes);
                    
                    // Show feedback
                    android.widget.Toast.makeText(adapter.context, 
                        "🗑️ Note deleted: " + noteToDelete.title, 
                        android.widget.Toast.LENGTH_SHORT).show();
                        
                } catch (Exception e) {
                    e.printStackTrace();
                    android.widget.Toast.makeText(adapter.context, 
                        "Error deleting note", 
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                View itemView = viewHolder.itemView;
                
                // Create background
                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#F44336"));
                
                // Create delete icon
                Paint iconPaint = new Paint();
                iconPaint.setColor(Color.WHITE);
                iconPaint.setTextSize(50);
                iconPaint.setTextAlign(Paint.Align.CENTER);
                
                // Draw background
                if (dX > 0) { // Swipe right
                    c.drawRect(itemView.getLeft(), itemView.getTop(), dX, itemView.getBottom(), paint);
                    
                    // Draw delete icon
                    float iconX = itemView.getLeft() + (dX / 2);
                    float iconY = itemView.getTop() + (itemView.getHeight() / 2) + 15;
                    c.drawText("🗑️", iconX, iconY, iconPaint);
                } else if (dX < 0) { // Swipe left
                    c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);
                    
                    // Draw delete icon
                    float iconX = itemView.getRight() + (dX / 2);
                    float iconY = itemView.getTop() + (itemView.getHeight() / 2) + 15;
                    c.drawText("🗑️", iconX, iconY, iconPaint);
                }
            }
            
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
} 