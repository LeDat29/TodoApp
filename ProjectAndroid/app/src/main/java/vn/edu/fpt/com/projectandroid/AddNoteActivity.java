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
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Arrays;
import android.util.Log;
import android.os.Build;

public class AddNoteActivity extends AppCompatActivity {
    private TextInputEditText etTitle, etContent;
    private AutoCompleteTextView spinnerTag, spinnerColor, spinnerNotifyType;
    private MaterialButton btnSave, btnPickDate;
    private TextView tvReminder;
    private MaterialToolbar toolbar;
    private NoteDatabase noteDatabase;
    private String reminderTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        try {
            // Initialize views
            etTitle = findViewById(R.id.etTitle);
            etContent = findViewById(R.id.etContent);
            spinnerTag = findViewById(R.id.spinnerTag);
            spinnerColor = findViewById(R.id.spinnerColor);
            spinnerNotifyType = findViewById(R.id.spinnerNotifyType);
            btnSave = findViewById(R.id.btnSave);
            btnPickDate = findViewById(R.id.btnPickDate);
            tvReminder = findViewById(R.id.tvReminder);
            toolbar = findViewById(R.id.toolbar);

            // Setup toolbar
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                    getSupportActionBar().setTitle("📝 My Notes");
                }
            }

            // Initialize database
            noteDatabase = Room.databaseBuilder(getApplicationContext(),
                    NoteDatabase.class, "note_db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration() // This will recreate the database if migration fails
                    .build();

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

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                spinnerTag.setText(tags[0], false); // Set default value
            }

            // Setup Color adapter
            if (spinnerColor != null) {
                String[] colors = getResources().getStringArray(R.array.colors);
                ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, colors);
                spinnerColor.setAdapter(colorAdapter);
                spinnerColor.setText(colors[0], false); // Set default value
            }

            // Setup Notify Type adapter
            if (spinnerNotifyType != null) {
                String[] notifyTypes = getResources().getStringArray(R.array.notify_types);
                ArrayAdapter<String> notifyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, notifyTypes);
                spinnerNotifyType.setAdapter(notifyAdapter);
                spinnerNotifyType.setText(notifyTypes[0], false); // Set default value
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
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(System.currentTimeMillis());

            Note note = new Note(title, content, tag, color);
            note.date = date;
            note.imagePath = ""; // Không còn hỗ trợ ảnh
            note.reminderTime = reminderTime != null ? reminderTime : "";
            note.reminderDate = reminderTime != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Long.parseLong(reminderTime)) : "";

            if (noteDatabase != null && noteDatabase.noteDao() != null) {
                noteDatabase.noteDao().insert(note);
                
                if (reminderTime != null) {
                    setReminder(title, Long.parseLong(reminderTime), notifyType);
                }
                
                Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: Database not initialized", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving note: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void setReminder(String title, long timeInMillis, String notifyType) {
        try {
            Log.d("AddNoteActivity", "Setting reminder for: " + title + " at: " + timeInMillis);
            
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
                
                Log.d("AddNoteActivity", "Alarm set successfully for: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(timeInMillis));
                Toast.makeText(this, "Reminder set for: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(timeInMillis), Toast.LENGTH_SHORT).show();
            } else {
                Log.e("AddNoteActivity", "AlarmManager is null");
                Toast.makeText(this, "Error: Could not set reminder", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("AddNoteActivity", "Error setting reminder: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        String title = etTitle != null ? etTitle.getText().toString().trim() : "";
        String content = etContent != null ? etContent.getText().toString().trim() : "";

        if (!title.isEmpty() || !content.isEmpty()) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Discard", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Keep Editing", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
} 