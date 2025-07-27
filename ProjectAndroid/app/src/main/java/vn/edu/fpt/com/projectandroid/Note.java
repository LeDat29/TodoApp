package vn.edu.fpt.com.projectandroid;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String title = "";
    public String content = "";
    public String date = ""; // yyyy-MM-dd HH:mm
    public String tag = "Personal";
    public String color = "Red";
    public boolean isCompleted = false;
    public String imagePath = ""; // Đường dẫn ảnh đính kèm
    public String reminderTime = ""; // Thời gian nhắc nhở (timestamp hoặc yyyy-MM-dd HH:mm)
    public String reminderDate = ""; // Ngày nhắc nhở hiển thị (yyyy-MM-dd HH:mm)

    // Default constructor
    public Note() {
        this.title = "";
        this.content = "";
        this.date = "";
        this.tag = "Personal";
        this.color = "Red";
        this.isCompleted = false;
        this.imagePath = "";
        this.reminderTime = "";
        this.reminderDate = "";
    }

    // Constructor with parameters
    public Note(String title, String content, String tag, String color) {
        this.title = title != null ? title : "";
        this.content = content != null ? content : "";
        this.tag = tag != null ? tag : "Personal";
        this.color = color != null ? color : "Red";
        this.isCompleted = false;
        this.imagePath = "";
        this.reminderTime = "";
        this.reminderDate = "";
    }
} 