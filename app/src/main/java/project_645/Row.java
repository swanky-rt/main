package project_645;
import java.io.Serializable;
import java.util.Date;

/**
 * Struct representing a database row, containing primary data types.
 */
public class Row implements Serializable {
    // Define primary data type fields, depending on the schema of the table
    // These fields are for the Movies table described below
    static final int MOVIE_ID_SIZE = 9;
    static final int TITLE_SIZE = 30;
    static final int PERSON_ID_SIZE = 10;
    static final int CATEGORY_SIZE = 20;
    static final int NAME_SIZE = 105;
    public byte[] movieId = new byte[9];
    public byte[] title = new byte[30];
    public byte[] personId = new byte[10];
    public byte[] category = new byte[20];
    public byte[] name = new byte[105];

    public Row(byte[] movieId, byte[] title) {
        System.arraycopy(movieId, 0, this.movieId, 0, Math.min(this.movieId.length, movieId.length));
        System.arraycopy(title, 0, this.title, 0, Math.min(this.title.length, title.length));
    }

    public Row(byte[] movieId, byte[] personId, byte[] category) {
        System.arraycopy(movieId, 0, this.movieId, 0, Math.min(this.movieId.length, movieId.length));
        System.arraycopy(personId, 0, this.personId, 0, Math.min(this.personId.length, personId.length));
        System.arraycopy(category, 0, this.category, 0, Math.min(this.category.length, category.length));
    }

    public Row(byte[] personId, byte[] name, boolean peopleTable) {
        System.arraycopy(personId, 0, this.personId, 0, Math.min(this.personId.length, personId.length));
        System.arraycopy(name, 0, this.name, 0, Math.min(this.name.length, name.length));
    }

    public Row(byte[] movieId, byte[] personId, File tempDataFile) {
        System.arraycopy(movieId, 0, this.movieId, 0, Math.min(this.movieId.length, movieId.length));
        System.arraycopy(personId, 0, this.personId, 0, Math.min(this.personId.length, personId.length));
    }

    public Row(byte[] movieId, byte[] title, byte[] personId, boolean bnl1) {
        System.arraycopy(movieId, 0, this.movieId, 0, Math.min(this.movieId.length, movieId.length));
        System.arraycopy(title, 0, this.title, 0, Math.min(this.title.length, title.length));
        System.arraycopy(personId, 0, this.personId, 0, Math.min(this.personId.length, personId.length));
    }

    public Row(byte[] movieId, byte[] title, byte[] personId, byte[] name) {
        System.arraycopy(movieId, 0, this.movieId, 0, Math.min(this.movieId.length, movieId.length));
        System.arraycopy(title, 0, this.title, 0, Math.min(this.title.length, title.length));
        System.arraycopy(personId, 0, this.personId, 0, Math.min(this.personId.length, personId.length));
        System.arraycopy(name, 0, this.name, 0, Math.min(this.name.length, name.length));
    }

    public byte[] getMovieId()
    {
        return movieId;
    }
    public byte[] getTitle()
    {
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

}
