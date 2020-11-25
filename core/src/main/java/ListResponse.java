import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ListResponse extends AbstractMessage {

    private final List<String> filesData;

    public ListResponse(Path dir) throws IOException {
        filesData = Files.list(dir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    public List<String> getFilesData() {
        return filesData;
    }
}
