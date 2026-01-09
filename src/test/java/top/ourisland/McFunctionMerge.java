package top.ourisland;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class McFunctionMerge extends JFrame {

    private final JLabel status = new JLabel("把文件夹拖到此窗口", SwingConstants.CENTER);

    public McFunctionMerge() {
        super("合并工具");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(520, 200);
        setLocationRelativeTo(null);

        status.setFont(status.getFont().deriveFont(16f));
        add(status, BorderLayout.CENTER);

        new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                handleDrop(dtde);
            }
        });
    }

    private void handleDrop(DropTargetDropEvent dtde) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);

            if (!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                setStatus("拖拽内容不支持（请拖入文件夹）");
                dtde.dropComplete(false);
                return;
            }

            @SuppressWarnings("unchecked")
            List<File> dropped = (List<File>) dtde.getTransferable()
                    .getTransferData(DataFlavor.javaFileListFlavor);

            if (dropped.isEmpty()) {
                setStatus("未检测到拖入内容");
                dtde.dropComplete(false);
                return;
            }

            File first = dropped.getFirst();
            File dir = first.isDirectory() ? first : first.getParentFile();
            if (dir == null || !dir.isDirectory()) {
                setStatus("请拖入一个文件夹");
                dtde.dropComplete(false);
                return;
            }

            // 选择输出文件
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("选择输出文件（合并结果）");
            chooser.setSelectedFile(new File("merged.txt"));
            int result = chooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                setStatus("已取消保存");
                dtde.dropComplete(true);
                return;
            }

            File out = chooser.getSelectedFile();
            mergeFiles(dir.toPath(), out.toPath());
            setStatus("合并完成： " + out.getAbsolutePath());
            dtde.dropComplete(true);

        } catch (Exception ex) {
            setStatus("发生错误： " + ex.getMessage());
            dtde.dropComplete(false);
        }
    }

    private void setStatus(String text) {
        SwingUtilities.invokeLater(() -> status.setText(text));
    }

    private void mergeFiles(Path baseDir, Path outputFile) throws IOException {
        List<Path> mcFiles;
        try (Stream<Path> s = Files.walk(baseDir)) {
            mcFiles = s.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(p -> baseDir.relativize(p).toString()))
                    .toList();
        }

        if (mcFiles.isEmpty()) {
            throw new IOException("该文件夹内未找到任何文件");
        }

        // 确保父目录存在
        Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            boolean firstBlock = true;

            for (Path p : mcFiles) {
                if (!firstBlock) {
                    writer.newLine();
                    writer.newLine(); // 文件之间空行隔开
                }
                firstBlock = false;

                String rel = baseDir.relativize(p).toString();
                writer.write(rel);
                writer.newLine();

                // 读取文件内容（UTF-8；如果你有非UTF-8文件，可告诉我改成“自动探测/容错”）
                List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new McFunctionMerge().setVisible(true));
    }
}
