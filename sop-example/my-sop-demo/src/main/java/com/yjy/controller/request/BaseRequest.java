package com.yjy.controller.request;

import lombok.Data;

@Data
public class BaseRequest<T> {

    private String name;

    private T data;
}
