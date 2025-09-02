import React, { useState, useEffect } from 'react';
import { PlusCircle, Edit, Trash2, Save, X } from 'lucide-react';

const NotesApp = () => {
  // State management for our notes application
  const [notes, setNotes] = useState([]); // Array to store all notes
  const [isCreating, setIsCreating] = useState(false); // Boolean to show/hide create form
  const [editingId, setEditingId] = useState(null); // ID of note currently being edited
  const [newNote, setNewNote] = useState({ title: '', content: '' }); // Data for new note being created
  const [editNote, setEditNote] = useState({ title: '', content: '' }); // Data for note being edited

  // API configuration - change this based on your backend choice
  const API_BASE = 'http://localhost:8000'; // For FastAPI backend
  // const API_BASE = 'http://localhost:8080'; // Uncomment this for Spring Boot backend

  // useEffect hook runs when component mounts to fetch initial data
  useEffect(() => {
    fetchNotes(); // Load all notes when app starts
  }, []);

  // Function to get all notes from the backend
  const fetchNotes = async () => {
     try {
      
      // const mockNotes = [
      //   { id: 1, title: 'Welcome Note', content: 'This is your first note! Click edit to modify it.' },
      //   { id: 2, title: 'Shopping List', content: 'Milk, Bread, Eggs, Coffee' },
      // ];
      // setNotes(mockNotes);
      
       REAL API CALL - Uncomment these lines when your backend is ready:
       const response = await fetch(`${API_BASE}/notes`);
       if (!response.ok) throw new Error('Failed to fetch notes');
       const data = await response.json();
       setNotes(data);
    } catch (error) {
      console.error('Error fetching notes:', error);
      // You might want to show an error message to the user here
    }
  };

  // Function to create a new note
  const createNote = async () => {
    // Validation: Don't create empty notes
    if (!newNote.title.trim() || !newNote.content.trim()) {
      alert('Please fill in both title and content');
      return;
    }

    // try {
      
    //   const mockNewNote = {
    //     id: Date.now(), // Using timestamp as temporary ID
    //     title: newNote.title,
    //     content: newNote.content,
    //     created_at: new Date().toISOString(),
    //     updated_at: new Date().toISOString()
    //   };
    //   setNotes([...notes, mockNewNote]); // Add new note to existing notes array
      
      // REAL API CALL 
       const response = await fetch(`${API_BASE}/notes`, {
         method: 'POST',
         headers: { 'Content-Type': 'application/json' },
         body: JSON.stringify(newNote) // Send note data as JSON
       });
       if (!response.ok) throw
         new Error('Failed to create note');
       const createdNote = await response.json();
       setNotes([...notes, createdNote]); // Add the newly created note to our list
      
      // Reset form and close create mode
      setNewNote({ title: '', content: '' });
      setIsCreating(false);
    } catch (error) {
      console.error('Error creating note:', error);
      alert('Failed to create note. Please try again.');
    }
  };

  // Function to update an existing note
  const updateNote = async (id) => {
    // Validation: Don't save empty notes
    if (!editNote.title.trim() || !editNote.content.trim()) {
      alert('Please fill in both title and content');
      return;
    }

    //try {
      // MOCK UPDATEd
      // setNotes(notes.map(note => 
      //   note.id === id 
      //     ? { ...note, title: editNote.title, content: editNote.content, updated_at: new Date().toISOString() }
      //     : note
      // ));
      
      // REAL API CALL -:
       const response = await fetch(`${API_BASE}/notes/${id}`, {
         method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
         body: JSON.stringify(editNote) // Send updated note data
       });
       if (!response.ok) throw 
         new Error('Failed to update note');
       const updatedNote = await response.json();
       setNotes(notes.map(note => note.id === id ? updatedNote : note));
      
      // Exit edit mode and clear edit form
      setEditingId(null);
      setEditNote({ title: '', content: '' });
    } catch (error) {
      console.error('Error updating note:', error);
      alert('Failed to update note. Please try again.');
    }
  };

  // Function to delete a note
  const deleteNote = async (id) => {
    // Ask for confirmation before deleting (good UX practice)
    if (!confirm('Are you sure you want to delete this note?')) return;

    try {
      // MOCK DELETION - Remove this section when connecting to real backend
      setNotes(notes.filter(note => note.id !== id)); // Remove note from array
      
      // REAL API CALL - Uncomment these lines when your backend is ready:
      // const response = await fetch(`${API_BASE}/notes/${id}`, { 
      //   method: 'DELETE' 
      // });
      // if (!response.ok) throw new Error('Failed to delete note');
      // setNotes(notes.filter(note => note.id !== id)); // Remove from local state
    } catch (error) {
      console.error('Error deleting note:', error);
      alert('Failed to delete note. Please try again.');
    }
  };

  // Helper function to start editing a note
  const startEditing = (note) => {
    setEditingId(note.id); // Set which note is being edited
    setEditNote({ title: note.title, content: note.content }); // Pre-fill form with current data
  };

  // Helper function to cancel editing
  const cancelEditing = () => {
    setEditingId(null); // Exit edit mode
    setEditNote({ title: '', content: '' }); // Clear edit form
  };

  // Helper function to cancel creating a new note
  const cancelCreating = () => {
    setIsCreating(false); // Hide create form
    setNewNote({ title: '', content: '' }); // Clear create form
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-6">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">My Notes</h1>
          <p className="text-gray-600">Organize your thoughts and ideas</p>
        </div>

        {/* Create Note Button */}
        {!isCreating && (
          <div className="mb-6">
            <button
              onClick={() => setIsCreating(true)}
              className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              <PlusCircle size={20} />
              Add New Note
            </button>
          </div>
        )}

        {/* Create Note Form */}
        {isCreating && (
          <div className="bg-white rounded-lg shadow-md p-6 mb-6 border-l-4 border-blue-500">
            <h3 className="text-lg font-semibold mb-4 text-gray-800">Create New Note</h3>
            <div className="space-y-4">
              <input
                type="text"
                placeholder="Note title..."
                value={newNote.title}
                onChange={(e) => setNewNote({ ...newNote, title: e.target.value })}
                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              <textarea
                placeholder="Write your note content here..."
                value={newNote.content}
                onChange={(e) => setNewNote({ ...newNote, content: e.target.value })}
                rows={4}
                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
              />
              <div className="flex gap-3">
                <button
                  onClick={createNote}
                  className="flex items-center gap-2 bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors"
                >
                  <Save size={16} />
                  Save Note
                </button>
                <button
                  onClick={cancelCreating}
                  className="flex items-center gap-2 bg-gray-500 text-white px-4 py-2 rounded-lg hover:bg-gray-600 transition-colors"
                >
                  <X size={16} />
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Notes Grid */}
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {notes.map((note) => (
            <div key={note.id} className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
              {editingId === note.id ? (
                // Edit Mode
                <div className="space-y-4">
                  <input
                    type="text"
                    value={editNote.title}
                    onChange={(e) => setEditNote({ ...editNote, title: e.target.value })}
                    className="w-full p-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500 focus:border-transparent font-medium"
                  />
                  <textarea
                    value={editNote.content}
                    onChange={(e) => setEditNote({ ...editNote, content: e.target.value })}
                    rows={4}
                    className="w-full p-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                  />
                  <div className="flex gap-2">
                    <button
                      onClick={() => updateNote(note.id)}
                      className="flex items-center gap-1 bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700 transition-colors"
                    >
                      <Save size={14} />
                      Save
                    </button>
                    <button
                      onClick={cancelEditing}
                      className="flex items-center gap-1 bg-gray-500 text-white px-3 py-1 rounded text-sm hover:bg-gray-600 transition-colors"
                    >
                      <X size={14} />
                      Cancel
                    </button>
                  </div>
                </div>
              ) : (
                // View Mode
                <div>
                  <h3 className="text-lg font-semibold text-gray-800 mb-3">{note.title}</h3>
                  <p className="text-gray-600 mb-4 whitespace-pre-wrap">{note.content}</p>
                  <div className="flex gap-2">
                    <button
                      onClick={() => startEditing(note)}
                      className="flex items-center gap-1 bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700 transition-colors"
                    >
                      <Edit size={14} />
                      Edit
                    </button>
                    <button
                      onClick={() => deleteNote(note.id)}
                      className="flex items-center gap-1 bg-red-600 text-white px-3 py-1 rounded text-sm hover:bg-red-700 transition-colors"
                    >
                      <Trash2 size={14} />
                      Delete
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>

        {notes.length === 0 && !isCreating && (
          <div className="text-center py-12">
            <div className="text-gray-400 mb-4">
              <PlusCircle size={64} className="mx-auto" />
            </div>
            <h3 className="text-xl font-semibold text-gray-600 mb-2">No notes yet</h3>
            <p className="text-gray-500">Create your first note to get started!</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default NotesApp;
