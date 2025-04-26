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
    public byte[] getRawKey() {
        return row.getMovieId();  // Get raw movieId as byte[]
    }

    public byte[] getRawValue() {
        return row.getTitle();  // Get raw title as byte[]
    }

    // Convert byte[] to String and trim it
    public String getMovieId() {
        return new String(row.getMovieId()).trim();  // Convert movieId (byte array) to String
    }

    public String getTitle() {
        return new String(row.getTitle()).trim();  // Convert title (byte array) to String
    }

    public String getPersonId() {
        return personId == null ? null : new String(personId).trim();  // Convert personId (byte array) to String
    }

    public String getCategory() {
        return category == null ? null : new String(category).trim();  // Convert category (byte array) to String
    }

    public String getName() {
        return name == null ? null : new String(name).trim();  // Convert name (byte array) to String
    }
}
