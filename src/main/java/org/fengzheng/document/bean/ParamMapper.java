package org.fengzheng.document.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Tibers on 17/1/12.
 */
@Setter
@Getter
@NoArgsConstructor
public class ParamMapper implements Serializable {
    private String dataType;//注解名称
    private String required;//参数类型
    private String name;//参数名称
    private String paramType;//参数类型
    private String annotation;//注解名称
    private String oldType;//原始类型

    /**
     * @param p
     */
    public ParamMapper(String[] p) {
        /**
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
            annotation=p[0].toString().replaceAll("\\s+", "").replaceAll("@","");
            if(Objects.equals(annotation,"PathVariable")){
                paramType="path";
            }else{
                paramType="query";
            }
            oldType = p[1].toString().replaceAll("\\s+", "");
            if(oldType.equalsIgnoreCase("long")||oldType.equalsIgnoreCase("int")||oldType.equalsIgnoreCase("Integer")){
                dataType="int";
            }else{
                dataType="String";
            }
            name = p[2].toString().replaceAll("\\s+", "");

        }

    }

    @Override
    public String toString() {
        return String.format("    @ApiImplicitParam(name = \"%s\", value = \"%s\" %s, dataType = \"%s\",paramType = \"%s\"),", name, "", StringUtils
                .isEmpty(annotation) ? "" : ", required = true", dataType, paramType);
    }

}
