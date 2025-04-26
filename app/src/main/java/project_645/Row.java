package project_645;

import java.io.Serializable;

public class Row implements Serializable {
    static final int MOVIE_ID_SIZE = 9;
    static final int TITLE_SIZE = 30;
    static final int PERSON_ID_SIZE = 10;
    static final int CATEGORY_SIZE = 20;
    static final int NAME_SIZE = 105;

    // Fields for Movies, WorkedOn, and People tables
    public byte[] movieId = new byte[MOVIE_ID_SIZE];
    public byte[] title = new byte[TITLE_SIZE];
    public byte[] personId = new byte[PERSON_ID_SIZE];
    public byte[] category = new byte[CATEGORY_SIZE];
    public byte[] name = new byte[NAME_SIZE];

    // Single constructor that accepts all possible fields
    public Row(byte[] movieId, byte[] title, byte[] personId, byte[] category, byte[] name) {
        if (movieId != null) System.arraycopy(movieId, 0, this.movieId, 0, Math.min(this.movieId.length, movieId.length));
        if (title != null) System.arraycopy(title, 0, this.title, 0, Math.min(this.title.length, title.length));
        if (personId != null) System.arraycopy(personId, 0, this.personId, 0, Math.min(this.personId.length, personId.length));
        if (category != null) System.arraycopy(category, 0, this.category, 0, Math.min(this.category.length, category.length));
        if (name != null) System.arraycopy(name, 0, this.name, 0, Math.min(this.name.length, name.length));
    }

    // Getter methods for each field
    public byte[] getMovieId() {
        return movieId;
    }

    public byte[] getTitle() {
        return title;
    }

    public byte[] getPersonId() {
        return personId;
    }

    public byte[] getCategory() {
        return category;
    }

    public byte[] getName() {
        return name;
    }

    // Helper methods to convert byte arrays to Strings
    public String getMovieIdString() {
        return new String(movieId).trim();
    }

    public String getTitleString() {
        return new String(title).trim();
    }

    public String getPersonIdString() {
        return new String(personId).trim();
    }

    public String getCategoryString() {
        return new String(category).trim();
    }

    public String getNameString() {
        return new String(name).trim();
    }

    // Setter methods for each table-specific field
    public void setMovieId(byte[] movieId) {
        if (movieId != null) System.arraycopy(movieId, 0, this.movieId, 0, Math.min(this.movieId.length, movieId.length));
    }

    public void setTitle(byte[] title) {
        if (title != null) System.arraycopy(title, 0, this.title, 0, Math.min(this.title.length, title.length));
    }

    public void setPersonId(byte[] personId) {
        if (personId != null) System.arraycopy(personId, 0, this.personId, 0, Math.min(this.personId.length, personId.length));
    }

    public void setCategory(byte[] category) {
        if (category != null) System.arraycopy(category, 0, this.category, 0, Math.min(this.category.length, category.length));
    }

    public void setName(byte[] name) {
        if (name != null) System.arraycopy(name, 0, this.name, 0, Math.min(this.name.length, name.length));
    }
}
