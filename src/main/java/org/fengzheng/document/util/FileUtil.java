package org.fengzheng.document.util;

import org.fengzheng.document.bean.ParamMapper;
import org.fengzheng.document.handle.MakeDocumentDesc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tibers on 17/1/12.
 */
public class FileUtil {
    private static final Logger _logger = Logger.getLogger("FileUtil");
    private static final String REQUEST_METHOD_COMPLETE_LINE = "^\\s+public\\s+[a-zA-Z]+\\s+[a-zA-Z]+\\(([a-zA-Z@,<>\\[\\]\\s]*)\\)\\s*(throws\\s+[a-zA-Z]*Exception)*\\s+\\{\\s*$";
    private static final String REQUEST_METHOD_NOT_COMPLETE_LINE_BEGIN = "^\\s+public\\s+([a-zA-Z]+)\\s+[a-zA-Z]+\\(([a-zA-Z@,<>\\[\\]\\s]*)$";
    private static final String REQUEST_METHOD_NOT_COMPLETE_LINE_END = "^\\s+[a-zA-Z@,<>\\[\\]\\s]*[)]*\\s+[throws\\s+[a-zA-Z]*Exception]*\\s+\\{\\s*$";
    static StringBuffer notComplete = new StringBuffer();
    static Set<String> makeTypeSet = new HashSet<String>() {{
        add("String");
        add("Long");
        add("int");
        add("long");
        add("Integer");
        add("List");
    }};
    static boolean completeFlag = true;//完整行标记

    public static List<String> readFileContext(File file) throws Exception {
        if (! file.getName().endsWith(".java")) {
            throw new Exception(String.format("非java源码文件[%s]",file.getName()));
        }
        List<String> wait = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String lineString;
        while ((lineString = br.readLine()) != null) {
            wait.add(lineString);
        }
        return wait;
    }
    private static List<String> makeMethodMessage(List<String> oldFileContext){
        List<String> result=new ArrayList<>();
        for (String lineString : oldFileContext) {
            if (lineString.matches("^\\s+public.*")) {
                if (matchCompleteMethod(lineString)) {
                    result.add(lineString);
                } else if (matchNotCompleteMethodBegin(lineString)) {
                    completeFlag = false;
                    notComplete.append(lineString);
                } else {
                    System.out.println(lineString);
                }
            } else {
                if (matchNotCompleteMethodEnd(lineString)) {
                    notComplete.append(lineString.replaceAll("\\s+", " "));
                    result.add(notComplete.toString());
                    notComplete.setLength(0);
                    completeFlag = true;
                } else if (! completeFlag) {
                    notComplete.append(lineString.replaceAll("\\s+", " "));
                }
            }
        }
        return result;
    }

    private static boolean matchCompleteMethod(String lineString) {
        return lineString.matches(REQUEST_METHOD_COMPLETE_LINE);
    }

    private static boolean matchNotCompleteMethodBegin(String lineString) {
        return lineString.matches(REQUEST_METHOD_NOT_COMPLETE_LINE_BEGIN);
    }

    private static boolean matchNotCompleteMethodEnd(String lineString) {
        return lineString.matches(REQUEST_METHOD_NOT_COMPLETE_LINE_END);
    }

    private static String getParamString(String line) {
        Pattern pattern = Pattern.compile(REQUEST_METHOD_COMPLETE_LINE);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static List<String> readAndMakeString(List<String> oldFileContext) throws Exception {
        List<String> list=makeMethodMessage(oldFileContext);
        List<String> finish = new ArrayList<>();
        List<List<ParamMapper>> all = new ArrayList<>();
        //参数列表整理成实体类
        for (String s : list) {
            List<ParamMapper> paramList = new ArrayList<>();
            String paramString = getParamString(s);
            if(paramString==null){
                continue;
            }
            String[] params = paramString.split(",");
            for (String param : params) {
                String[] p = param.trim().split("\\s+");
                ParamMapper mapper = new ParamMapper(p);
                if (checkInclude(mapper) && MakeDocumentDesc.makeNormalParams) {
                    paramList.add(mapper);
                }
                if (Objects.equals(mapper.getParamType(), "header") && MakeDocumentDesc.makeHeaderParams) {
                    paramList.add(mapper);
                    continue;
                }
                 _logger.warning(String.format("未包含在允许生成接口说明的类型中[%s]", mapper.toString()));
            }
            all.add(paramList);
        }
        //整理成最后格式
        for (List<ParamMapper> paramMappers : all) {
            StringBuilder method = new StringBuilder();
            if (paramMappers.size() > 1) {
                method.append("@ApiImplicitParams({").append("\n");
            }
            for (ParamMapper paramMapper : paramMappers) {
                if (paramMappers.size() > 1) {
                    method.append("    ");
                }
                method.append(paramMapper.toString()).append("\n");
            }
            if (paramMappers.size() > 1) {
                method.append("    ");
                method.append("})").append("\n");
            }
            if (paramMappers.size() == 1) {
                finish.add(method.toString().replaceAll(",$", ""));
            } else {
                finish.add(method.toString());
            }
        }
        return finish;
    }

    private static boolean checkInclude(ParamMapper mapper) {
        for (String s : makeTypeSet) {
            if (mapper.getOldType().equalsIgnoreCase(s) || mapper.getOldType().startsWith(s)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}
