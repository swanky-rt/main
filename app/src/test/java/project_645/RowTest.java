package project_645;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RowTest {
    private Row row;

    @Test
    public void testRowInitializationAndGetters() {
        byte[] movieId = "m123".getBytes();
        byte[] title = "The Matrix".getBytes();
        byte[] personId = "p456".getBytes();
        byte[] category = "director".getBytes();
        byte[] name = "John Doe".getBytes();

        Row row = new Row(movieId, title, personId, category, name);

        // Check that contents were correctly copied
        assertArrayEquals(pad(movieId, Row.MOVIE_ID_SIZE), row.getMovieId(), "Movie ID mismatch");
        assertArrayEquals(pad(title, Row.TITLE_SIZE), row.getTitle(), "Title mismatch");
        assertArrayEquals(pad(personId, Row.PERSON_ID_SIZE), row.getPersonId(), "Person ID mismatch");
        assertArrayEquals(pad(category, Row.CATEGORY_SIZE), row.getCategory(), "Category mismatch");
        assertArrayEquals(pad(name, Row.NAME_SIZE), row.getName(), "Name mismatch");
    }

    @Test
    public void testNullInputs() {
        Row row = new Row(null, null, null, null, null);

        assertNotNull(row.getMovieId());
        assertNotNull(row.getTitle());
        assertNotNull(row.getPersonId());
        assertNotNull(row.getCategory());
        assertNotNull(row.getName());

        assertEquals(Row.MOVIE_ID_SIZE, row.getMovieId().length);
        assertEquals(Row.TITLE_SIZE, row.getTitle().length);
        assertEquals(Row.PERSON_ID_SIZE, row.getPersonId().length);
        assertEquals(Row.CATEGORY_SIZE, row.getCategory().length);
        assertEquals(Row.NAME_SIZE, row.getName().length);
    }

    // Helper method to pad the byte[] with trailing zeros to match fixed size
    private byte[] pad(byte[] input, int size) {
        byte[] padded = new byte[size];
        System.arraycopy(input, 0, padded, 0, Math.min(input.length, size));
        return padded;
    }
}
