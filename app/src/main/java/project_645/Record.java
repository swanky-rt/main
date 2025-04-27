package project_645;

public class Record {
    private final Row row;
    private final byte[] personId;
    private final byte[] category;
    private final byte[] name;

    // Constructor for handling fields from Movies and WorkedOn tables
    public Record(Row row, byte[] personId, byte[] category, byte[] name) {
        this.row = row;
        this.personId = personId;
        this.category = category;
        this.name = name;
    }

    // Accessors for the fields in the Row
    public byte[] getMovieIdBytes() {
        return row.getMovieId();  // Get raw movieId as byte[]
    }

    public byte[] getMovieTitleBytes() {
        return row.getTitle();  // Get raw title as byte[]
    }

    public byte[] getPersonIdBytes() {
        return row.getPersonId();
    }

    public byte[] getCategoryBytes() {
        return row.getCategory();
    }

    public byte[] getNameBytes() {
        return row.getName();
    }

    // Convert byte[] to String and trim it
    public String getMovieIdDeserialized() {
        return new String(row.getMovieId()).trim();  // Convert movieId (byte array) to String
    }

    public String getTitleDeserialized() {
        return new String(row.getTitle()).trim();  // Convert title (byte array) to String
    }

    public String getPersonIdDeserialized() {
        return new String(row.getPersonId()).trim();  // Convert personId (byte array) to String
    }

    public String getCategory() {
        return new String(row.getCategory()).trim();  // Convert category (byte array) to String
    }

    public String getName() {
        return new String(row.getName()).trim();  // Convert name (byte array) to String
    }
}