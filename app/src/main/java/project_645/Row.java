package project_645;
import java.io.Serializable;

/**
 * Struct representing a database row, containing primary data types.
 */
public class Row implements Serializable {
    // Define primary data type fields, depending on the schema of the table
    // These fields are for the Movies table described below
    static final int MOVIE_ID_SIZE = 9;
    static final int TITLE_SIZE = 30;
    public byte[] movieId = new byte[9];
    public byte[] title = new byte[30];

    public Row(byte[] movieId, byte[] title) {
        System.arraycopy(movieId, 0, this.movieId, 0, Math.min(this.movieId.length, movieId.length));
        System.arraycopy(title, 0, this.title, 0, Math.min(this.title.length, title.length));
    }
}

