# main.py - FastAPI Backend for Notes App
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import sqlite3
import os
from datetime import datetime

# Create FastAPI application instance
app = FastAPI(title="Notes API", description="A simple CRUD API for notes management")

# Configure CORS to allow frontend to communicate with backend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://127.0.0.1:3000"],  # React dev server URLs
    allow_credentials=True,
    allow_methods=["*"],  # Allow all HTTP methods (GET, POST, PUT, DELETE)
    allow_headers=["*"],  # Allow all headers
)

# Database configuration
DB_NAME = "notes.db"  # SQLite database file name

# Function to initialize the database and create tables
def init_db():
    """Create the notes table if it doesn't exist"""
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()
    
    # Create notes table 
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS notes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,  -- Auto-incrementing primary key
            title TEXT NOT NULL,                   -- Note title (required)
            content TEXT NOT NULL,                 -- Note content (required)
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- When note was created
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP   -- When note was last updated
        )
    ''')
    conn.commit()
    conn.close()

# Pydantic models for request/response validation

class NoteCreate(BaseModel):
    """Model for creating a new note - only title and content required"""
    title: str
    content: str

class NoteUpdate(BaseModel):
    """Model for updating a note - all fields are optional"""
    title: Optional[str] = None
    content: Optional[str] = None

class Note(BaseModel):
    """Model for a complete note with all fields"""
    id: int
    title: str
    content: str
    created_at: str
    updated_at: str

# Initialize database when the application starts
@app.on_event("startup")
async def startup_event():
    """This runs once when the server starts up"""
    init_db()  # Create database tables if they don't exist

# Helper function to get database connection with proper row formatting
def get_db_connection():
    """Returns a database connection with row_factory for easier data access"""
    conn = sqlite3.connect(DB_NAME)
    conn.row_factory = sqlite3.Row  # This allows us to access columns by name
    return conn

# API Endpoints - These handle HTTP requests from the frontend

@app.get("/")
async def root():
    """Health check endpoint to verify the API is running"""
    return {"message": "Notes API is running!", "status": "healthy"}

@app.get("/notes", response_model=List[Note])
async def get_notes():
    """
    GET /notes - Retrieve all notes
    Returns: List of all notes ordered by most recently updated
    """
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # Get all notes, ordered by most recent updates first
    cursor.execute("SELECT * FROM notes ORDER BY updated_at DESC")
    notes = cursor.fetchall()
    conn.close()
    
    # Convert database rows to dictionaries for JSON response
    return [dict(note) for note in notes]

@app.get("/notes/{note_id}", response_model=Note)
async def get_note(note_id: int):
    """
    GET /notes/{id} - Retrieve a specific note by ID
    Args: note_id (int) - The ID of the note to retrieve
    Returns: Single note object
    Raises: 404 if note not found
    """
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM notes WHERE id = ?", (note_id,))
    note = cursor.fetchone()
    conn.close()
    
    if note is None:
        raise HTTPException(status_code=404, detail="Note not found")
    
    return dict(note)

@app.post("/notes", response_model=Note)
async def create_note(note: NoteCreate):
    """
    POST /notes - Create a new note
    Args: note (NoteCreate) - Note data with title and content
    Returns: Newly created note with generated ID and timestamps
    """
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # Generate current timestamp for created_at and updated_at
    now = datetime.now().isoformat()
    
    # Insert new note into database
    cursor.execute(
        "INSERT INTO notes (title, content, created_at, updated_at) VALUES (?, ?, ?, ?)",
        (note.title, note.content, now, now)
    )
    note_id = cursor.lastrowid  # Get the ID of the newly created note
    conn.commit()
    
    # Fetch the complete note data to return to client
    cursor.execute("SELECT * FROM notes WHERE id = ?", (note_id,))
    created_note = cursor.fetchone()
    conn.close()
    
    return dict(created_note)

@app.put("/notes/{note_id}", response_model=Note)
async def update_note(note_id: int, note_update: NoteUpdate):
    """
    PUT /notes/{id} - Update an existing note
    Args: 
        note_id (int) - ID of the note to update
        note_update (NoteUpdate) - Updated note data (partial updates allowed)
    Returns: Updated note object
    Raises: 404 if note not found
    """
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # First, check if the note exists
    cursor.execute("SELECT * FROM notes WHERE id = ?", (note_id,))
    existing_note = cursor.fetchone()
    
    if existing_note is None:
        conn.close()
        raise HTTPException(status_code=404, detail="Note not found")
    
    # Update only the fields that were provided (partial update support)
    title = note_update.title if note_update.title is not None else existing_note['title']
    content = note_update.content if note_update.content is not None else existing_note['content']
    updated_at = datetime.now().isoformat()
    
    # Perform the update
    cursor.execute(
        "UPDATE notes SET title = ?, content = ?, updated_at = ? WHERE id = ?",
        (title, content, updated_at, note_id)
    )
    conn.commit()
    
    # Fetch and return the updated note
    cursor.execute("SELECT * FROM notes WHERE id = ?", (note_id,))
    updated_note = cursor.fetchone()
    conn.close()
    
    return dict(updated_note)

@app.delete("/notes/{note_id}")
async def delete_note(note_id: int):
    """
    DELETE /notes/{id} - Delete a note
    Args: note_id (int) - ID of the note to delete
    Returns: Success message
    Raises: 404 if note not found
    """
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # Check if note exists before trying to delete
    cursor.execute("SELECT * FROM notes WHERE id = ?", (note_id,))
    note = cursor.fetchone()
    
    if note is None:
        conn.close()
        raise HTTPException(status_code=404, detail="Note not found")
    
    # Delete the note
    cursor.execute("DELETE FROM notes WHERE id = ?", (note_id,))
    conn.commit()
    conn.close()
    
    return {"message": "Note deleted successfully"}

# Main entry point for running the application
if __name__ == "__main__":
    import uvicorn
    print("Starting Notes API server...")
    print("API Documentation available at: http://localhost:8000/docs")
    uvicorn.run(app, host="0.0.0.0", port=8000)


# requirements.txt
"""
fastapi==0.104.1
uvicorn==0.24.0
pydantic==2.5.0
"""

# To run the FastAPI backend:
# 1. pip install fastapi uvicorn
# 2. python main.py
