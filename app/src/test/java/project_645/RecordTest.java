package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RecordTest {
    private Record record;
    private BufferManagerImpl bufferManager;
    @BeforeEach
    public void setUp() {
        Row row = new Row(
                "m123".getBytes(),             // movieId
                "The Matrix".getBytes(),       // movieTitle
                "p456".getBytes(),             // personId
                "director".getBytes(),         // category
                "John Doe".getBytes()          // name
        );
        record = new Record(row, "m123".getBytes(), "The Matrix".getBytes(), "John Doe".getBytes(), new Rid(1, 2));
    }

    @Test
    public void testGetRow() {
        Row result = record.getRow();
        assertNotNull(result);
    }
    @Test
    public void testGetMovieIdBytes() {
        assertNotNull(record.getMovieIdBytes());
    }

    @Test
    public void testGetMovieTitleBytes() {
        assertNotNull(record.getMovieTitleBytes());
    }

    @Test
    public void testGetPersonIdBytes() {
        assertNotNull(record.getPersonIdBytes());
    }

    @Test
    public void testGetCategoryBytes() {
        assertNotNull(record.getCategoryBytes());
    }

    @Test
    public void testGetNameBytes() {
        assertNotNull(record.getNameBytes());
    }

    // Convert byte[] to String and trim it
    @Test
    public void testGetMovieIdDeserialized() {
        String movieId = record.getMovieIdDeserialized();
        assertNotNull(movieId);
    }

    @Test
    public void testGetTitleDeserialized() {
        assertNotNull(record.getTitleDeserialized());
    }

    @Test
    public void testGetPersonIdDeserialized() {
        assertNotNull(record.getPersonIdDeserialized());
    }

    @Test
    public void testGetCategory() {
        assertNotNull(record.getCategory());
    }

    @Test
    public void testGetName() {
        assertNotNull(record.getName());// Convert name (byte array) to String
    }

    @Test
    public void testGetRid() {
        assertNotNull(record.getRid());
    }
}