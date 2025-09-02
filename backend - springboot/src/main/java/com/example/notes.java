// NotesApplication.java - Main Spring Boot Application Class
package com.example.notes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Notes App
 * @SpringBootApplication annotation enables auto-configuration, component scanning, and configuration
 */
@SpringBootApplication
public class NotesApplication {
    /**
     * Main method - entry point of the Spring Boot application
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(NotesApplication.class, args);
        System.out.println("Notes API is running on http://localhost:8080");
        System.out.println("H2 Database Console: http://localhost:8080/h2-console");
    }
}

// Note.java - JPA Entity Class
package com.example.notes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Note entity class representing a note in the database
 * This class is mapped to the 'notes' table
 */
@Entity // Marks this class as a JPA entity
@Table(name = "notes") // Specifies the table name in the database
public class Note {
    
    // Primary key field with auto-generation
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;
    
    // Note title - cannot be null
    @Column(nullable = false)
    private String title;
    
    // Note content - using TEXT type for longer content
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    // Timestamp when the note was created
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Timestamp when the note was last updated
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Automatically set timestamps before persisting new entity
     * This method runs before INSERT operations
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Automatically update timestamp before updating entity
     * This method runs before UPDATE operations
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    
    /**
     * Default constructor (required by JPA)
     */
    public Note() {}
    
    /**
     * Constructor for creating a new note
     * @param title The title of the note
     * @param content The content of the note
     */
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    // Getter and Setter methods for all fields
    // These are required for JPA and JSON serialization/deserialization
    
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getTitle() { 
        return title; 
    }
    
    public void setTitle(String title) { 
        this.title = title; 
    }
    
    public String getContent() { 
        return content; 
    }
    
    public void setContent(String content) { 
        this.content = content; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
    
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; 
    }
}

// NoteRepository.java - Data Access Layer
package com.example.notes.repository;

import com.example.notes.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Note entity
 * Spring Data JPA automatically implements this interface with CRUD operations
 * JpaRepository<Note, Long> provides:
 * - save(), findById(), findAll(), deleteById(), etc.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    /**
     * Custom query method to find all notes ordered by update time (newest first)
     * Spring Data JPA automatically generates the implementation based on method name
     * @return List of notes sorted by updatedAt in descending order
     */
    List<Note> findAllByOrderByUpdatedAtDesc();
}

// NoteController.java - REST API Controller
package com.example.notes.controller;

import com.example.notes.model.Note;
import com.example.notes.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for handling HTTP requests related to notes
 * @RestController combines @Controller and @ResponseBody
 * @RequestMapping sets the base path for all endpoints in this controller
 * @CrossOrigin allows frontend running on localhost:3000 to access this API
 */
@RestController
@RequestMapping("/notes")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class NoteController {
    
    // Dependency injection - Spring automatically provides NoteRepository instance
    @Autowired
    private NoteRepository noteRepository;
    
    /**
     * GET /notes - Retrieve all notes
     * @return List of all notes ordered by most recently updated
     */
    @GetMapping
    public List<Note> getAllNotes() {
        System.out.println("Fetching all notes...");
        return noteRepository.findAllByOrderByUpdatedAtDesc();
    }
    
    /**
     * GET /notes/{id} - Retrieve a specific note by ID
     * @param id The ID of the note to retrieve
     * @return ResponseEntity with note data or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        System.out.println("Fetching note with ID: " + id);
        Optional<Note> note = noteRepository.findById(id);
        
        // Return note if found, otherwise return 404 Not Found
        return note.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * POST /notes - Create a new note
     * @param noteRequest The note data from request body
     * @return The created note with generated ID and timestamps
     */
    @PostMapping
    public Note createNote(@RequestBody NoteRequest noteRequest) {
        System.out.println("Creating new note: " + noteRequest.getTitle());
        
        // Create new Note entity from request data
        Note note = new Note(noteRequest.getTitle(), noteRequest.getContent());
        
        // Save to database (ID and timestamps are automatically generated)
        return noteRepository.save(note);
    }
    
    /**
     * PUT /notes/{id} - Update an existing note
     * @param id The ID of the note to update
     * @param noteRequest The updated note data
     * @return ResponseEntity with updated note or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody NoteRequest noteRequest) {
        System.out.println("Updating note with ID: " + id);
        
        Optional<Note> optionalNote = noteRepository.findById(id);
        
        if (optionalNote.isPresent()) {
            Note note = optionalNote.get();
            
            if (noteRequest.getTitle() != null && !noteRequest.getTitle().trim().isEmpty()) {
                note.setTitle(noteRequest.getTitle());
            }
            if (noteRequest.getContent() != null && !noteRequest.getContent().trim().isEmpty()) {
                note.setContent(noteRequest.getContent());
            }
            Note updatedNote = noteRepository.save(note);
            return ResponseEntity.ok(updatedNote);
        } else {
            System.out.println("Note with ID " + id + " not found");
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * DELETE /notes/{id} - Delete a note
     * @param id The ID of the note to delete
     * @return ResponseEntity with success status or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        System.out.println("Deleting note with ID: " + id);
        
        if (noteRepository.existsById(id)) {
            noteRepository.deleteById(id);
            System.out.println("Note deleted successfully");
            return ResponseEntity.ok().build();
        } else {
            System.out.println("Note with ID " + id + " not found");
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Inner class for handling JSON request bodies
     * This represents the data structure expected from the frontend
     */
    public static class NoteRequest {
        private String title;   // Note title from frontend
        private String content; // Note content from frontend
        
        // Default constructor (required for JSON deserialization)
        public NoteRequest() {}
        
        // Constructor with parameters
        public NoteRequest(String title, String content) {
            this.title = title;
            this.content = content;
        }
        
        // Getters and setters for JSON serialization/deserialization
        public String getTitle() { 
            return title; 
        }
        
        public void setTitle(String title) { 
            this.title = title; 
        }
        
        public String getContent() { 
            return content; 
        }
        
        public void setContent(String content) { 
            this.content = content; 
        }
    }
}

// application.properties
"""
# Database configuration (H2 in-memory for development)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# H2 Console (for development)
spring.h2.console.enabled=true

# Server configuration
server.port=8080
"""

// pom.xml
"""
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>notes</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>notes</name>
    <description>Simple Notes CRUD API</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
"""

// To run the Spring Boot backend:
// 1. Ensure you have Java 17+ and Maven installed
// 2. Create a new Spring Boot project with the above structure
// 3. mvn spring-boot:run

