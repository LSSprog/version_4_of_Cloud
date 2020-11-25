import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.file.*;

public class FileManager extends ChannelInboundHandlerAdapter {

    private String dir = "Server_dir";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileRequest) {
            FileRequest request = (FileRequest) msg;
            ctx.writeAndFlush(new FileMessage(Paths.get(dir, request.getFileName())));
        }
        if (msg instanceof ListRequest) {
            ctx.writeAndFlush(new ListResponse(Paths.get(dir)));
        }
        if (msg instanceof FileMessage) {
            FileMessage file = (FileMessage) msg;
            Files.write(Paths.get(dir, file.getFileName()), file.getData(), StandardOpenOption.CREATE);
        }
        if (msg instanceof DeleteFile) {
            DeleteFile file = (DeleteFile) msg;
            Files.delete(Paths.get(dir, file.getFileName()));
        }
        if (msg instanceof RenameFile) {
            RenameFile file = (RenameFile) msg;
            Files.move(Paths.get(dir, file.getFileName()), Paths.get(dir,file.getNewFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
