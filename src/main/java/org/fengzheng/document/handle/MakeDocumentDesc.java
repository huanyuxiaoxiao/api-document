package org.fengzheng.document.handle;

import org.fengzheng.document.util.FileUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by Tibers on 17/1/12.
 */
public class MakeDocumentDesc {
    private static final Logger _logger = Logger.getLogger("MakeDocumentDesc");
    private static final File controllerFolder = new File("/Users/Tibers/software/bwl/bwl-server/src/main/java/com/wangtiansoft/bwl/server/controller");
    private static final File outputFile = new File("/Users/Tibers/Desktop/bwl");

    private static final Set<String> unMakeFile = new HashSet<String>() {{
        add("ExceptionController");
        add("FileController");
    }};
    static List<String> oldFileContent = new ArrayList<>();
    static List<String> documentDesc = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        outputFile.deleteOnExit();
        outputFile.mkdirs();
        if (! controllerFolder.exists()) {
            throw new Exception("controller目录不存在");
        }
        for (File file : controllerFolder.listFiles()) {
            try {
                buildDocument(file);
                buildFinishFile(file.getName());
                oldFileContent.clear();
                documentDesc.clear();
            } catch (Exception e) {
                _logger.warning("接口文档生成失败:"+e.getMessage());
            }
        }
    }

    private static void buildFinishFile(String name) throws IOException {
        StringBuffer newFileContext = new StringBuffer();
        File out = new File(outputFile.getPath().concat(File.separator).concat(name));
        if(!out.exists()){
            out.createNewFile();
        }
        FileWriter fw = new FileWriter(out);
        BufferedWriter bw = new BufferedWriter(fw);
        int i = 0;
        for (String line : oldFileContent) {
            //以 "   public "开头的方法被认为是含有注解的方法,则为其追加接口文档
            if (line.matches("^\\s+public\\s+.*")) {
                newFileContext.append("    ".concat(documentDesc.get(i)));
                i++;
            }
            newFileContext.append(line).append("\n");

        }
        bw.write(newFileContext.toString());
        bw.close();
    }

    private static void buildDocument(File file) throws Exception {
        if (unMakeFile.contains(file.getName().replaceAll(".java$", ""))) {
            throw  new Exception(String.format("文件[%s]在不生成接口文档范围的列表中", file.getName()));
        }
        oldFileContent = FileUtil.readFileContext(file);
        documentDesc = FileUtil.readAndMakeString(oldFileContent);
    }
}
