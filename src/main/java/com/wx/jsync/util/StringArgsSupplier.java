package com.wx.jsync.util;


import com.wx.action.arg.ArgumentsSupplier;
import com.wx.action.arg.ObjectsSupplier;
import com.wx.util.representables.string.*;

import java.util.List;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 10.06.17.
 */
public class StringArgsSupplier implements ArgumentsSupplier {

    private final ObjectsSupplier supplier;

    public StringArgsSupplier(String[] args) {
        this.supplier = new ObjectsSupplier(args);
    }

    @Override
    public String supplyString() {
        return supplier.supplyString();
    }

    @Override
    public List<String> supplyStringList() {
        return new ListRepr<>(new StringRepr()).castOut(supplyString());
    }

    @Override
    public Boolean supplyBoolean() {
        return new BooleanRepr().castOut(supplyString());
    }

    @Override
    public Double supplyDouble() {
        return new DoubleRepr().castOut(supplyString());
    }

    @Override
    public Integer supplyInteger() {
        return new IntRepr().castOut(supplyString());
    }

    @Override
    public List<Integer> supplyIntegerList() {
        return new ListRepr<>(new IntRepr()).castOut(supplyString());
    }

    @Override
    public <T> List<T> supplyList(Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T supply(Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasMore() {
        return supplier.hasMore();
    }
}
