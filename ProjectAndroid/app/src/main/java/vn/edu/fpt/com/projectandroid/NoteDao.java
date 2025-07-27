package vn.edu.fpt.com.projectandroid;

import androidx.room.*;
import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Query("UPDATE notes SET isCompleted = :isCompleted WHERE id = :noteId")
    void updateNoteStatus(int noteId, boolean isCompleted);

    @Delete
    void delete(Note note);

    @Query("DELETE FROM notes WHERE id = :noteId")
    void deleteNote(int noteId);

    @Query("SELECT * FROM notes ORDER BY date DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :id")
    Note getNoteById(int id);

    // Search and filter queries can be added later
} 