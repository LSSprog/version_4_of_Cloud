public class RenameFile extends AbstractMessage{

    private String fileName;
    private String newFileName;

    public RenameFile(String fileName, String newFileName) {
        this.fileName = fileName;
        this.newFileName = newFileName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getNewFileName() {
        return newFileName;
    }
}
