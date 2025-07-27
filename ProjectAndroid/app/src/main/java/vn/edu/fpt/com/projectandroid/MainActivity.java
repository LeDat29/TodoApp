package vn.edu.fpt.com.projectandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;
import java.util.ArrayList;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.app.AlertDialog;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private NoteDatabase noteDatabase;
    private MaterialToolbar toolbar;
    private TextInputEditText searchView;
    private ChipGroup chipGroup;
    private View emptyState;
    private String currentSearchKeyword = "";
    private String currentFilter = "All";
    private SharedPreferences preferences;
    private static final String PREF_NAME = "NoteAppPrefs";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before super.onCreate
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean(KEY_DARK_MODE, false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        searchView = findViewById(R.id.searchView);
        chipGroup = findViewById(R.id.chipGroup);
        emptyState = findViewById(R.id.emptyState);

        // Setup toolbar
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("📝 My Notes");
        }

        // Initialize database
        noteDatabase = Room.databaseBuilder(getApplicationContext(),
                NoteDatabase.class, "note_db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(this);
        recyclerView.setAdapter(adapter);

        // Setup swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new NoteAdapter.SwipeToDeleteCallback(adapter, noteDatabase));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Setup FAB with animation
        if (fab != null) {
            fab.setOnClickListener(v -> {
                // Add scale animation
                fab.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100)
                    .withEndAction(() -> {
                        fab.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                        startActivity(intent);
                    }).start();
            });
        }

        // Setup search
        setupSearch();

        // Setup chip group
        setupChipGroup();

        // Load notes
        loadNotes();
    }

    private void setupSearch() {
        if (searchView != null) {
            searchView.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchKeyword = s.toString();
                    applyFilters();
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void setupChipGroup() {
        if (chipGroup != null) {
            Log.d("MainActivity", "Setting up chip group");
            
            // Đảm bảo chip "All" được chọn mặc định
            chipGroup.check(R.id.chipAll);
            
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                Log.d("MainActivity", "Chip selection changed. Checked IDs: " + checkedIds.size());
                
                if (checkedIds.isEmpty()) {
                    currentFilter = "All";
                    Log.d("MainActivity", "No chip selected, setting filter to 'All'");
                } else {
                    int checkedId = checkedIds.get(0);
                    Log.d("MainActivity", "Checked ID: " + checkedId);
                    
                    // Map ID to text
                    String filterText = "";
                    if (checkedId == R.id.chipAll) {
                        filterText = "All";
                    } else if (checkedId == R.id.chipWork) {
                        filterText = "Work";
                    } else if (checkedId == R.id.chipPersonal) {
                        filterText = "Personal";
                    } else if (checkedId == R.id.chipImportant) {
                        filterText = "Important";
                    } else {
                        filterText = "All";
                    }
                    
                    currentFilter = filterText;
                    Log.d("MainActivity", "Chip selected: '" + currentFilter + "'");
                }
                
                Log.d("MainActivity", "Applying filters after chip selection");
                applyFilters();
            });
            Log.d("MainActivity", "Chip group setup completed");
        } else {
            Log.e("MainActivity", "ChipGroup is null");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        try {
            List<Note> notes = noteDatabase.noteDao().getAllNotes();
            adapter.setNotes(notes);
            adapter.notifyDataSetChanged();
            updateEmptyState();
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading notes: " + e.getMessage(), e);
        }
    }

    private void applyFilters() {
        try {
            Log.d("MainActivity", "=== APPLYING FILTERS ===");
            Log.d("MainActivity", "Search keyword: '" + currentSearchKeyword + "'");
            Log.d("MainActivity", "Filter tag: '" + currentFilter + "'");
            
            List<Note> allNotes = noteDatabase.noteDao().getAllNotes();
            if (allNotes == null) {
                allNotes = new ArrayList<>();
            }

            Log.d("MainActivity", "Total notes before filter: " + allNotes.size());

            List<Note> filtered = new ArrayList<>();
            for (Note note : allNotes) {
                if (note == null) continue;

                boolean match = true;
                String reason = "";

                // Apply tag filter
                if (!currentFilter.equals("All")) {
                    if (note.tag == null || note.tag.trim().isEmpty()) {
                        match = false;
                        reason = "Note has no tag";
                    } else if (!note.tag.trim().equalsIgnoreCase(currentFilter.trim())) {
                        match = false;
                        reason = "Tag mismatch: note tag='" + note.tag + "', filter='" + currentFilter + "'";
                    }
                }

                // Apply search filter
                if (match && !currentSearchKeyword.isEmpty()) {
                    String keyword = currentSearchKeyword.toLowerCase();
                    boolean searchMatch = (note.title != null && note.title.toLowerCase().contains(keyword))
                            || (note.content != null && note.content.toLowerCase().contains(keyword))
                            || (note.tag != null && note.tag.toLowerCase().contains(keyword));
                    
                    if (!searchMatch) {
                        match = false;
                        reason = "Search keyword not found";
                    }
                }

                if (match) {
                    filtered.add(note);
                }
            }

            Log.d("MainActivity", "Filtered notes count: " + filtered.size());
            adapter.setNotes(filtered);
            adapter.notifyDataSetChanged();
            updateEmptyState();

        } catch (Exception e) {
            Log.e("MainActivity", "Error applying filters: " + e.getMessage(), e);
        }
    }

    private void updateEmptyState() {
        if (emptyState != null && recyclerView != null) {
            if (adapter.getItemCount() == 0) {
                emptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note_menu, menu);
        
        // Update dark mode icon based on current theme
        MenuItem darkModeItem = menu.findItem(R.id.action_dark_mode);
        if (darkModeItem != null) {
            boolean isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
            darkModeItem.setIcon(isDarkMode ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
            darkModeItem.setTitle(isDarkMode ? "Light Mode" : "Dark Mode");
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_note) {
            Intent intent = new Intent(this, AddNoteActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_dark_mode) {
            toggleDarkMode();
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            showShareOptions();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleDarkMode() {
        boolean isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        
        // Toggle theme
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            preferences.edit().putBoolean(KEY_DARK_MODE, false).apply();
            Toast.makeText(this, "🌞 Light Mode Enabled", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            preferences.edit().putBoolean(KEY_DARK_MODE, true).apply();
            Toast.makeText(this, "🌙 Dark Mode Enabled", Toast.LENGTH_SHORT).show();
        }
        
        // Recreate activity for smooth transition
        recreate();
    }

    private void showShareOptions() {
        String[] options = {"Share All Notes", "Share Selected Notes", "Share as Text", "Share as HTML"};
        
        new AlertDialog.Builder(this)
            .setTitle("📤 Share Options")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        shareAllNotes();
                        break;
                    case 1:
                        shareSelectedNotes();
                        break;
                    case 2:
                        shareAsText();
                        break;
                    case 3:
                        shareAsHTML();
                        break;
                }
            })
            .show();
    }

    private void shareAllNotes() {
        try {
            List<Note> allNotes = noteDatabase.noteDao().getAllNotes();
            if (allNotes == null || allNotes.isEmpty()) {
                Toast.makeText(this, "No notes to share", Toast.LENGTH_SHORT).show();
                return;
            }
            
            StringBuilder shareText = new StringBuilder();
            shareText.append("📚 MY NOTES COLLECTION\n");
            shareText.append("Total Notes: ").append(allNotes.size()).append("\n\n");
            
            for (int i = 0; i < allNotes.size(); i++) {
                Note note = allNotes.get(i);
                shareText.append("📝 ").append(i + 1).append(". ").append(note.title).append("\n");
                shareText.append("📄 ").append(note.content).append("\n");
                shareText.append("🏷️ ").append(note.tag).append(" | 🎨 ").append(note.color).append("\n");
                shareText.append("📅 ").append(note.date).append("\n");
                if (note.reminderDate != null && !note.reminderDate.isEmpty()) {
                    shareText.append("⏰ Reminder: ").append(note.reminderDate).append("\n");
                }
                shareText.append("\n---\n\n");
            }
            
            shareText.append("Shared from My Notes App");
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Notes Collection");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
            
            startActivity(Intent.createChooser(shareIntent, "Share All Notes"));
            
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing notes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void shareSelectedNotes() {
        // For now, share filtered notes
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, "No notes to share", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get current filtered notes
        List<Note> filteredNotes = adapter.getCurrentNotes();
        if (filteredNotes == null || filteredNotes.isEmpty()) {
            Toast.makeText(this, "No notes to share", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder shareText = new StringBuilder();
        shareText.append("📚 FILTERED NOTES\n");
        if (!currentSearchKeyword.isEmpty()) {
            shareText.append("Search: '").append(currentSearchKeyword).append("'\n");
        }
        if (!currentFilter.equals("All")) {
            shareText.append("Filter: ").append(currentFilter).append("\n");
        }
        shareText.append("Total: ").append(filteredNotes.size()).append(" notes\n\n");
        
        for (int i = 0; i < filteredNotes.size(); i++) {
            Note note = filteredNotes.get(i);
            shareText.append("📝 ").append(i + 1).append(". ").append(note.title).append("\n");
            shareText.append("📄 ").append(note.content).append("\n");
            shareText.append("🏷️ ").append(note.tag).append(" | 🎨 ").append(note.color).append("\n");
            shareText.append("📅 ").append(note.date).append("\n");
            if (note.reminderDate != null && !note.reminderDate.isEmpty()) {
                shareText.append("⏰ Reminder: ").append(note.reminderDate).append("\n");
            }
            shareText.append("\n---\n\n");
        }
        
        shareText.append("Shared from My Notes App");
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Filtered Notes");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        
        startActivity(Intent.createChooser(shareIntent, "Share Selected Notes"));
    }

    private void shareAsText() {
        shareAllNotes(); // Same as share all notes for now
    }

    private void shareAsHTML() {
        try {
            List<Note> allNotes = noteDatabase.noteDao().getAllNotes();
            if (allNotes == null || allNotes.isEmpty()) {
                Toast.makeText(this, "No notes to share", Toast.LENGTH_SHORT).show();
                return;
            }
            
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html><html><head><title>My Notes</title>");
            htmlContent.append("<style>body{font-family:Arial,sans-serif;margin:20px;}");
            htmlContent.append(".note{border:1px solid #ddd;margin:10px 0;padding:15px;border-radius:8px;}");
            htmlContent.append(".title{font-size:18px;font-weight:bold;color:#333;}");
            htmlContent.append(".content{margin:10px 0;color:#666;}");
            htmlContent.append(".meta{font-size:12px;color:#999;}");
            htmlContent.append("</style></head><body>");
            htmlContent.append("<h1>📚 My Notes Collection</h1>");
            htmlContent.append("<p>Total Notes: ").append(allNotes.size()).append("</p>");
            
            for (Note note : allNotes) {
                htmlContent.append("<div class='note'>");
                htmlContent.append("<div class='title'>📝 ").append(note.title).append("</div>");
                htmlContent.append("<div class='content'>📄 ").append(note.content).append("</div>");
                htmlContent.append("<div class='meta'>🏷️ ").append(note.tag).append(" | 🎨 ").append(note.color).append(" | 📅 ").append(note.date).append("</div>");
                if (note.reminderDate != null && !note.reminderDate.isEmpty()) {
                    htmlContent.append("<div class='meta'>⏰ Reminder: ").append(note.reminderDate).append("</div>");
                }
                htmlContent.append("</div>");
            }
            
            htmlContent.append("<hr><p><em>Shared from My Notes App</em></p></body></html>");
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/html");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Notes Collection");
            shareIntent.putExtra(Intent.EXTRA_TEXT, htmlContent.toString());
            
            startActivity(Intent.createChooser(shareIntent, "Share as HTML"));
            
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing HTML: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}