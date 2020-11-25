public class DeleteFile extends AbstractMessage{

    private final String fileName;

    public DeleteFile(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
