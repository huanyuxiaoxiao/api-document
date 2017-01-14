package org.fengzheng.document.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.fengzheng.document.handle.MakeDocumentDesc;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Tibers on 17/1/12.
 */
@Setter
@Getter
@NoArgsConstructor
public class ParamMapper implements Serializable {
    private final String HEADER_TOKEN_NAME = "Access-Token";
    private String dataType;//注解名称
    private String required;//参数类型
    private String name;//参数名称
    private String paramType;//参数类型
    private String annotation;//注解名称
    private String oldType;//原始类型
    private String value = "";//参数描述

    /**
     * @param p
     */
    public ParamMapper(String[] p) {
        /**
         * @PathVariable 注解有多个
         * @PathVariable 可选
         * String
         * mobile
         */
        if (p.length == 2) {
            oldType = p[0].toString().replaceAll("\\s+", "");
            paramType="query";
            if(oldType.equalsIgnoreCase("long")||oldType.equalsIgnoreCase("int")||oldType.equalsIgnoreCase("Integer")){
                dataType="int";
            }else{
                dataType="String";
            }
            name = p[1].toString().replaceAll("\\s+", "");

        } else {
            int maxPoit = p.length - 1;
            annotation=p[0].toString().replaceAll("\\s+", "").replaceAll("@","");
            if(Objects.equals(annotation,"PathVariable")){
                paramType="path";
            }else{
                paramType="query";
            }
            oldType = p[maxPoit - 1].toString().replaceAll("\\s+", "");
            if(oldType.equalsIgnoreCase("long")||oldType.equalsIgnoreCase("int")||oldType.equalsIgnoreCase("Integer")){
                dataType="int";
            }else{
                dataType="String";
            }
            name = p[maxPoit].toString().replaceAll("\\s+", "");
        }
        changeParamType();

    }

    private void changeParamType() {
        if (Objects.equals(oldType, MakeDocumentDesc.tokenParamType)) {
            this.paramType = "header";
            this.name = HEADER_TOKEN_NAME;
            this.value = "登录token";
            this.dataType = "String";
        }
    }

    @Override
    public String toString() {
        return String.format("    @ApiImplicitParam(name = \"%s\", value = \"%s\" %s, dataType = \"%s\",paramType = \"%s\"),", name, value, StringUtils
                .isEmpty(annotation) ? "" : ", required = true", dataType, paramType);
    }

}
