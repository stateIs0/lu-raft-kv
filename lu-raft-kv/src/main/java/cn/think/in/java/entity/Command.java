package cn.think.in.java.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 *
 * @author 莫那·鲁道
 */
@Getter
@Setter
@ToString
@Builder
public class Command implements Serializable {

    String key;

    String value;

}
