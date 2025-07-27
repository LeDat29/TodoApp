package vn.edu.fpt.com.projectandroid;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Arrays;
import android.util.Log;
import android.os.Build;
import android.view.Menu;

public class EditNoteActivity extends AppCompatActivity {
    private TextInputEditText etTitle, etContent;
    private AutoCompleteTextView spinnerTag, spinnerColor, spinnerNotifyType;
    private MaterialButton btnSave, btnPickDate, btnDelete;
    private TextView tvReminder;
    private MaterialToolbar toolbar;
    private NoteDatabase noteDatabase;
    private String reminderTime = null;
    private Note currentNote;
    private int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        // Get note ID from intent
        noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId == -1) {
            Toast.makeText(this, "Error: Note not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            // Initialize views
            etTitle = findViewById(R.id.etTitle);
            etContent = findViewById(R.id.etContent);
            spinnerTag = findViewById(R.id.spinnerTag);
            spinnerColor = findViewById(R.id.spinnerColor);
            spinnerNotifyType = findViewById(R.id.spinnerNotifyType);
            btnSave = findViewById(R.id.btnSave);
            btnPickDate = findViewById(R.id.btnPickDate);
            btnDelete = findViewById(R.id.btnDelete);
            tvReminder = findViewById(R.id.tvReminder);
            toolbar = findViewById(R.id.toolbar);

            // Setup toolbar
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                    getSupportActionBar().setTitle("Edit Note");
                }
            }

            // Initialize database
            noteDatabase = Room.databaseBuilder(getApplicationContext(),
                    NoteDatabase.class, "note_db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();

            // Load note data
            loadNoteData();

            // Setup date picker
            if (btnPickDate != null) {
                btnPickDate.setOnClickListener(v -> {
                    try {
                        Calendar calendar = Calendar.getInstance();
                        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                reminderTime = String.valueOf(calendar.getTimeInMillis());
                                if (tvReminder != null) {
                                    tvReminder.setText("Reminder: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.getTime()));
                                }
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error setting date: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }

            // Setup AutoCompleteTextView adapters
            setupAutoCompleteAdapters();

            // Setup save button
            if (btnSave != null) {
                btnSave.setOnClickListener(v -> saveNote());
            }

            // Setup delete button
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> deleteNote());
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note_menu, menu);
        return true;
    }

    private void loadNoteData() {
        try {
            currentNote = noteDatabase.noteDao().getNoteById(noteId);
            if (currentNote == null) {
                Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Fill form with existing data
            if (etTitle != null) {
                etTitle.setText(currentNote.title);
            }
            if (etContent != null) {
                etContent.setText(currentNote.content);
            }
            if (spinnerTag != null) {
                spinnerTag.setText(currentNote.tag, false);
            }
            if (spinnerColor != null) {
                spinnerColor.setText(currentNote.color, false);
            }
            if (spinnerNotifyType != null) {
                spinnerNotifyType.setText("Default", false); // Default for existing notes
            }

            // Set reminder if exists
            if (currentNote.reminderTime != null && !currentNote.reminderTime.isEmpty()) {
                reminderTime = currentNote.reminderTime;
                if (tvReminder != null && currentNote.reminderDate != null) {
                    tvReminder.setText("Reminder: " + currentNote.reminderDate);
                }
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setupAutoCompleteAdapters() {
        try {
            // Setup Tag adapter
            if (spinnerTag != null) {
                String[] tags = getResources().getStringArray(R.array.tags);
                ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tags);
                spinnerTag.setAdapter(tagAdapter);
            }

            // Setup Color adapter
            if (spinnerColor != null) {
                String[] colors = getResources().getStringArray(R.array.colors);
                ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, colors);
                spinnerColor.setAdapter(colorAdapter);
            }

            // Setup Notify Type adapter
            if (spinnerNotifyType != null) {
                String[] notifyTypes = getResources().getStringArray(R.array.notify_types);
                ArrayAdapter<String> notifyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, notifyTypes);
                spinnerNotifyType.setAdapter(notifyAdapter);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up dropdowns: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void saveNote() {
        try {
            if (etTitle == null || etContent == null) {
                Toast.makeText(this, "Error: Views not initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            
            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                return;
            }
            
            if (content.isEmpty()) {
                etContent.setError("Content is required");
                return;
            }
            
            String tag = (spinnerTag != null && spinnerTag.getText() != null && !spinnerTag.getText().toString().trim().isEmpty())
                ? spinnerTag.getText().toString().trim()
                : "Personal";
            String color = (spinnerColor != null && spinnerColor.getText() != null && !spinnerColor.getText().toString().trim().isEmpty())
                ? spinnerColor.getText().toString().trim()
                : "Red";
            String notifyType = (spinnerNotifyType != null && spinnerNotifyType.getText() != null && !spinnerNotifyType.getText().toString().trim().isEmpty())
                ? spinnerNotifyType.getText().toString().trim()
                : "Default";

            // Update note
            currentNote.title = title;
            currentNote.content = content;
            currentNote.tag = tag;
            currentNote.color = color;
            currentNote.reminderTime = reminderTime != null ? reminderTime : "";
            currentNote.reminderDate = reminderTime != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Long.parseLong(reminderTime)) : "";

            if (noteDatabase != null && noteDatabase.noteDao() != null) {
                noteDatabase.noteDao().update(currentNote);
                
                // Update reminder if changed
                if (reminderTime != null && !reminderTime.equals(currentNote.reminderTime)) {
                    setReminder(title, Long.parseLong(reminderTime), notifyType);
                }
                
                Toast.makeText(this, "Note updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: Database not initialized", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating note: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void deleteNote() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                try {
                    if (noteDatabase != null && noteDatabase.noteDao() != null) {
                        noteDatabase.noteDao().deleteNote(noteId);
                        Toast.makeText(this, "Note deleted successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error deleting note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void setReminder(String title, long timeInMillis, String notifyType) {
        try {
            Log.d("EditNoteActivity", "Setting reminder for: " + title + " at: " + timeInMillis);
            
            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("notifyType", notifyType);
            
            int requestCode = (int) (timeInMillis % Integer.MAX_VALUE);
            
            // Fix PendingIntent flags for different Android versions
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, flags);
            
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null) {
                // Use setExactAndAllowWhileIdle for better reliability
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }
                
                Log.d("EditNoteActivity", "Alarm set successfully for: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(timeInMillis));
                Toast.makeText(this, "Reminder set for: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(timeInMillis), Toast.LENGTH_SHORT).show();
            } else {
                Log.e("EditNoteActivity", "AlarmManager is null");
                Toast.makeText(this, "Error: Could not set reminder", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("EditNoteActivity", "Error setting reminder: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackPress();
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            shareNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!handleBackPress()) {
            super.onBackPressed();
        }
    }

    private boolean handleBackPress() {
        // Check if user has made changes
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        
        if (!title.equals(currentNote.title) || !content.equals(currentNote.content)) {
            // Show confirmation dialog
            new AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Discard", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Keep Editing", null)
                .show();
            return true; // handled
        }
        return false; // not handled, call super
    }

    private void shareNote() {
        if (currentNote == null) {
            Toast.makeText(this, "No note to share", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Format note content for sharing
            StringBuilder shareText = new StringBuilder();
            shareText.append("📝 ").append(currentNote.title).append("\n\n");
            shareText.append("📄 Content:\n").append(currentNote.content).append("\n\n");
            shareText.append("🏷️ Tag: ").append(currentNote.tag).append("\n");
            shareText.append("🎨 Color: ").append(currentNote.color).append("\n");
            shareText.append("📅 Created: ").append(currentNote.date).append("\n");
            
            if (currentNote.reminderDate != null && !currentNote.reminderDate.isEmpty()) {
                shareText.append("⏰ Reminder: ").append(currentNote.reminderDate).append("\n");
            }
            
            shareText.append("\n---\nShared from My Notes App");

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Note: " + currentNote.title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());

            // Start share activity
            startActivity(Intent.createChooser(shareIntent, "Share Note"));
            
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
} 